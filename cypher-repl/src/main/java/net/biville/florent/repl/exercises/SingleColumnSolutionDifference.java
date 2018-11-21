package net.biville.florent.repl.exercises;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SingleColumnSolutionDifference implements SolutionDifference {


    private final List<Collection<Object>> expectedNotFound = new ArrayList<>();

    private final List<Collection<Object>> unexpectedFound = new ArrayList<>();

    private SingleColumnSolutionDifference() {
    }

    public static SingleColumnSolutionDifference empty() {
        return new SingleColumnSolutionDifference();
    }

    @Override
    public boolean isEmpty() {
        return expectedNotFound.isEmpty() && unexpectedFound.isEmpty();
    }

    @Override
    public String getReport() {
        String expectedNotFound = formatDiff(this.expectedNotFound);
        String unexpectedFound = formatDiff(this.unexpectedFound);
        if (expectedNotFound.isEmpty() && unexpectedFound.isEmpty()) {
            return "All good!";
        }

        return String.format("Expected values not found:%s%nUnexpected values found:%s",
                expectedNotFound.isEmpty() ? "\tNone" : expectedNotFound,
                unexpectedFound.isEmpty() ? "\tNone" : unexpectedFound);
    }

    public SingleColumnSolutionDifference addExpectedNotFound(List<Collection<Object>> values) {
        this.expectedNotFound.addAll(values);
        return this;
    }

    public SingleColumnSolutionDifference addUnexpectedFound(List<Collection<Object>> values) {
        this.unexpectedFound.addAll(values);
        return this;
    }

    private String formatDiff(List<Collection<Object>> rows) {
        return rows.stream()
                .map(values -> {
                    if (values.isEmpty()) {
                        return "";
                    }
                    return String.join(",", formatEntries(values));
                })
                .filter(s -> !s.isEmpty())
                .reduce("", (a, b) -> String.format("%s%n%s", a, b));
    }

    private List<String> formatEntries(Collection<Object> entries) {
        return entries.stream()
                .map(e -> String.format("\t%s", e.toString()))
                .collect(Collectors.toList());
    }
}
