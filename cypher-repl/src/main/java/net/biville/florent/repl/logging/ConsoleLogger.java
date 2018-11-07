package net.biville.florent.repl.logging;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.PrintStream;

import static org.jline.utils.AttributedStyle.BLUE;
import static org.jline.utils.AttributedStyle.CYAN;
import static org.jline.utils.AttributedStyle.DEFAULT;
import static org.jline.utils.AttributedStyle.GREEN;
import static org.jline.utils.AttributedStyle.RED;
import static org.jline.utils.AttributedStyle.YELLOW;

public class ConsoleLogger {

    private final Terminal terminal;

    public ConsoleLogger(Terminal terminal) {
        this.terminal = terminal;
    }

    public void information(String string, Object... args) {
        log(string, DEFAULT.foreground(CYAN), System.out, args);
    }

    public void system(String string, Object... args) {
        log(string, DEFAULT.foreground(YELLOW), System.out, args);
    }

    public void success(String string, Object... args) {
        log("✔ " + string, DEFAULT.foreground(GREEN), System.out, args);
    }

    public void failure(String string, Object... args) {
        log("✗ " + string, DEFAULT.foreground(RED), System.err, args);
    }

    public void error(String string, Object... args) {
        log(string, DEFAULT.foreground(RED), System.err, args);
    }

    private void log(String string, AttributedStyle style, PrintStream output, Object... args) {
        if (args.length == 0) {
            log(System.out, style, string);
            return;
        }
        log(output, style, String.format(string, args));
    }

    private void log(PrintStream out, AttributedStyle style, String log) {
        out.println(new AttributedStringBuilder().append(log, style).toAnsi(terminal));
    }
}
