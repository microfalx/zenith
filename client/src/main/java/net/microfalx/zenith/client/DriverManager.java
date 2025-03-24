package net.microfalx.zenith.client;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A Selenium Driver manager supported by {@link WebDriverManager}.
 */
public class DriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverManager.class);

    private static volatile DriverManager instance;

    public static DriverManager getInstance() {
        if (instance == null) {
            synchronized (DriverManager.class) {
                if (instance == null) instance = new DriverManager();
            }
        }
        return instance;
    }

    private DriverManager() {
    }

    /**
     * Registers a driver with the right version for a given browser.
     *
     * @param browser the browser
     */
    public void registerDriver(Options.Browser browser) {
        requireNonNull(browser);
        switch (browser) {
            case CHROME:
                registerDriver(DriverManagerType.CHROME);
                break;
            case FIREFOX:
                registerDriver(DriverManagerType.FIREFOX);
                break;
            case IE:
                registerDriver(DriverManagerType.IEXPLORER);
                break;
        }
    }

    /**
     * Registers a driver for a given browser.
     *
     * @param type the driver type (browser)
     */
    private void registerDriver(DriverManagerType type) {
        WebDriverManager driverManager = WebDriverManager.getInstance(type);
        driverManager.avoidTmpFolder();
        driverManager.setup();
        LOGGER.info("Driver " + type.getBrowserName() + " registered, version " + driverManager.getDownloadedDriverVersion());
    }
}
