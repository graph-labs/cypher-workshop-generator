package net.biville.florent.repl.console;

import net.biville.florent.repl.console.commands.Command;
import net.biville.florent.repl.console.commands.CommandRegistry;
import net.biville.florent.repl.console.commands.CypherSessionFallbackCommand;
import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.graph.cypher.CypherQueryExecutor;
import net.biville.florent.repl.graph.cypher.CypherStatementValidator;
import net.biville.florent.repl.logging.ConsoleLogger;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import static net.biville.florent.repl.console.commands.CommandRegistry.HELP_COMMAND;
import static org.jline.utils.AttributedStyle.GREEN;
import static org.jline.utils.AttributedStyle.YELLOW;

public class Console {

    private final ConsoleLogger logger;
    private final CommandRegistry commandRegistry;
    private final LineReader lineReader;
    private final TraineeSession session;
    private final Command defaultCypherCommand;

    public Console(ConsoleLogger logger,
                   LineReader lineReader,
                   CommandRegistry commandRegistry,
                   TraineeSession session,
                   CypherQueryExecutor cypherQueryExecutor,
                   CypherStatementValidator cypherStatementValidator) {

        this.logger = logger;
        this.lineReader = lineReader;
        this.commandRegistry = commandRegistry;
        this.session = session;
        this.defaultCypherCommand = new CypherSessionFallbackCommand(
                logger,
                cypherQueryExecutor,
                cypherStatementValidator
        );
        
    }

    public void start() {
        logger.information(
                "                                                                                     \n" +
                        "                                                                                     \n" +
                        "                                                                                     \n" +
                        "                                                                              .hy    \n" +
                        "                                                                               :-    \n" +
                        "   `.   `....``          `.---..`           ``.-:-.``             `.           .`    \n" +
                        "   +m:+yysssyhh+`      -syysoosyyo-       -oyysooosyyo-          :ms           my    \n" +
                        "   +Nds.     `:dd.   `yd/.      .omo    .yd+.       .+dy`       -my`           Nh    \n" +
                        "   +Ns         -Ns   hd`          :N+  `dd.           .dd`     -my`            Nh    \n" +
                        "   +N:          Nh  -NhooooooooooooNd  /N/             /N/    -mh`             Nh    \n" +
                        "   +N-          Nh  -Ns--------------  /N/             /N/   -mh`      .:`     Nh    \n" +
                        "   +N-          Nh  `dd`               `dd`           `dd`  -mh`       oN:     Nh    \n" +
                        "   +N-          Nh   .dh:`       `--    .hd/`       `/dh.  .mh`        oN:     Nh    \n" +
                        "   +N-          Nh    `/yhs+//+oyys-     `:shyo+/+oyhs:`  .dNhsssssssssdN:     Nh    \n" +
                        "   `.           ..       `-:///:.`          `.:///:.`     `............sN:     Nh    \n" +
                        "                                                                       oN:     Ny    \n" +
                        "                                                                       oN: -:/ym:    \n" +
                        "                                                                       .:` :++/`     \n" +
                        "                                                                                     \n" +
                        "                                                                                     \n");

        logger.system("");
        logger.system("Initializing session now...");
        session.init(this.getClass().getResourceAsStream("/exercises/dump.cypher"));
        logger.system("... done!");
        logger.system("");
        logger.information("Welcome! Available commands can be displayed with '%s'", HELP_COMMAND);
        logger.information("");
        logger.information("Please make sure your Cypher statements end with a semicolon.");
        logger.information("Every exercise is independent, no changes are persisted against your database.");
        logger.information("Make sure to undo the insertions in the browser before using this REPL!");
        logger.information("");
        logger.information("Ask for help when needed and have fun!");
        while (true) {
            try {
                String statement = lineReader.readLine(prompt());
                commandRegistry
                        .findFirst(statement)
                        .orElse(defaultCypherCommand)
                        .accept(session, statement);
            } catch (UserInterruptException ignored) {
            } catch (EndOfFileException e) {
                logger.information("Goodbye!");
                return;
            }
        }
    }

    private String prompt() {
        return new AttributedStringBuilder()
                .style(AttributedStyle.BOLD)
                .append("(:Everyone)-[:`<3`]-(:Cypher)> ").toAnsi();
    }
}
