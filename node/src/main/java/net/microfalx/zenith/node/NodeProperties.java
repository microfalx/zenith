package net.microfalx.zenith.node;

import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("zenith.node")
@Getter
@ToString
public class NodeProperties {

    private int port = 5555;
    private int maxSessions = 1;
    private Duration timeout = Duration.ofSeconds(120);
    private Duration browserTimeout = Duration.ofSeconds(120);
    private int maxThreads = 10;
    private Duration validationInterval = Duration.ofSeconds(30);
}
