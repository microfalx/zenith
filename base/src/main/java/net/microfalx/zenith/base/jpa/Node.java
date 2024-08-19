package net.microfalx.zenith.base.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "zenith_node")
@Getter
@Setter
@Name("Nodes")
@ReadOnly
@ToString
public class Node extends NamedAndTimestampedIdentityAware<Integer> {

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
    @Formattable(prettyPrint = false)
    @Description("The port where the Selenium node is reachable")
    @Width("80px")
    private int port;

    @Column(name = "active", nullable = false)
    @Position(30)
    @Description("Indicate whether the Selenium node is active")
    @Width("80px")
    private boolean active;

    @Column(name = "pinged_at")
    @Position(502)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was last time modified")
    @LastModifiedDate
    @ModifiedAt
    private LocalDateTime pingedAt;
}
