package net.microfalx.zenith.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.zenith"})
public class ZenithApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZenithApplication.class, args);
    }
}
