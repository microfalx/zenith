package net.microfalx.zenith.api.node;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.FileUtils;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Identifies a process which runs a test (browser).
 */
@Getter
@ToString
public class Runner extends NamedIdentityAware<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 3267147983921361905L;

    private final int pid;
    private final String path;
    private String directory;
    private final Node node;
    private int parentPid;
    private long age;
    private float cpu;
    private long virtualMemory;
    private long residentMemory;
    private Collection<String> arguments;

    private Runner(int pid, String path, Node node) {
        this.pid = pid;
        this.path = path;
        this.node = node;
    }

    public Collection<String> getArguments() {
        return Collections.unmodifiableCollection(arguments);
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private final int pid;

        private String path;
        private String directory;

        private final Node node;

        private int parentPid;
        private long age;
        private float cpu;
        private long virtualMemory;
        private long residentMemory;
        private Collection<String> arguments = Collections.emptyList();

        public Builder(int pid, String path, Node node) {
            requireNonNull(path);
            requireNonNull(node);
            this.pid = pid;
            Hashing hashing = Hashing.create();
            hashing.update(pid);
            hashing.update(node.getId());
            this.id(hashing.asString());
            this.path = path;
            this.name(FileUtils.getFileName(path));
            this.node = node;
        }

        public Builder path(String path) {
            requireNonNull(path);
            this.path = path;
            this.name(FileUtils.getFileName(path));
            return this;
        }

        public Builder directory(String directory) {
            requireNonNull(directory);
            this.directory = directory;
            return this;
        }

        public Builder cpu(float cpu) {
            this.cpu = cpu;
            return this;
        }

        public Builder age(long age) {
            this.age = age;
            return this;
        }

        public Builder parentPid(int parentPid) {
            this.parentPid = parentPid;
            return this;
        }

        public Builder memory(long virtualMemory, long residentMemory) {
            this.virtualMemory = virtualMemory;
            this.residentMemory = residentMemory;
            return this;
        }

        public Builder arguments(Collection<String> arguments) {
            requireNonNull(arguments);
            this.arguments = new ArrayList<>(arguments);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Runner(pid, path, node);
        }

        public Runner build() {
            Runner runner = (Runner) super.build();
            runner.directory = directory;
            runner.parentPid = parentPid;
            runner.age = age;
            runner.cpu = cpu;
            runner.virtualMemory = virtualMemory;
            runner.residentMemory = residentMemory;
            runner.arguments = arguments;
            return runner;
        }
    }
}
