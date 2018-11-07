package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.logging.ConsoleLogger;
import org.jline.utils.AttributedStyle;

import java.util.LinkedHashSet;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static org.jline.utils.AttributedStyle.BLACK;
import static org.jline.utils.AttributedStyle.WHITE;

public class CommandRegistry implements Command {

    public static final String HELP_COMMAND = Command.PREFIX + "help";

    private final ConsoleLogger logger;
    private final LinkedHashSet<Command> commands;

    public CommandRegistry(ConsoleLogger logger, Command[] commands) {
        this.logger = logger;
        this.commands = new LinkedHashSet<>(asList(commands));
        this.commands.add(new ShowCommand(logger));
        this.commands.add(new ExitCommand(logger));
        this.commands.add(new ResetProgressionCommand(logger));
        this.commands.add(new CypherRefcard(logger));
        this.commands.add(this);
    }

    public Optional<Command> findFirst(String query) {
        return commands.stream()
                .filter(command -> command.matches(query))
                .findFirst();
    }

    @Override
    public boolean matches(String query) {
        return normalize(query).equals(HELP_COMMAND);
    }

    @Override
    public String help() {
        return format("{0} - displays the list of available commands", HELP_COMMAND);
    }

    @Override
    public void accept(TraineeSession session, String ignored) {
        commands.forEach(command -> {
            logger.information(command.help());
        });
    }
}
