package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.ExerciseRepository;
import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.logging.ConsoleLogger;

public class SkipCommand implements Command {

    private final ConsoleLogger logger;
    private final ExerciseRepository exerciseRepository;

    public SkipCommand(ConsoleLogger logger, ExerciseRepository exerciseRepository) {
        this.logger = logger;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String help() {
        return String.format("%s Skips the current exercise and goes to the next", name());
    }

    @Override
    public String name() {
        return Command.PREFIX + "skip";
    }

    @Override
    public boolean hidden() {
        return true;
    }

    @Override
    public void accept(TraineeSession traineeSession, String s) {
        if (!exerciseRepository.moveToNextExercise()) {
            logger.error("Could not move to next exercise. Is it the last one?");
        }
    }
}
