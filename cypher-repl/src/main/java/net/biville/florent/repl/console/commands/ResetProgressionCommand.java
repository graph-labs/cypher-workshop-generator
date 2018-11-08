package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.logging.ConsoleLogger;

public class ResetProgressionCommand implements Command {

    private final ConsoleLogger logger;

    public ResetProgressionCommand(ConsoleLogger logger) {
        this.logger = logger;
    }

    @Override
    public String help() {
        return String.format("%s - resets progression, you'll start over at the first exercise", name());
    }

    @Override
    public String name() {
        return PREFIX + "reset";
    }

    @Override
    public void accept(TraineeSession session, String ignored) {
        session.reset();
        logger.information("Progression reset! Current exercise is now:");
        session.getCurrentExercise().accept(logger);
    }
}
