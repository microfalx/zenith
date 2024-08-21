package net.microfalx.zenith.hub;

import net.microfalx.lang.ThreadUtils;
import net.microfalx.zenith.api.hub.HubService;
import net.microfalx.zenith.client.Options;
import net.microfalx.zenith.client.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

class HubHealthCheck implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubHealthCheck.class);

    private final HubService hubService;
    private Session session;

    public HubHealthCheck(HubService hubService) {
        this.hubService = hubService;
    }

    @Override
    public void run() {
        if (!hubService.isReady()) {
            LOGGER.debug("Hub is not ready, cancel health check validation");
        } else {
            try {
                createSession();
                runRequests();
            } finally {
                closeSession();
            }
        }
    }

    private void createSession() {
        Options options = Options.create().withHubUri(hubService.getWsUri());
        session = Session.create(options);
        session.setProject("Zenith").setCategory("Health Check").setNamespace("net.microfalx.zenith.hub");
        session.addTag("zenith").addTag("validation");
    }

    private void runRequests() {
        session.open(URLS[ThreadLocalRandom.current().nextInt(URLS.length)]);
        session.takeScreenShot();
        ThreadUtils.sleepSeconds(20);
    }

    private void closeSession() {
        if (session == null) return;
        session.close();
    }

    private static final String[] URLS = {
            "https://google.com", "https://x.com", "https://www.facebook.com", "https://www.linkedin.com"
    };
}
