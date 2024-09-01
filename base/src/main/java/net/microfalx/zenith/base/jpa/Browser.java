package net.microfalx.zenith.base.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "zenith_browser")
@Getter
@Setter
@Name("Browsers")
@ReadOnly
@ToString
public class Browser extends NamedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "version", nullable = false)
    @NotEmpty
    @Position(21)
    @Description("Indicate the version of the browser")
    @Width("80px")
    @Name
    private String version;
}
