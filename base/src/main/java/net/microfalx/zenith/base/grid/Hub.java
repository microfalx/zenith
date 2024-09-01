package net.microfalx.zenith.base.grid;

import com.google.auto.service.AutoService;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.cli.CliCommand;
import org.openqa.selenium.events.EventBus;
import org.openqa.selenium.grid.config.Config;
import org.openqa.selenium.grid.distributor.config.DistributorOptions;
import org.openqa.selenium.grid.distributor.local.LocalDistributor;
import org.openqa.selenium.grid.graphql.GraphqlHandler;
import org.openqa.selenium.grid.log.LoggingOptions;
import org.openqa.selenium.grid.router.ProxyWebsocketsIntoGrid;
import org.openqa.selenium.grid.router.Router;
import org.openqa.selenium.grid.router.httpd.RouterOptions;
import org.openqa.selenium.grid.security.BasicAuthenticationFilter;
import org.openqa.selenium.grid.security.Secret;
import org.openqa.selenium.grid.security.SecretOptions;
import org.openqa.selenium.grid.server.BaseServerOptions;
import org.openqa.selenium.grid.server.EventBusOptions;
import org.openqa.selenium.grid.server.NetworkOptions;
import org.openqa.selenium.grid.sessionqueue.NewSessionQueue;
import org.openqa.selenium.grid.sessionqueue.config.NewSessionQueueOptions;
import org.openqa.selenium.grid.sessionqueue.local.LocalNewSessionQueue;
import org.openqa.selenium.grid.web.CombinedHandler;
import org.openqa.selenium.grid.web.GridUiRoute;
import org.openqa.selenium.grid.web.RoutableHttpClientFactory;
import org.openqa.selenium.remote.http.*;
import org.openqa.selenium.remote.tracing.Tracer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static org.openqa.selenium.remote.http.Route.combine;

@AutoService(CliCommand.class)
public class Hub extends org.openqa.selenium.grid.commands.Hub {

    private static final Logger LOG = Logger.getLogger(Hub.class.getName());

    private SessionMap sessions;
    private LocalDistributor distributor;
    private SessionManager sessionManager;

    @Override
    protected Handlers createHandlers(Config config) {
        LoggingOptions loggingOptions = new LoggingOptions(config);
        Tracer tracer = loggingOptions.getTracer();

        EventBusOptions events = new EventBusOptions(config);
        EventBus bus = events.getEventBus();

        CombinedHandler handler = new CombinedHandler();

        sessions = new SessionMap(tracer, bus);
        handler.addHandler(sessions);

        BaseServerOptions serverOptions = new BaseServerOptions(config);
        SecretOptions secretOptions = new SecretOptions(config);
        Secret secret = secretOptions.getRegistrationSecret();

        URL externalUrl;
        try {
            externalUrl = serverOptions.getExternalUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

        NetworkOptions networkOptions = new NetworkOptions(config);
        HttpClient.Factory clientFactory =
                new RoutableHttpClientFactory(
                        externalUrl, handler, networkOptions.getHttpClientFactory(tracer));

        DistributorOptions distributorOptions = new DistributorOptions(config);
        NewSessionQueueOptions newSessionRequestOptions = new NewSessionQueueOptions(config);
        NewSessionQueue queue =
                new LocalNewSessionQueue(
                        tracer,
                        distributorOptions.getSlotMatcher(),
                        newSessionRequestOptions.getSessionRequestTimeoutPeriod(),
                        newSessionRequestOptions.getSessionRequestTimeout(),
                        secret,
                        newSessionRequestOptions.getBatchSize());
        handler.addHandler(queue);

        distributor =
                new LocalDistributor(
                        tracer,
                        bus,
                        clientFactory,
                        sessions,
                        queue,
                        distributorOptions.getSlotSelector(),
                        secret,
                        distributorOptions.getHealthCheckInterval(),
                        distributorOptions.shouldRejectUnsupportedCaps(),
                        newSessionRequestOptions.getSessionRequestRetryInterval(),
                        distributorOptions.getNewSessionThreadPoolSize(),
                        distributorOptions.getSlotMatcher());
        handler.addHandler(distributor);

        Router router = new Router(tracer, clientFactory, sessions, queue, distributor);
        GraphqlHandler graphqlHandler =
                new GraphqlHandler(
                        tracer, distributor, queue, serverOptions.getExternalUri(), getServerVersion());

        HttpHandler readinessCheck =
                req -> {
                    boolean ready = router.isReady() && bus.isReady();
                    return new HttpResponse()
                            .setStatus(ready ? HTTP_OK : HTTP_UNAVAILABLE)
                            .setContent(Contents.utf8String("Router is " + ready));
                };

        Routable routerWithSpecChecks = router.with(networkOptions.getSpecComplianceChecks());

        RouterOptions routerOptions = new RouterOptions(config);
        String subPath = routerOptions.subPath();

        Routable appendRoute =
                Stream.of(
                                baseRoute(subPath, combine(routerWithSpecChecks)),
                                hubRoute(subPath, combine(routerWithSpecChecks)),
                                graphqlRoute(subPath, () -> graphqlHandler))
                        .reduce(Route::combine)
                        .get();

        Routable httpHandler;
        if (routerOptions.disableUi()) {
            LOG.info("Grid UI has been disabled.");
            httpHandler = appendRoute;
        } else {
            Routable ui = new GridUiRoute(subPath);
            httpHandler = combine(ui, appendRoute);
        }

        UsernameAndPassword uap = secretOptions.getServerAuthentication();
        if (uap != null) {
            LOG.info("Requiring authentication to connect");
            httpHandler = httpHandler.with(new BasicAuthenticationFilter(uap.username(), uap.password()));
        }

        // Allow the liveness endpoint to be reached, since k8s doesn't make it easy to authenticate
        // these checks
        httpHandler = combine(httpHandler, Route.get("/readyz").to(() -> readinessCheck));

        return new Handlers(httpHandler, new ProxyWebsocketsIntoGrid(clientFactory, sessions));
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        if (sessions != null) {
            sessions.sessionManager = sessionManager;
            sessionManager.distributor = distributor;
        }
    }

    private String getServerVersion() {
        BuildInfo info = new BuildInfo();
        return String.format("%s (revision %s)", info.getReleaseLabel(), info.getBuildRevision());
    }
}
