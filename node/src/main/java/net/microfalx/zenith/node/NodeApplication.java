package net.microfalx.zenith.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.zenith"})
@EnableJpaRepositories({"net.microfalx.bootstrap", "net.microfalx.zenith"})
@EntityScan({"net.microfalx.bootstrap", "net.microfalx.zenith"})
@EnableTransactionManagement
public class NodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);
    }
}
