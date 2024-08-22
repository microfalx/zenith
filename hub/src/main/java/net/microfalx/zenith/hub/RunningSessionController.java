package net.microfalx.zenith.hub;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/session")
@DataSet(model = RunningSession.class)
@Help("/session")
public class RunningSessionController extends DataSetController<RunningSession, String> {
}
