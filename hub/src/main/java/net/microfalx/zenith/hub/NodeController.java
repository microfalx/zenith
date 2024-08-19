package net.microfalx.zenith.hub;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.zenith.base.jpa.Node;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/node")
@DataSet(model = Node.class)
@Help("/node")
public class NodeController extends DataSetController<Node, Integer> {
}
