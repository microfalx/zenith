package net.microfalx.zenith.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;
import net.microfalx.lang.annotation.Width;
import net.microfalx.zenith.api.common.Browser;
import net.microfalx.zenith.api.common.Session;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@ReadOnly
public class RunningSession extends NamedIdentityAware<String> {

    @Position(15)
    @Description("The browser running the session")
    @Width("120px")
    private Browser browser;

    @Position(20)
    @Description("Indicates the time when the session was started")
    @Width("120px")
    private LocalDateTime startedAt;

    @Position(21)
    @Description("Indicates for how long the session was running")
    @Width("120px")
    private Duration duration;

    @Position(30)
    @Description("Indicates for how long the session was running")
    @Width("120px")
    private String tags;

    static RunningSession from(Session session) {
        RunningSession runningSession = new RunningSession();
        runningSession.setId(session.getId());
        runningSession.setName(session.getName());
        runningSession.setDescription(session.getDescription());
        runningSession.setBrowser(session.getBrowser());
        runningSession.setStartedAt(session.getStartedAt());
        runningSession.setDuration(session.getDuration());
        runningSession.setTags(CollectionUtils.setToString(session.getTags()));
        return runningSession;
    }
}
