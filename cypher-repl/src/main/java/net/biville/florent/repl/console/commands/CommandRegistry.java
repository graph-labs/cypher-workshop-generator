package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.logging.ConsoleLogger;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

public class CommandRegistry implements Command {

    private static final String HELP_COMMAND = Command.PREFIX + "help";
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
    public String help() {
        return format("{0} - displays the list of available commands", name());
    }

    @Override
    public String name() {
        return HELP_COMMAND;
    }

    @Override
    public void accept(TraineeSession session, String ignored) {
        commands.stream().sorted().forEach(command -> {
            logger.information(command.help());
        });
    }
}
