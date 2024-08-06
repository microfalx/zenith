package net.microfalx.zenith.hub;

import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.zenith.api.common.Log;
import net.microfalx.zenith.api.common.Screenshot;
import net.microfalx.zenith.api.common.Server;
import net.microfalx.zenith.api.common.Session;
import net.microfalx.zenith.api.hub.Hub;
import net.microfalx.zenith.api.hub.HubService;
import net.microfalx.zenith.api.node.Node;
import net.microfalx.zenith.api.node.Slot;
import net.microfalx.zenith.base.jpa.HubRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;

@Service
public class HubServiceImpl implements HubService, InitializingBean, ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private HubProperties properties = new HubProperties();

    private Hub hub;

    @Override
    public Hub getHub() {
        return null;
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public Collection<Session> getSessions() {
        return null;
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
        return null;
    }

    @Override
    public void register(Node node) {

    }

    @Override
    public void unregister(Node node) {

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
}
