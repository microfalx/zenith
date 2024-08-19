package net.microfalx.zenith.node;

import net.microfalx.zenith.api.node.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class NodeHealthCheck implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeHealthCheck.class);

    private final NodeService nodeService;

    NodeHealthCheck(NodeService nodeService) {
        requireNonNull(nodeService);
        this.nodeService = nodeService;
    }

    @Override
    public void run() {
        if (!nodeService.isReady()) {
            LOGGER.debug("Hub is not ready, cancel health check validation");
        } else {

        }
    }
}
