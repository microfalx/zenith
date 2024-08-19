package net.microfalx.zenith.base.grid;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.grid.commands.EventBusCommand;
import org.openqa.selenium.grid.commands.Hub;
import org.openqa.selenium.grid.node.httpd.NodeServer;
import org.openqa.selenium.grid.router.httpd.RouterServer;
import org.openqa.selenium.grid.sessionmap.httpd.SessionMapServer;

class ComponentTest {

    @Test
    void eventBus() {
        Component<EventBusCommand> component = Component.create(EventBusCommand.class);
        Assertions.assertThat(component.describeConfig()).contains("events");
    }

    @Test
    void router() {
        Component<RouterServer> component = Component.create(RouterServer.class);
        Assertions.assertThat(component.describeConfig()).contains("distributor");
    }

    @Test
    void sessionMap() {
        Component<SessionMapServer> component = Component.create(SessionMapServer.class);
        Assertions.assertThat(component.describeConfig()).contains("sessions");
    }

    @Test
    void node() {
        Component<NodeServer> component = Component.create(NodeServer.class);
        Assertions.assertThat(component.describeConfig()).contains("node");
    }

    @Test
    void hub() {
        Component<Hub> component = Component.create(Hub.class);
        Assertions.assertThat(component.describeConfig()).contains("distributor").contains("sessions").contains("server");
    }

    @Test
    void startHub() {
        Component<Hub> component = Component.create(Hub.class);
        component.start();
    }

}