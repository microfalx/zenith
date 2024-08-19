package net.microfalx.zenith.hub;

import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.lang.TimeUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.zenith.api.common.Log;
import net.microfalx.zenith.api.common.Screenshot;
import net.microfalx.zenith.api.common.Server;
import net.microfalx.zenith.api.common.Session;
import net.microfalx.zenith.api.hub.Hub;
import net.microfalx.zenith.api.hub.HubException;
import net.microfalx.zenith.api.hub.HubService;
import net.microfalx.zenith.api.node.Node;
import net.microfalx.zenith.api.node.Slot;
import net.microfalx.zenith.base.ZenithUtils;
import net.microfalx.zenith.base.jpa.HubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.TimeUtils.TEN_SECONDS;
import static net.microfalx.lang.TimeUtils.millisSince;

@Service
public class HubServiceImpl implements HubService, InitializingBean, ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubService.class);

    static Metrics GRID_METRICS = ZenithUtils.ZENITH_METRICS.withGroup("Grid");

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private HubProperties properties = new HubProperties();

    @Autowired
    private TaskScheduler taskScheduler;

    private Hub hub;
    private volatile boolean ready;
    private volatile long lastReadyUpdate = TimeUtils.ONE_DAY;

    @Override
    public Hub getHub() {
        if (hub == null) {
            throw new HubException("A hub is not not available");
        }
        return hub;
    }

    @Override
    public URI getUri() {
        return HubFactory.getInstance().getUri();
    }

    @Override
    public URI getWsUri() {
        return HubFactory.getInstance().getWsUri();
    }

    @Override
    public boolean isReady() {
        if (millisSince(lastReadyUpdate) > TEN_SECONDS) {
            lastReadyUpdate = currentTimeMillis();
            this.ready = new HubStatus(hub).execute();
        }
        GRID_METRICS.count(ready ? "Ready" : "Not Ready");
        return ready;
    }

    @Override
    public Collection<Session> getSessions() {
        return Collections.emptyList();
    }

    @Override
    public Session getSession(String id) {
        return null;
    }

    @Override
    public Collection<Slot> getSlots() {
        return null;
    }

    @Override
    public Node getNode(String id) {
        return null;
    }

    @Override
    public Log getLog(String id, Log.Type type) {
        return null;
    }

    @Override
    public Screenshot getScreenshot(String id) {
        return null;
    }

    @Override
    public boolean isRunning(String id) {
        return false;
    }

    @Override
    public Collection<Node> getNodes() {
        return Collections.emptyList();
    }

    @Override
    public void refresh() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        initializeHub();
        registerHub();
        registerTasks();
    }

    private void registerHub() {
        hub = Hub.create(Server.get(), properties.getPort());
        NaturalIdEntityUpdater<net.microfalx.zenith.base.jpa.Hub, Integer> updater = new NaturalIdEntityUpdater<>(metadataService, hubRepository);
        updater.findByNaturalIdAndUpdate(net.microfalx.zenith.base.jpa.Hub.from(hub));
    }

    private void initializeHub() {
        HubFactory factory = HubFactory.getInstance();
        factory.setProperties(properties);
        factory.start();
    }

    private void registerTasks() {
        taskScheduler.scheduleAtFixedRate(new HubHealthCheck(this), properties.getValidationInterval());
    }
}
