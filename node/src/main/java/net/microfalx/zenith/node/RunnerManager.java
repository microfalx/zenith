package net.microfalx.zenith.node;

import net.microfalx.lang.TimeUtils;
import net.microfalx.zenith.api.node.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.*;

/**
 * A manager for processes executed to support Selenium sessions.
 */
class RunnerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerManager.class);

    private static final long DEFAULT_REFRESH = 60;

    private final NodeServiceImpl nodeService;
    private final TaskScheduler taskScheduler;
    private volatile Collection<Runner> runners = Collections.emptyList();

    private volatile long lastRefresh = System.currentTimeMillis();

    RunnerManager(NodeServiceImpl nodeService, TaskScheduler taskScheduler) {
        this.nodeService = nodeService;
        this.taskScheduler = taskScheduler;
    }

    public Collection<Runner> getRunners() {
        refreshWorkers();
        return Collections.unmodifiableCollection(runners);
    }

    void start() {
        taskScheduler.scheduleAtFixedRate(new ScannerWorker(), Duration.ofSeconds(DEFAULT_REFRESH));
        taskScheduler.scheduleAtFixedRate(new KillWorker(), Duration.ofSeconds(DEFAULT_REFRESH * 15));
    }

    private void scanProcesses() {
        lastRefresh = System.currentTimeMillis();
        Collection<Runner> currentRunners = new ArrayList<>();

        this.runners = currentRunners;
    }

    private void refreshWorkers() {
        if (TimeUtils.millisSince(lastRefresh) > 3 * DEFAULT_REFRESH) new ScannerWorker().run();
        taskScheduler.scheduleWithFixedDelay(new ScannerWorker(), Duration.ofSeconds(1));
    }

    private Runner scanProcess(Process systemInformation, long pid) {
        return null;
    }

    class KillWorker implements Runnable {

        @Override
        public void run() {

        }
    }

    class ScannerWorker implements Runnable {

        @Override
        public void run() {
            scanProcesses();
        }
    }

    private static final Set<String> PROCESS_NAMES = new HashSet<>();

    static {
        PROCESS_NAMES.add("chrome");
        PROCESS_NAMES.add("firefox");
    }
}
