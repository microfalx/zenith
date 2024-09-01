package net.microfalx.zenith.node;

import com.google.common.cache.Cache;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.lang.*;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.Resource;
import net.microfalx.zenith.api.common.Log;
import net.microfalx.zenith.api.common.Screenshot;
import net.microfalx.zenith.api.common.Server;
import net.microfalx.zenith.api.common.Session;
import net.microfalx.zenith.api.hub.Hub;
import net.microfalx.zenith.api.node.Node;
import net.microfalx.zenith.api.node.NodeException;
import net.microfalx.zenith.api.node.NodeService;
import net.microfalx.zenith.api.node.Runner;
import net.microfalx.zenith.base.ZenithUtils;
import net.microfalx.zenith.base.grid.NodeStatus;
import net.microfalx.zenith.base.jpa.HubRepository;
import net.microfalx.zenith.base.jpa.NodeRepository;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.grid.node.ActiveSession;
import org.openqa.selenium.grid.node.local.SessionSlot;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.TimeUtils.TEN_SECONDS;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * The implementation of the {@link NodeService}.
 */
@Service
public class NodeServiceImpl implements NodeService, InitializingBean, ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeService.class);

    private static final long CLOSED_SESSIONS_RETENTION = TimeUtils.FIFTEEN_MINUTE;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static Metrics NODE_METRICS = ZenithUtils.ZENITH_METRICS.withGroup("Node");

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private NodeProperties properties = new NodeProperties();

    private volatile Node node;
    private volatile boolean ready;
    private volatile long lastReadyUpdate = TimeUtils.ONE_DAY;
    private volatile Hub hub;
    private volatile NodeFactory factory;
    private RunnerManager runnerManager;
    private final Map<String, SessionHolder> sessions = new ConcurrentHashMap<>();
    private Cache<SessionId, SessionSlot> nodeSessions;

    private TaskExecutor taskExecutor;

    private volatile long lastHubRefresh = TimeUtils.oneHourAgo();

    @Override
    public Node getNode() {
        if (node == null) {
            throw new NodeException("A node is not available");
        }
        return node;
    }

    @Override
    public URI getUri() {
        return getNode().getUri();
    }

    @Override
    public boolean isReady() {
        if (millisSince(lastReadyUpdate) > TEN_SECONDS) {
            lastReadyUpdate = currentTimeMillis();
            this.ready = new NodeStatus(node).execute();
        }
        NODE_METRICS.count(ready ? "Ready" : "Not Ready");
        return ready;
    }

    @Override
    public Collection<Session> getSessions() {
        return sessions.values().stream().map(SessionHolder::getSession).toList();
    }

    @Override
    public Log getLog(String id, Log.Type type) {
        requireNonNull(id);
        requireNonNull(type);
        SessionHolder holder = sessions.get(id);
        return holder != null ? holder.getLog(type) : Log.create(type);
    }

    @Override
    public Screenshot getScreenshot(String id) {
        requireNonNull(id);
        SessionHolder holder = sessions.get(id);
        return holder != null ? holder.getScreenshot() : Screenshot.create();
    }

    @Override
    public boolean isRunning(String id) {
        requireNonNull(id);
        SessionHolder holder = sessions.get(id);
        return holder != null && !holder.isClosed();
    }

    @Override
    public Collection<Runner> getRunners() {
        return runnerManager.getRunners();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeNode();
        initializeExecutor();
        initializeMisc();
        setupNode();
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        taskScheduler.scheduleAtFixedRate(new RegisterWorker(), Duration.ofSeconds(60));
        taskScheduler.scheduleAtFixedRate(new MaintenanceWorker(), Duration.ofSeconds(60));

        runnerManager = new RunnerManager(this, taskScheduler);
        runnerManager.start();

        setupNode();
        registerTasks();
    }

    private void initializeNode() {
        node = Node.create(Server.get(), properties.getPort());
        updateNodeInDatabase();
    }

    private void initializeExecutor() {
        taskExecutor = TaskExecutorFactory.create("hub").createExecutor();
    }

    private void initializeMisc() {
    }

    private void setupNode() {
        NodeUtilities.setupJavaLogger();
    }

    private void registerTasks() {
        taskScheduler.scheduleAtFixedRate(new NodeHealthCheck(this), properties.getValidationInterval());
    }

    private synchronized void registerWithHub() {
        Hub hub = getHub();
        if (hub == null) {
            LOGGER.info("A Selenium Hub is not available");
            return;
        }
        if (!hub.equals(this.hub)) destroyNode();
        if (factory == null) {
            LOGGER.info("Change Hub connectivity, host " + hub.getServer().getHostname() + ", port " + hub.getPort());
            factory = NodeFactory.getInstance();
            factory.setHubUri(hub.getWsUri());
            factory.startup();
            LOGGER.info("Selenium Node created");
            registerSeleniumListener();
        }
        setupNode();
    }

    private void destroyNode() {
        LOGGER.info("Hub changed, destroy node");
        NodeFactory.shutdown();
        factory = null;
    }

    private void registerSeleniumListener() {
        /*org.openqa.selenium.grid.node.Node node = factory.getNode();
        this.nodeSessions = Reflect.on(node).get("currentSessions");
        taskScheduler.scheduleAtFixedRate(new DiscoverSessionWorker(), Duration.ofSeconds(1));*/
    }

    private void closeSession(SessionHolder session) {
        session.close();
        taskExecutor.execute(new PersistWorker(session));
    }

    private Hub getHub() {
        if (hub != null && millisSince(lastHubRefresh) > TimeUtils.ONE_MINUTE) return hub;
        Iterator<net.microfalx.zenith.base.jpa.Hub> hubIterator = hubRepository.findAll().iterator();
        if (!hubIterator.hasNext()) {
            this.hub = null;
        } else {
            net.microfalx.zenith.base.jpa.Hub hubJpa = hubIterator.next();
            this.hub = Hub.create(Server.get(hubJpa.getHostname()), hubJpa.getPort());
        }
        lastHubRefresh = currentTimeMillis();
        return hub;
    }

    private void updateNodeInDatabase() {
        NaturalIdEntityUpdater<net.microfalx.zenith.base.jpa.Node, Integer> updater = new NaturalIdEntityUpdater<>(metadataService, nodeRepository);
        net.microfalx.zenith.base.jpa.Node nodeJpa = new net.microfalx.zenith.base.jpa.Node();
        nodeJpa.setNaturalId(node.getId());
        nodeJpa.setName(node.getName());
        nodeJpa.setHostname(node.getServer().getHostname());
        nodeJpa.setPort(node.getPort());
        nodeJpa.setActive(true);
        nodeJpa.setPingedAt(LocalDateTime.now());
        updater.findByNaturalIdAndUpdate(nodeJpa);
    }

    private class RegisterWorker implements Runnable {

        @Override
        public void run() {
            registerWithHub();
        }
    }

    private class DiscoverSessionWorker implements Runnable {

        @Override
        public void run() {
            Set<String> existingSessions = new HashSet<>();
            for (SessionSlot slot : nodeSessions.asMap().values()) {
                if (!slot.isAvailable()) continue;
                ActiveSession session = null;
                try {
                    session = slot.getSession();
                } catch (NoSuchSessionException e) {
                    continue;
                }
                ActiveSession finalSession = session;
                existingSessions.add(session.getId().toString());
                sessions.computeIfAbsent(session.getId().toString(), s -> new SessionHolder(finalSession, null));
            }
            for (Map.Entry<String, SessionHolder> session : NodeServiceImpl.this.sessions.entrySet()) {
                if (!existingSessions.contains(session.getKey())) {
                    closeSession(session.getValue());
                }
            }
        }

    }

    private class MaintenanceWorker implements Runnable {

        private void removeOldSessions() {
            for (SessionHolder sessionHolder : sessions.values()) {
                if (sessionHolder.isExpired()) {
                    sessions.remove(sessionHolder.getId());
                }
            }
        }

        @Override
        public void run() {
            removeOldSessions();
            updateNodeInDatabase();
        }
    }

    static class PersistWorker implements Runnable {

        private SessionHolder holder;

        public PersistWorker(SessionHolder holder) {
            this.holder = holder;
        }

        private Resource getDirectory() {
            return NodeUtilities.getSessionDirectory().resolve(DAY_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY)
                    .resolve(holder.id, Resource.Type.DIRECTORY);
        }

        private void appendText(String fileName, String text) {
            if (StringUtils.isNotEmpty(text)) {
                Resource directory = getDirectory();
                try {
                    IOUtils.appendStream(new OutputStreamWriter(directory.resolve(fileName).getOutputStream(), StandardCharsets.UTF_8), new StringReader(text));
                } catch (IOException e) {
                    LOGGER.error("Failed to store text file " + fileName + " for session " + holder.id, e);
                }
            }
        }

        private void appendResource(Resource resource) {
            Resource directory = getDirectory();
            try {
                directory.resolve(resource.getName()).copyFrom(resource);
            } catch (Exception e) {
                LOGGER.error("Failed to store text file " + resource.getName() + " for session " + holder.id, e);
            }
        }

        @Override
        public void run() {
            appendText("browser.log", holder.seleniumSession.getBrowserLogger().toString());
            appendText("driver.log", holder.seleniumSession.getDriverLogger().toString());
            appendText("client.log", holder.seleniumSession.getClientLogger().toString());
            appendText("server.log", holder.seleniumSession.getServerLogger().toString());
            for (Resource screenshot : holder.seleniumSession.getScreenshots()) {
                appendResource(screenshot);
            }
        }
    }

    static class SessionHolder implements Identifiable<String> {

        private final String id;
        private final AtomicInteger screenshotIndex = new AtomicInteger(1);
        private final ActiveSession nodeSession;
        private final net.microfalx.zenith.client.Session seleniumSession;
        private volatile long lastUsed = currentTimeMillis();
        private volatile Resource lastScreenshot;
        private volatile long lastScreenshotUpdated = TimeUtils.oneHourAgo();
        private volatile long closedTime;
        private volatile boolean closed;

        SessionHolder(ActiveSession nodeSession, net.microfalx.zenith.client.Session seleniumSession) {
            this.id = nodeSession.getId().toString();
            this.nodeSession = nodeSession;
            this.seleniumSession = seleniumSession;
        }

        @Override
        public String getId() {
            return id;
        }

        boolean isClosed() {
            return closed;
        }

        void close() {
            closed = true;
            closedTime = currentTimeMillis();
            takeScreenshot();
            getLog(Log.Type.ALL);
            // give some time to flush the screen buffers and loggers and capture
            ThreadUtils.sleep(Duration.ofSeconds(1));
            takeScreenshot();
            getLog(Log.Type.ALL);
        }

        boolean isExpired() {
            return closed && millisSince(closedTime) > CLOSED_SESSIONS_RETENTION;
        }

        Log getLog(Log.Type type) {
            touch();
            switch (type) {
                case ALL:
                    StringBuilder builder = new StringBuilder();
                    StringBuilder browserLogger = seleniumSession.getBrowserLogger();
                    StringBuilder seleniumLogger = seleniumSession.getSeleniumLogger();
                    if (closed) {
                        builder.append("Session closed ").append(FormatterUtils.formatDateTime(closedTime)).append(", ");
                    }
                    /*builder.append("Browser: errors=").append(browserLogger.getErrorCount())
                            .append(", warnings=").append(browserLogger.getWarningCount()).append(", infos=")
                            .append(browserLogger.getInfoCount());
                    builder.append("; Selenium: errors=").append(seleniumLogger.getErrorCount())
                            .append(", warnings=").append(seleniumLogger.getWarningCount()).append(", infos=")
                            .append(seleniumLogger.getInfoCount());
                    builder.append("\n").append(seleniumSession.getLogs(false));*/
                    return Log.create(type, builder.toString());
                case SELENIUM:
                    return Log.create(type, seleniumSession.getSeleniumLogger().toString());
                case SELENIUM_CLIENT:
                    return Log.create(type, seleniumSession.getClientLogger().toString());
                case SELENIUM_DRIVER:
                    return Log.create(type, seleniumSession.getDriverLogger().toString());
                case SELENIUM_SERVER:
                    return Log.create(type, seleniumSession.getServerLogger().toString());
                case BROWSER:
                    return Log.create(type, seleniumSession.getBrowserLogger().toString());
                default:
                    return Log.create(type);
            }
        }

        Screenshot getScreenshot() {
            touch();
            if (lastScreenshot == null || millisSince(lastScreenshotUpdated) > NodeUtilities.INTERVAL_BETWEEN_SCREENSHOTS && !closed) {
                takeScreenshot();
            }
            byte[] data = new byte[0];
            try {
                data = lastScreenshot.loadAsBytes();
            } catch (IOException e) {
                LOGGER.error("Failed to extract screenshot from " + lastScreenshot, e);
            }
            return Screenshot.create(UUID.randomUUID().toString(), data);
        }

        void takeScreenshot() {
            touch();
            String name = org.apache.commons.lang3.StringUtils.leftPad(Integer.toString(screenshotIndex.getAndIncrement()), 3, '0');
            Resource resource = seleniumSession.takeScreenShot(name);
            if (resource != null) {
                lastScreenshot = resource;
                lastScreenshotUpdated = currentTimeMillis();
            }
        }

        Session getSession() {
            touch();
            Session.Builder builder = Session.builder(id);
            nodeSession.getCapabilities().asMap().forEach(builder::capability);
            return builder.build();
        }

        private void touch() {
            lastUsed = currentTimeMillis();
        }
    }
}
