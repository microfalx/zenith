package net.microfalx.zenith.base.grid;

import com.google.auto.service.AutoService;
import org.openqa.selenium.cli.CliCommand;
import org.openqa.selenium.grid.node.httpd.NodeServer;

@AutoService(CliCommand.class)
public class Node extends NodeServer {

    public Node() {
    }
}
