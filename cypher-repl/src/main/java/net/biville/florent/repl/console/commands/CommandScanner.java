package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.graph.cypher.CypherQueryExecutor;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

public class CommandScanner {

    private final CypherQueryExecutor queryExecutor;
    private final Reflections reflections;

    public CommandScanner(CypherQueryExecutor queryExecutor, String packageToScan) {
        this.queryExecutor = queryExecutor;
        this.reflections = new Reflections(packageToScan);
    }

    public Command[] scan() {
        Predicate<Class<?>> notDefaultCommands =
                Predicate.<Class<?>>isEqual(CypherSessionFallbackCommand.class).negate()
                        .and(Predicate.<Class<?>>isEqual(CommandRegistry.class).negate())
                        .and(Predicate.<Class<?>>isEqual(ShowCommand.class).negate())
                        .and(Predicate.<Class<?>>isEqual(ExitCommand.class).negate())
                        .and(Predicate.<Class<?>>isEqual(ResetProgressionCommand.class).negate())
                        .and(Predicate.<Class<?>>isEqual(CypherRefcardCommand.class).negate())
                        .and(Predicate.<Class<?>>isEqual(SkipCommand.class).negate())
                ;

        return reflections.getSubTypesOf(Command.class)
                .stream()
                .filter(notDefaultCommands)
                .map(this::instantiate)
                .toArray(Command[]::new);
    }

    private <T> T instantiate(Class<? extends T> type) {
        Result<? extends T> result = defaultConstructor(type);
        if (!result.hasFailed()) {
            return result.get();
        }
        result = publicParameterlessConstructor(type);
        if (!result.hasFailed()) {
            return result.get();
        }
        result = executorArgConstructor(type);
        if (!result.hasFailed()) {
            return result.get();
        }
        throw new RuntimeException("Cannot instantiate command");
    }

    private <T> Result<T> defaultConstructor(Class<? extends T> type) {
        try {
            return new Result<T>(type.getDeclaredConstructor().newInstance());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            return new Result<T>(e);
        }
    }

    private <T> Result<T> publicParameterlessConstructor(Class<? extends T> type) {
        try {
            return new Result<T>(type.getConstructor().newInstance());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            return new Result<T>(e);
        }
    }

    private <T> Result<T> executorArgConstructor(Class<? extends T> type) {
        try {
            return new Result<T>(type.getConstructor(CypherQueryExecutor.class).newInstance(queryExecutor));
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            return new Result<T>(e);
        }
    }

}

class Result<T> {
    private final Exception exception;
    private final T result;

    public Result(T result) {
        this.exception = null;
        this.result = result;
    }

    public Result(Exception exception) {
        this.exception = exception;
        this.result = null;
    }

    public boolean hasFailed() {
        return exception != null;
    }

    public T get() {
        return result;
    }
}
