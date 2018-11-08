package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.logging.ConsoleLogger;

public class ExitCommand implements Command {

    private final ConsoleLogger logger;

    public ExitCommand(ConsoleLogger logger) {
        this.logger = logger;
    }

    @Override
    public String help() {
        return String.format("%s - exits REPL", name());
    }

    @Override
    public String name() {
        return Command.PREFIX + "exit";
    }

    @Override
    public void accept(TraineeSession session, String s) {
        if (session.isCompleted()) {
            logger.success("Byyye! Keep rocking!");
        } else {
            logger.information(
                    "     .-\"\"\"\"\"\"-.\n" +
                            "   .'          '.\n" +
                            "  /   O      O   \\\n" +
                            " :           `    :\n" +
                            " |                |\n" +
                            " :    .------.    :\n" +
                            "  \\  '        '  /\n" +
                            "   '.          .'\n" +
                            "     '-......-'\n"
                            + "Sad to see you go");
        }
        System.exit(0);
    }
}
