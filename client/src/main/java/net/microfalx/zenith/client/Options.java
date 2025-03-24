package net.microfalx.zenith.client;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;

import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Options used to create {@link Session}.
 */
@Getter
@ToString
public class Options implements Cloneable {

    public static URI DEFAULT_URI = URI.create("http://localhost:4444/wd/hub");

    private Browser browser = Browser.CHROME;
    private boolean local = true;
    private boolean debug;
    private RecordingMode recordingMode = RecordingMode.SKIP;
    private boolean fullScreen;
    private boolean headless = true;
    private boolean profiler;
    private boolean container;
    private URI uri;

    public static Options create() {
        return new Options();
    }

    private Options() {
    }

    public boolean isLocal() {
        if (uri != null) {
            return false;
        } else {
            return debug || local;
        }
    }

    public boolean shouldRecord() {
        return recordingMode != RecordingMode.SKIP;
    }

    public URI getUri() {
        return uri != null ? uri : DEFAULT_URI;
    }

    public Options withBrowser(Browser browser) {
        requireNonNull(browser);
        Options copy = copy();
        copy.browser = browser;
        return copy;
    }

    public Options withLocal(boolean local) {
        Options copy = copy();
        copy.local = local;
        return copy;
    }

    public Options withRecordingMode(RecordingMode recordingMode) {
        requireNonNull(recordingMode);
        Options copy = copy();
        copy.recordingMode = recordingMode;
        if (copy.shouldRecord()) copy.headless = false;
        return copy;
    }

    public Options withDebug(boolean debug) {
        Options copy = copy();
        copy.debug = debug;
        return copy;
    }

    public Options withProfiler(boolean profiler) {
        Options copy = copy();
        copy.profiler = profiler;
        return copy;
    }

    public Options withFullScreen(boolean fullScreen) {
        Options copy = copy();
        copy.fullScreen = fullScreen;
        return copy;
    }

    public Options withHeadless(boolean headless) {
        Options copy = copy();
        copy.headless = headless;
        return copy;
    }

    public Options withContainer(boolean container, String uri) {
        requireNonNull(uri);
        Options copy = copy();
        copy.container = container;
        if (container) copy.uri = URI.create(uri);
        return copy;
    }

    public Options withHubUri(String uri) {
        requireNonNull(uri);
        Options copy = copy();
        copy.uri = URI.create(uri);
        copy.local = false;
        return copy;
    }

    public Options withHubUri(URI uri) {
        requireNonNull(uri);
        Options copy = copy();
        copy.uri = uri;
        copy.local = false;
        return copy;
    }

    private Options copy() {
        try {
            return (Options) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    public String toDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(StringUtils.capitalize(browser.name()));
        addSeparator(builder);
        if (local) {
            builder.append("local");
        } else {
            builder.append(uri);
        }
        if (headless) addSeparator(builder).append("headless");
        return builder.toString();
    }

    private StringBuilder addSeparator(StringBuilder builder) {
        if (!builder.isEmpty()) builder.append(", ");
        return builder;
    }

    public enum Browser {
        CHROME,
        FIREFOX,
        IE
    }

    public enum RecordingMode {
        SKIP,
        RECORD_ALL,
        RECORD_FAILING
    }
}
