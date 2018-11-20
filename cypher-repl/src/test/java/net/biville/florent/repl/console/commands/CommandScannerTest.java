package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;
import net.biville.florent.repl.graph.cypher.CypherQueryExecutor;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CommandScannerTest {

    @Test
    public void returns_nothings_if_no_custom_commands_in_package() {
        Command[] commands = new CommandScanner(mock(CypherQueryExecutor.class), "com.acme").scan();

        assertThat(commands).isEmpty();
    }

    @Test
    public void returns_custom_commands_found_in_classpath() {
        Command[] commands = new CommandScanner(mock(CypherQueryExecutor.class), this.getClass().getPackage().getName()).scan();

        assertThat(commands).containsOnlyOnce(new MyCustomCommand());
    }

    @Test
    public void returns_custom_commands_with_executor_in_sole_ctor_arg() {
        Command[] commands = new CommandScanner(mock(CypherQueryExecutor.class), this.getClass().getPackage().getName()).scan();


        assertThat(commands)
                .filteredOn((cmd) -> cmd instanceof MyExecutorCommand)
                .isNotEmpty();
    }

    static class MyCustomCommand implements Command {

        @Override
        public String help() {
            return "welp";
        }

        @Override
        public String name() {
            return "hello";
        }

        @Override
        public void accept(TraineeSession session, String statement) {
        }

        @Override
        public int hashCode() {
            return Objects.hash("hello");
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final MyCustomCommand other = (MyCustomCommand) obj;
            return Objects.equals("hello", "hello");
        }

        @Override
        public String toString() {
            return Command.PREFIX + "hello";
        }
    }

    static class MyExecutorCommand implements Command {

        private final CypherQueryExecutor executor;

        public MyExecutorCommand(CypherQueryExecutor executor) {
            this.executor = executor;
        }

        @Override
        public String help() {
            return "exec";
        }

        @Override
        public String name() {
            return ":exec";
        }

        @Override
        public void accept(TraineeSession traineeSession, String s) {
            this.executor.commit((tx) -> {
                tx.run(s);
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyExecutorCommand that = (MyExecutorCommand) o;
            return Objects.equals(name(), that.name());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name());
        }
    }

}
