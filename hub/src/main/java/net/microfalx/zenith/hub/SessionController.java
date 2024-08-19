package net.microfalx.zenith.hub;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.zenith.base.jpa.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/session")
@DataSet(model = Session.class)
@Help("/session")
public class SessionController extends DataSetController<Session, Integer> {
}
