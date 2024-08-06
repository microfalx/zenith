package net.microfalx.zenith.hub;

import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("zenith.hub")
@Getter
@ToString
public class HubProperties {

    private int port = 49100;
    private Duration timeout = Duration.ofSeconds(120);
    private Duration browserTimeout = Duration.ofSeconds(120);
    private int maxThreads = 50;
}
