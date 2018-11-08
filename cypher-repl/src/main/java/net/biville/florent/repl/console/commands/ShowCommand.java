package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.logging.ConsoleLogger;

public class ShowCommand implements Command {

    private final ConsoleLogger logger;

    public ShowCommand(ConsoleLogger logger) {
        this.logger = logger;
    }

    @Override
    public String help() {
        return String.format("%s - shows current exercise instructions", name());
    }

    @Override
    public String name() {
        return PREFIX + "show";
    }

    @Override
    public void accept(TraineeSession session, String ignored) {
        session.getCurrentExercise().accept(logger);
    }

    @Override
    public String toString() {
        return ":show";
    }
}
