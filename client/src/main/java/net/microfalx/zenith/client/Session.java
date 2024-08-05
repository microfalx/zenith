package net.microfalx.zenith.client;

import net.microfalx.lang.*;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.Resource;
import net.microfalx.zenith.base.ZenithUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverCommandExecutor;
import org.openqa.selenium.remote.service.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A wrapper over a Selenium Driver with additional enhancements.
 */
public class Session extends NamedIdentityAware<String> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final Metrics METRIC = Metrics.of("Selenium");

    public static final String TASK_NAME_PREFIX = "Zenith Selenium";
    public static final String JAVA_SCRIPT_TAG = "script";
    public static final String SCREEN_SHOT_TAG = "screenshot";
    public static final String LOGS_TAG = "logs";
    public static final String CAPABILITY = "zenit:options";
    public static final String LOGGING_CAPABILITY = "loggingPrefs";
    public static final String PROJECT_CAPABILITY = "sessionProject";
    public static final String NAME_CAPABILITY = "sessionName";
    public static final String PACKAGE_CAPABILITY = "sessionPackage";
    public static final String CATEGORY_CAPABILITY = "sessionCategory";
    public static final String TAGS_CAPABILITY = "sessionTags";

    private static final int WIDTH = 1680;
    private static final int HEIGHT = 1050;

    private static final Collection<Session> sessions = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<Session> CURRENT = new ThreadLocal<>();

    private final String id = UUID.randomUUID().toString();
    private volatile String name;
    private volatile String _package;
    private volatile String project;
    private volatile String category;
    private final Set<String> tags = new HashSet<>();
    private volatile String description;
    private final Options options;
    private final long startTime = currentTimeMillis();
    private volatile long endTime;
    private volatile long lastRequest = startTime;
    private volatile Level level = Level.INFO;
    private volatile URI lastUri;
    private volatile boolean waitedForPageToLoad;
    private final Collection<Resource> screenshots = new CopyOnWriteArrayList<>();

    private final StringBuilder logger = new StringBuilder();
    private final StringBuilder browserLogger = new StringBuilder();
    private final StringBuilder clientLogger = new StringBuilder();
    private final StringBuilder driverLogger = new StringBuilder();
    private final StringBuilder serverLogger = new StringBuilder();
    private final Map<String, Collection<LogEntry>> logEntries = new ConcurrentHashMap<>();
    private volatile OutputStream driverLoggerOutput;
    private volatile WebDriver driver;
    private volatile boolean attached;
    private volatile boolean keepOpen;
    private volatile Resource storage;
    private volatile Resource reportsDirectory;
    private volatile boolean closed;
    private boolean disableScreenshot;

    /**
     * Creates a new selenium session with give options.
     *
     * @param options the options
     * @return a non-null instance
     */
    public static Session create(Options options) {
        return new Session(options);
    }

    /**
     * Creates a new local selenium session with default browser (Chrome).
     *
     * @return a non-null instance
     */
    public static Session local() {
        return create(Options.create().withLocal(true));
    }

    /**
     * Creates a new remote (default grid unless configuration is changed) selenium session with default browser (Chrome).
     *
     * @return a non-null instance
     */
    public static Session remote() {
        return create(Options.create().withLocal(false));
    }

    /**
     * Returns the session associated with the current thread.
     * <p>
     * If a sessions does not exist, a local headless session is created, with default browser (Chrome).
     *
     * @return a non-null instance
     */
    public static Session get() {
        return get(true);
    }

    /**
     * Returns the session associated with the current thread.
     *
     * @param create <code>true</code> to create the session if it does not exist, <code>false</code> otherwise
     * @return a non-null instance
     */
    public static Session get(boolean create) {
        Session session = CURRENT.get();
        if (session == null && create) {
            session = local();
            CURRENT.set(session);
        }
        return session;
    }

    /**
     * Changes the session associated with the current thread.
     *
     * @param session the session, null to detached the current thread
     */
    public static void set(Session session) {
        if (session == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(session);
        }
    }

    /**
     * Returns all created sessions.
     *
     * @return a non-null instance
     */
    public static Collection<Session> getSessions() {
        return unmodifiableCollection(sessions);
    }

    private Session(Options options) {
        requireNonNull(options);
        logger.append("Create session");
        this.options = options;
        registerSession();
        initOptions();
        CURRENT.set(this);
    }

    public String getPackage() {
        return _package;
    }

    public void setPackage(String _package) {
        this._package = _package;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTag(String tag) {
        requireNonNull(tag);
        this.tags.add(tag);
    }

    public boolean isDisableScreenshot() {
        return disableScreenshot;
    }

    public Session setDisableScreenshot(boolean disableScreenshot) {
        this.disableScreenshot = disableScreenshot;
        return this;
    }

    public boolean isClosed() {
        return closed;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastRequest() {
        return lastRequest;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return endTime > 0 ? endTime - startTime : currentTimeMillis() - startTime;
    }

    public Level getLevel() {
        return level;
    }

    public Session setLevel(Level level) {
        this.level = level;
        return this;
    }

    public Options getOptions() {
        return options;
    }

    public Resource getStorage() {
        if (storage == null) {
            storage = ZenithUtils.getLocalStorage().resolve("session", Resource.Type.DIRECTORY);
        }
        return storage;
    }

    public Session setStorage(Resource storage) {
        requireNonNull(storage);
        this.storage = storage;
        return this;
    }

    public Resource getReportsDirectory() {
        return reportsDirectory;
    }

    public Session setReportsDirectory(Resource reportsDirectory) {
        requireNonNull(reportsDirectory);
        this.reportsDirectory = reportsDirectory;
        return this;
    }

    public synchronized WebDriver getWebDriver() {
        touch();
        if (driver == null) executeTask(o -> initializeDriver());
        return driver;
    }

    public String getDriverSessionId() {
        try {
            RemoteWebDriver webDriver = (RemoteWebDriver) getWebDriver();
            return webDriver.getSessionId().toString();
        } catch (Exception e) {
            throw new SessionException("Failed to acquire driver session id", e);
        }
    }

    public URI getDriverUri() {
        try {
            RemoteWebDriver webDriver = (RemoteWebDriver) getWebDriver();
            DriverCommandExecutor commandExecutor = (DriverCommandExecutor) webDriver.getCommandExecutor();
            return commandExecutor.getAddressOfRemoteServer().toURI();
        } catch (Exception e) {
            throw new SessionException("Failed to acquire driver remote address", e);
        }
    }

    public String getLogs() {
        return getLogs(true);
    }

    public String getLogs(boolean includeSession) {
        StringBuilder logger = new StringBuilder();
        if (includeSession) {
            appendLogger("Session", logger, this.logger);
        }
        StringBuilder browserLogger = this.getBrowserLogger();
        if (browserLogger.length() > 0) {
            appendLogger("Browser", logger, browserLogger);
        }
        StringBuilder seleniumLogger = this.getSeleniumLogger();
        if (seleniumLogger.length() > 0) {
            appendLogger("Selenium", logger, seleniumLogger);
        }
        return logger.toString();
    }

    private void appendLogger(String title, StringBuilder target, StringBuilder source) {
        String output = source.toString();
        if (!StringUtils.isNotEmpty(output)) {
            logHeader(target, title);
            target.append(TextUtils.insertSpaces(output, 2));
        }
    }

    public StringBuilder getSessionLogger() {
        return logger;
    }

    public StringBuilder getBrowserLogger() {
        return getBrowserLogger(null);
    }

    public StringBuilder getBrowserLogger(Level equalOrAboveLevelOnly) {
        StringBuilder browserLogger = new StringBuilder();
        collectLogs(LogType.BROWSER, browserLogger, equalOrAboveLevelOnly);
        return browserLogger;
    }

    public StringBuilder getClientLogger() {
        return getClientLogger(null);
    }

    public StringBuilder getClientLogger(Level equalOrAboveLevelOnly) {
        StringBuilder clientLogger = new StringBuilder();
        collectLogs(LogType.CLIENT, clientLogger, equalOrAboveLevelOnly);
        return clientLogger;
    }

    public StringBuilder getDriverLogger() {
        return getDriverLogger(null);
    }

    public StringBuilder getDriverLogger(Level equalOrAboveLevelOnly) {
        StringBuilder driverLogger = new StringBuilder();
        collectLogs(LogType.DRIVER, driverLogger, equalOrAboveLevelOnly);
        return driverLogger;
    }

    public StringBuilder getServerLogger() {
        return getServerLogger(null);
    }

    public StringBuilder getServerLogger(Level equalOrAboveLevelOnly) {
        StringBuilder serverLogger = new StringBuilder();
        collectLogs(LogType.SERVER, serverLogger, equalOrAboveLevelOnly);
        return serverLogger;
    }

    public StringBuilder getSeleniumLogger() {
        StringBuilder seleniumLogger = new StringBuilder();
        appendLogger("Client", seleniumLogger, clientLogger);
        appendLogger("Driver", seleniumLogger, driverLogger);
        appendLogger("Server", seleniumLogger, serverLogger);
        return seleniumLogger;
    }

    /**
     * Resets the rule and browser state.
     */
    public void reset() {
        logInfo(getSessionLogger(), "Reset session");
        lastUri = null;
        name = null;
        logEntries.clear();
        if (driver == null) return;
    }

    /**
     * Returns all captured screenshots, in the order they were captured.
     *
     * @return a non-null instance
     */
    public Collection<Resource> getScreenshots() {
        return unmodifiableCollection(screenshots);
    }

    /**
     * Takes a screenshot.
     */
    public Resource takeScreenShot() {
        return takeScreenShot("No Reason");
    }

    /**
     * Takes a screenshot and present a reason
     */
    public Resource takeScreenShot(String reason) {
        touch();
        if (!(driver instanceof TakesScreenshot)) {
            LOGGER.info("Driver " + driver + " does not support taking screen shots");
            return null;
        }
        String fileFragment = StringUtils.toIdentifier(reason);
        try {
            byte[] screenShot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String fileName = "screenshot_" + fileFragment + "_" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + ".png";
            logInfo(logger, "Take screenshot for '" + reason + "', file name " + fileName);
            Resource screenShotResource = getStorage().resolve(fileName, Resource.Type.FILE);
            if (!disableScreenshot) {
                IOUtils.appendStream(screenShotResource.getOutputStream(), new ByteArrayInputStream(screenShot));
                screenshots.add(screenShotResource);
            }
            LOGGER.info("Screen shot available at " + screenShotResource.toString());
            return screenShotResource;
        } catch (IOException e) {
            LOGGER.warn("Failed to move screen shot to , reason: " + ExceptionUtils.getRootCauseMessage(e));
        } catch (WebDriverException e) {
            LOGGER.warn("Screen shot could not be taken, reason: " + ExceptionUtils.getRootCauseMessage(e));
        }

        return null;
    }


    /**
     * Opens a page, waiting to be loaded.
     *
     * @param uri the URI/URL
     * @return self
     */
    public Session open(String uri) {
        requireNonNull(uri);
        return open(URI.create(uri));
    }

    /**
     * Opens a page, waiting to be loaded.
     *
     * @param uri the URI/URL
     * @return self
     */
    public Session open(URI uri) {
        return open(uri, true);
    }

    /**
     * Opens a page.
     *
     * @param uri     the URI/URL
     * @param waitFor <code>true</code> to wait to be loaded, <code>false</code> otherwise
     * @return self
     */
    public Session open(URI uri, boolean waitFor) {
        requireNonNull(uri);
        logEntries.clear();
        if (lastUri != null && lastUri.equals(uri)) {
            LOGGER.info("URI '" + uri + "' is already opened");
        } else {
            waitedForPageToLoad = false;
            if (StringUtils.isEmpty(name)) name = uri.getHost();
            description = uri.toASCIIString();
            logInfo(logger, "Open '" + uri + "'");
            getWebDriver().get(uri.toASCIIString());
            if (waitFor) waitUntilPageLoaded();
            if (waitFor) {
                takeScreenShot("After Open");
            }
            this.lastUri = uri;
        }
        return this;
    }

    /**
     * Waits until the page is reported  in "loaded" state.
     * <p>
     * This method relies on the DOM reporing the "loaded" event
     */
    public void waitUntilPageLoaded() {
        if (!waitedForPageToLoad) {
            getWebDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            waitedForPageToLoad = true;
        }
    }

    /**
     * Keeps the session open (for troubleshooting purposes).
     */
    public void keepOpen() {
        this.keepOpen = true;
    }

    /**
     * Closes the current window
     *
     * @see WebDriver#close()
     */
    public void closeWindow() {
        if (keepOpen) return;
        if (driver != null && !attached) {
            logInfo(logger, "Close driver");
            try {
                executeTask(o -> driver.close());
            } catch (Exception e) {
                LOGGER.warn("Failed to close driver", e);
            }
        }
    }

    /**
     * Closes the session and releases the aassociated web driver.
     *
     * @see WebDriver#quit()
     */
    @Override
    public void close() {
        if (keepOpen) return;
        endTime = currentTimeMillis();
        takeScreenShot("Close Session");
        if (driver != null && !attached) {
            logInfo(logger, "Close driver");
            try {
                executeTask(o -> driver.close());
            } catch (Exception e) {
                LOGGER.warn("Failed to close driver", e);
            }
            logInfo(logger, "Quit driver");
            try {
                executeTask(o -> driver.quit());
            } catch (Exception e) {
                LOGGER.warn("Failed to quit driver", e);
            }
        }
        // if the session that's closed is also the one attached to the current thread, we should detached the session
        if (CURRENT.get() == this) CURRENT.remove();
        closed = true;
    }

    /**
     * Attaches a web driver to be used with the session instead of creating one using the options.
     *
     * @param driver the web driver
     */
    private void attach(WebDriver driver) {
        this.driver = driver;
        this.attached = true;
    }

    /**
     * Creates and initializes the driver, if not already created.
     */
    private void initializeDriver() {
        logInfo(logger, "Initialize session, options: " + options.toDescription());
        DriverManager.getInstance().registerDriver(options.getBrowser());
        logInfo(logger, "Create driver");
        if (options.isLocal()) {
            driver = createLocalDriver();
            logInfo(logger, "Web driver created with local browser " + driver.toString());
        } else {
            URL url = getHubDriverUrl();
            driver = new RemoteWebDriver(url, getCapabilities());
            logInfo(logger, "Web driver created using Selenium Hub (" + url + "), remote browser " + driver.toString());
        }
    }

    /**
     * Returns the Selenium HUB URL.
     *
     * @return a non-null url
     */
    private URL getHubDriverUrl() {
        URI uri;
        if (options.getUri() != null) {
            uri = options.getUri();
        } else {
            uri = Options.DEFAULT_URI;
        }
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot create Selenium Hub URL", e);
        }
    }

    /**
     * Returns the capabilities for the target browser.
     *
     * @return the capabilities
     */
    protected Capabilities getCapabilities() {
        setupSystemProperties();
        Capabilities capabilities;
        switch (options.getBrowser()) {
            case CHROME:
                ChromeOptions chromeOptions = new ChromeOptions();
                updateCommonCapabilities(chromeOptions);
                // TODO how do we bring this back???
                //chromeOptions.setLogLevel(ChromeDriverLogLevel.SEVERE);
                chromeOptions.setCapability(ChromeOptions.LOGGING_PREFS, getLoggingPreferences());
                if (options.isHeadless()) chromeOptions.addArguments("--headless=new");
                chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--ignore-certificate-errors");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                if (options.isFullScreen()) {
                    chromeOptions.addArguments("--start-maximized");
                } else {
                    chromeOptions.addArguments(String.format("--window-size=%d,%d", WIDTH, HEIGHT));
                }
                if (options.isDebug()) {
                    chromeOptions.addArguments("--auto-open-devtools-for-tabs");
                }
                //chromeOptions.addExtensions(new File("/home/ady/download/10.02/App-Inspector-for-Sencha™_v2.0.4.crx"));
                chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                //chromeOptions.setExperimentalOption("useAutomationExtension", false);
                chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
                capabilities = chromeOptions;
                break;
            case IE:
                InternetExplorerOptions ieOptions = new InternetExplorerOptions();
                updateCommonCapabilities(ieOptions);
                ieOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                capabilities = ieOptions;
                break;
            case FIREFOX:
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
                updateCommonCapabilities(firefoxOptions);
                if (options.isHeadless()) firefoxOptions.addArguments("--headless");
                if (options.isDebug()) firefoxOptions.addArguments("--devtools");
                firefoxOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                firefoxOptions.addArguments(String.format("--width=%d", WIDTH));
                firefoxOptions.addArguments(String.format("--height=%d", HEIGHT));
                capabilities = firefoxOptions;
                break;
            default:
                throw new IllegalStateException("Unhandled browser type: " + options.getBrowser());
        }
        return capabilities;
    }

    /**
     * Returns a local driver capabilities based on the target browser.
     *
     * @return the capabilities
     */
    private WebDriver createLocalDriver() {
        final Capabilities capabilities = getCapabilities();
        switch (options.getBrowser()) {
            case CHROME:
                return new ChromeDriver((ChromeDriverService) createDriverService(capabilities), (ChromeOptions) capabilities);
            case IE:
                return new InternetExplorerDriver((InternetExplorerDriverService) createDriverService(capabilities), (InternetExplorerOptions) capabilities);
            case FIREFOX:
                return new FirefoxDriver((FirefoxDriverService) createDriverService(capabilities), (FirefoxOptions) capabilities);
            default:
                throw new IllegalStateException("Unhandled browser type: " + options.getBrowser());
        }
    }

    private DriverService createDriverService(Capabilities capabilities) {
        driverLoggerOutput = new ByteArrayOutputStream();
        switch (options.getBrowser()) {
            case CHROME:
                return new ChromeDriverService.Builder().withLogLevel(ChromiumDriverLogLevel.INFO)
                        .withLogOutput(driverLoggerOutput)
                        .build();
            case IE:
                return new InternetExplorerDriverService.Builder().withLogLevel(InternetExplorerDriverLogLevel.INFO)
                        .withLogOutput(driverLoggerOutput)
                        .build();
            case FIREFOX:
                return new GeckoDriverService.Builder().withLogLevel(FirefoxDriverLogLevel.INFO)
                        .withLogOutput(driverLoggerOutput)
                        .build();
            default:
                throw new IllegalStateException("Unhandled browser type: " + options.getBrowser());
        }
    }

    /**
     * Returns driver capabilities based on the target browser.
     */
    private void updateCommonCapabilities(MutableCapabilities capabilities) {
        MutableCapabilities customCapabilities = new MutableCapabilities();
        if (StringUtils.isNotEmpty(name)) customCapabilities.setCapability(NAME_CAPABILITY, name);
        if (StringUtils.isNotEmpty(_package)) customCapabilities.setCapability(PACKAGE_CAPABILITY, _package);
        if (StringUtils.isNotEmpty(category)) customCapabilities.setCapability(CATEGORY_CAPABILITY, category);
        if (StringUtils.isNotEmpty(project)) customCapabilities.setCapability(PROJECT_CAPABILITY, project);
        if (!tags.isEmpty()) customCapabilities.setCapability(TAGS_CAPABILITY, CollectionUtils.setToString(tags));
        capabilities.setCapability(CAPABILITY, customCapabilities);
    }

    /**
     * Returns the logging preferences.
     *
     * @return a non-null instance
     */
    private LoggingPreferences getLoggingPreferences() {
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, level);
        logPrefs.enable(LogType.CLIENT, Level.INFO);
        logPrefs.enable(LogType.DRIVER, level == Level.INFO ? Level.WARNING : level);
        logPrefs.enable(LogType.SERVER, Level.INFO);
        return logPrefs;
    }

    private void setupSystemProperties() {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
    }

    private void registerSession() {
        synchronized (Session.class) {
            if (sessions.isEmpty()) {
                logInfo(logger, "Register cleanup hook");
                Runtime.getRuntime().addShutdownHook(new CleanupThread());
            }
        }
        sessions.add(this);
    }

    /**
     * Tracks a task related Selenium.
     *
     * @param consumer the consumer
     * @param <T>      the consumer type
     */
    protected final <T> void executeTask(Consumer<T> consumer) {
        executeTask(StringUtils.EMPTY_STRING, consumer);
    }

    /**
     * Tracks a task related Selenium.
     *
     * @param consumer the consumer
     * @param <T>      the consumer type
     */
    protected final <T> void executeTask(String name, Consumer<T> consumer) {
        name = (StringUtils.isNotEmpty(name) ? TASK_NAME_PREFIX + " - " + name : TASK_NAME_PREFIX) + " (" + options.getBrowser().name() + ")";
        METRIC.time(name, o -> consumer.accept(null));
    }


    /**
     * Initializes various internal settings based on external options.
     */
    private void initOptions() {
        if (options.isDebug()) {
            level = Level.FINEST;
        }
    }

    /**
     * Touches the session, to mark it "in use".
     */
    private void touch() {
        lastRequest = currentTimeMillis();
    }

    /**
     * Appends a logs from a source logger to a target logger, inserting a heading if there is at least one message.
     *
     * @param target the target
     * @param source the source
     * @param title  the title
     */
    private void collectLogs(StringBuilder target, StringBuilder source, String title) {
        if (source.length() > 0) logHeader(target, title);
        target.append(source);
    }

    /**
     * Collect logs from Selenium
     *
     * @param type   the type of log
     * @param logger the logger
     */
    private void collectLogs(String type, StringBuilder logger, Level equalOrAboveLevelOnly) {
        if (driver == null) return;
        Iterator<LogEntry> logEntries = Collections.emptyIterator();
        try {
            Logs logs = driver.manage().logs();
            Set<String> availableLogTypes = logs.getAvailableLogTypes();
            if (!availableLogTypes.contains(type)) return;
            logEntries = logs.get(type).iterator();
        } catch (Exception e) {
            if (e.getMessage().contains("did not match a known command")) {
                LOGGER.info("Logs " + type + " are not available");
            } else {
                LOGGER.warn("Failed to extract logs of type " + type + ", root cause: " + ExceptionUtils.getRootCauseMessage(e));
            }
        }
        Collection<LogEntry> currentLogEntries = this.logEntries.computeIfAbsent(type, s -> new ConcurrentLinkedQueue<>());
        while (logEntries.hasNext()) {
            LogEntry logEntry = logEntries.next();
            currentLogEntries.add(logEntry);
        }
        for (LogEntry logEntry : currentLogEntries) {
            dumpLogEntry(logger, logEntry, equalOrAboveLevelOnly);
        }
    }

    /**
     * Logs a header.
     *
     * @param logger the logger
     * @param title  the title
     */
    private void logHeader(StringBuilder logger, String title) {
        logger.append(StringUtils.getStringOfChar('=', 120)).append('\n');
        logger.append(title + " Logs").append('\n');
        logger.append(StringUtils.getStringOfChar('=', 120)).append('\n');
    }

    /**
     * Appends the selenium log entry to the logger.
     *
     * @param logger   the logger
     * @param logEntry the log entry
     */
    private void dumpLogEntry(StringBuilder logger, LogEntry logEntry, Level equalOrAboveLevelOnly) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(LOG_TIME_FORMATTER.format(TimeUtils.toLocalDateTime(logEntry.getTimestamp()))).append(' ');
        buffer.append("[").append(logEntry.getLevel().getName()).append("] - ")
                .append(logEntry.getMessage());

        String message = buffer.toString();
        message = StringUtils.trim(message);
        if (StringUtils.isEmpty(message)) return;

        int level = changeLogEntryLevel(logEntry.getLevel(), message).intValue();
        if (equalOrAboveLevelOnly != null && level <= equalOrAboveLevelOnly.intValue()) return;
        if (level >= this.level.intValue()) {
            if (level <= Level.FINE.intValue()) {
                logDebug(logger, true, message);
            } else if (level <= Level.INFO.intValue()) {
                logInfo(logger, true, message);
            } else if (level <= Level.WARNING.intValue()) {
                logWarn(logger, true, message);
            } else {
                logError(logger, true, message);
            }
        }
    }

    /**
     * Changes the severity level based on additional application rules.
     *
     * @param level   the original level
     * @param message the message
     * @return the final level
     */
    private Level changeLogEntryLevel(Level level, String message) {
        if (level == Level.SEVERE && message.endsWith("401 (Unauthorized)")) {
            return Level.INFO;
        } else {
            return level;
        }
    }

    private void logDebug(StringBuilder builder, String message) {
        logInfo(builder, false, message);
    }

    private void logDebug(StringBuilder builder, boolean includeLevel, String message) {
        if (includeLevel) builder.append("DEBUG  ");
        builder.append(message).append('\n');
    }

    private void logInfo(StringBuilder builder, String message) {
        logInfo(builder, false, message);
    }

    private void logInfo(StringBuilder builder, boolean includeLevel, String message) {
        if (includeLevel) builder.append("INFO  ");
        builder.append(message).append('\n');
    }

    private void logWarn(StringBuilder builder, String message) {
        logWarn(builder, false, message);
    }

    private void logWarn(StringBuilder builder, boolean includeLevel, String message) {
        if (includeLevel) builder.append("WARN  ");
        builder.append(message).append('\n');
    }

    private void logError(StringBuilder builder, String message) {
        logError(builder, false, message);
    }

    private void logError(StringBuilder builder, boolean includeLevel, String message) {
        if (includeLevel) builder.append("ERROR ");
        builder.append(message).append('\n');
    }

    /**
     * Returns a reference into the reports directory.
     *
     * @param file the file
     * @return the reference
     */
    public String getWorkspaceFileReference(File file) {
        return file.getName();
    }

    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss");

    static class CleanupThread extends Thread {

        public CleanupThread() {
            setName("Selenium Session Cleanup");
        }

        @Override
        public void run() {
            LOGGER.info("Cleanup lost Selenium sessions");
            for (Session session : sessions) {
                if (!session.isClosed()) {
                    LOGGER.info(" - " + session.getName() + " (" + session.getId() + ") - " + session.getDescription());
                    session.close();
                }
            }
        }
    }
}
