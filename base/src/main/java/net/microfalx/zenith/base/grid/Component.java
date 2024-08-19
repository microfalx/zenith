package net.microfalx.zenith.base.grid;

import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.MemoryResource;
import org.openqa.selenium.cli.CliCommand;
import org.openqa.selenium.grid.TemplateGridCommand;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which encapsulate one of the Grid components.
 */
public class Component<T extends TemplateGridCommand> {

    private final Class<T> commandClass;
    private final List<String> arguments = new ArrayList<>();

    private PrintStream out;
    private PrintStream err;
    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;

    private T command;

    public static <T extends TemplateGridCommand> Component<T> create(Class<T> commandClass) {
        return new Component<>(commandClass);
    }

    private Component(Class<T> commandClass) {
        requireNonNull(commandClass);
        this.commandClass = commandClass;
        initDefaultConfigs();
    }

    /**
     * Returns the instance of the Grid component.
     *
     * @return the instance
     * @throws IllegalStateException if the component was not started
     */
    public T get() {
        if (command == null) {
            throw new IllegalStateException("Component '" + ClassUtils.getName(commandClass) + "' was not started");
        }
        return command;
    }

    /**
     * Adds an argument/option to configure the component.
     *
     * @param name the name
     */
    public Component<T> option(String name) {
        requireNonNull(name);
        this.arguments.add(name);
        return this;
    }

    /**
     * Adds an argument/option to configure the component.
     *
     * @param name the name
     */
    public <A> Component<T> option(String name, A value) {
        requireNonNull(name);
        this.arguments.add("--" + name);
        this.arguments.add(ObjectUtils.toString(value));
        return this;
    }

    /**
     * Adds an argument/option to configure the component.
     *
     * @param value the value
     * @param <A>   the type of the value
     */
    public <A> Component<T> option(A value) {
        requireNonNull(value);
        this.arguments.add(ObjectUtils.toString(value));
        return this;
    }

    /**
     * Starts the component.
     */
    public void start() {
        createCommand();
        createStreams();
        CliCommand.Executable executable = command.configure(out, err, getFinalArguments());
        executable.run();
        if (hasErrors()) {
            throw new IllegalStateException("The component '" + ClassUtils.getName(commandClass) + "' could not be started, error:\n " + getErrors());
        }
    }

    /**
     * Stops the component.
     */
    public void stop() {
        // there is no stop
    }

    /**
     * Returns the output of the command.
     *
     * @return a non-null instance
     */
    public String getOutput() {
        return outStream.toString();
    }

    /**
     * Returns the output of the command.
     *
     * @return a non-null instance
     */
    public String getErrors() {
        return errStream.toString();
    }

    /**
     * Returns whether the command has errors.
     *
     * @return {@code true} if it has errors, {@code false} otherwise
     */
    public boolean hasErrors() {
        return errStream.size() > 0;
    }

    /**
     * Describes the options used to configure the component.
     *
     * @return a non-nunll instance
     */
    public String describeConfig() {
        createCommand();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(output);
        TemplateGridCommand command = ClassUtils.create(commandClass);
        command.configure(stream, stream, "--dump-config").run();
        try {
            return MemoryResource.create(output.toByteArray()).loadAsString();
        } catch (Exception e) {
            return ExceptionUtils.throwException(e);
        }
    }

    private void createCommand() {
        command = ClassUtils.create(commandClass);
    }

    private void createStreams() {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        out = new PrintStream(outStream);
        err = new PrintStream(errStream);
    }

    private void initDefaultConfigs() {
        option("configure-logging", false);
    }

    private String[] getFinalArguments() {
        List<String> finalArguments = new ArrayList<>();
        finalArguments.addAll(arguments);
        return finalArguments.toArray(new String[0]);
    }
}
