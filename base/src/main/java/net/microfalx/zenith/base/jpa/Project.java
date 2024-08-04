package net.microfalx.zenith.base.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;
import net.microfalx.lang.annotation.Width;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "zenith_project")
@Getter
@Setter
@ToString
public class Project extends NamedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "active", nullable = false)
    @Position(21)
    @Description("Indicate whether the Selenium node is active")
    @Width("80px")
    private boolean active;
}
