package net.microfalx.zenith.base.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "zenith_hub")
@Getter
@Setter
@ToString
public class Hub extends NamedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "hostname", nullable = false)
    @NotBlank
    @Position(20)
    @Description("The hostname where the Selenium node runs")
    @Width("200px")
    private String hostname;

    @Column(name = "port", nullable = false)
    @Position(21)
    @Description("The port where the Selenium node is reachable")
    @Width("80px")
    private int port;

    @Column(name = "pinged_at")
    @Position(502)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was last time modified")
    @LastModifiedDate
    @ModifiedAt
    private LocalDateTime pingedAt;

    public static Hub from(net.microfalx.zenith.api.hub.Hub hub) {
        Hub jpaHub = new Hub();
        jpaHub.setNaturalId(hub.getId());
        jpaHub.setName(hub.getName());
        jpaHub.setHostname(hub.getServer().getHostname());
        jpaHub.setPort(hub.getPort());
        jpaHub.setPingedAt(LocalDateTime.now());
        return jpaHub;
    }
}
