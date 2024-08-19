package net.microfalx.zenith.base.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedIdentityAware;
import net.microfalx.lang.annotation.*;
import net.microfalx.zenith.api.common.Browser;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "zenith_session")
@Getter
@Setter
@Name("Sessions")
@ReadOnly
@ToString
public class Session extends NamedIdentityAware<Long> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Position(20)
    @Description("The project which owns the session")
    private Project project;

    @Column(name = "browser", nullable = false)
    @NotBlank
    @Position(30)
    @Description("The browser type")
    @Width("100px")
    private Browser browser;

    @Column(name = "category", nullable = false)
    @NotBlank
    @Position(30)
    @Description("A category associated with a session")
    @Width("100px")
    private String category;

    @Column(name = "tags", nullable = false)
    @NotBlank
    @Position(30)
    @Description("A set of tags associated with the session")
    @Width("100px")
    private String tags;
}
