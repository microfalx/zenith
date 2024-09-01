package net.microfalx.zenith.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.annotation.*;
import net.microfalx.zenith.api.common.Browser;
import net.microfalx.zenith.api.common.Session;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.microfalx.lang.CollectionUtils.setToString;

@Getter
@Setter
@ToString
@Name("Sessions")
@ReadOnly
public class RunningSession extends NamedIdentityAware<String> {

    @Position(20)
    @Description("The project running the session")
    @Width("120px")
    private String project;

    @Position(21)
    @Description("The category associated with the session")
    @Width("10px")
    private String category;

    @Position(22)
    @Description("The namespace associated with the session")
    @Width("250px")
    private String namespace;

    @Position(30)
    @Description("The browser running the session")
    @Width("120px")
    private Browser browser;

    @Position(31)
    @Description("The version of the browser running the session")
    @Width("90px")
    private String browserVersion;

    @Position(40)
    @Description("Indicates the time when the session was started")
    @Width("120px")
    private LocalDateTime startedAt;

    @Position(41)
    @Description("Indicates for how long the session was running")
    @Width("120px")
    private Duration duration;

    @Position(50)
    @Description("Indicates for how long the session was running")
    @Width("120px")
    private String tags;

    static RunningSession from(Session session) {
        RunningSession runningSession = new RunningSession();
        runningSession.setId(session.getId());
        runningSession.setName(session.getName());
        runningSession.setDescription(session.getDescription());
        runningSession.setProject(session.getProject());
        runningSession.setNamespace(session.getNamespace());
        runningSession.setCategory(session.getCategory());
        runningSession.setBrowser(session.getBrowser());
        runningSession.setBrowserVersion(session.getBrowserVersion());
        runningSession.setStartedAt(session.getStartedAt());
        runningSession.setDuration(session.getDuration());
        runningSession.setTags(setToString(session.getTags()));
        return runningSession;
    }
}
