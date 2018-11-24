package net.biville.florent.repl.console.commands;

import net.biville.florent.repl.exercises.TraineeSession;

import java.util.Locale;
import java.util.function.BiConsumer;

public interface Command extends BiConsumer<TraineeSession, String>, Comparable<Command> {

    String PREFIX = ":";

    String help();

    String name();

    default boolean hidden() {
        return false;
    }

    default boolean matches(String query) {
        return normalize(query).equals(name());
    }

    @Override
    default int compareTo(Command other) {
        return name().compareTo(other.name());
    }

    default String normalize(String input) {
        return input.trim().toLowerCase(Locale.ENGLISH);
    }
}
