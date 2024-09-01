package net.microfalx.zenith.base.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "zenith_session")
@Getter
@Setter
@Name("History")
@ReadOnly
@ToString
public class Session extends NamedIdentityAware<Long> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "browser_id")
    @Position(20)
    @Description("The browser supporting this session")
    private Browser browser;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Position(20)
    @Description("The project which owns the session")
    private Project project;

    @Column(name = "category", nullable = false)
    @NotBlank
    @Position(30)
    @Description("The category associated with the session")
    @Width("100px")
    private String category;

    @Column(name = "namespace", nullable = false)
    @NotBlank
    @Position(31)
    @Description("The namespace associated with the session")
    @Width("250px")
    private String namespace;

    @Column(name = "started_at", nullable = false)
    @NotNull
    @Position(40)
    @Description("Indicates the time when the session was started")
    @Width("120px")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    @Position(41)
    @Description("Indicates the time when the session was started")
    @Width("120px")
    private LocalDateTime endedAt;

    @Column(name = "duration")
    @Position(42)
    @Description("The duration of the session")
    @Width("120px")
    private Duration duration;

    @Column(name = "status")
    @NotNull
    @Position(50)
    @Description("The status of the session")
    @Width("120px")
    @Enumerated(EnumType.STRING)
    private net.microfalx.zenith.api.common.Session.Status status;

    @Column(name = "reason")
    @Position(50)
    @Description("The status of the session")
    @Width("120px")
    @Enumerated(EnumType.STRING)
    @Visible(value = false)
    private net.microfalx.zenith.api.common.Session.Reason reason;

    @Column(name = "tags", nullable = false)
    @NotBlank
    @Position(30)
    @Description("A set of tags associated with the session")
    @Width("100px")
    private String tags;

    @Column(name = "reason_message")
    @Visible(value = false)
    private String reasonMessage;
}
