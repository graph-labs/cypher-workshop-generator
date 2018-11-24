package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.logging.ConsoleLogger;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CypherRefcardCommand implements Command {

    private static final String COMMAND_NAME = Command.PREFIX + "refcard";
    private final ConsoleLogger logger;

    public CypherRefcardCommand(ConsoleLogger logger) {
        this.logger = logger;
    }

    @Override
    public String help() {
        return String.format("%s - opens Cypher cheat sheet / reference card (refcard)", name());
    }

    @Override
    public String name() {
        return COMMAND_NAME;
    }

    @Override
    public void accept(TraineeSession traineeSession, String s) {
        try {
            Desktop.getDesktop()
                    .open(loadFile("neo4j-cypher-refcard-stable.pdf"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private File loadFile(String name) {
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, "refcard.pdf");
        if (file.exists()) {
            return file;
        }
        copyToFile(name, file);
        return file;
    }

    private void copyToFile(String name, File file) {
        try (InputStream input = CypherRefcardCommand.class.getClassLoader().getResourceAsStream(name);
             OutputStream output = new FileOutputStream(file)) {
            if (input == null) {
                logger.error("Cannot read %s", file.getAbsolutePath());
                return;
            }
            int readBytes;
            byte[] data = new byte[Math.max(input.available(), 1024)];
            while ((readBytes = input.read(data, 0, data.length)) != -1) {
                output.write(data, 0, readBytes);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
