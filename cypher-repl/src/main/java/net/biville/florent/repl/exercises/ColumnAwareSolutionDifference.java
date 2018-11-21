package net.biville.florent.repl.exercises;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ColumnAwareSolutionDifference implements SolutionDifference {

    private final List<Map<String, Object>> expectedNotFound = new ArrayList<>();

    private final List<Map<String, Object>> unexpectedFound = new ArrayList<>();

    private ColumnAwareSolutionDifference() {
    }

    public static ColumnAwareSolutionDifference empty() {
        return new ColumnAwareSolutionDifference();
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

        return String.format("Expected records not found:%s%nUnexpected records found:%s",
                expectedNotFound.isEmpty() ? "\tNone" : expectedNotFound,
                unexpectedFound.isEmpty() ? "\tNone" : unexpectedFound);
    }

    public ColumnAwareSolutionDifference addExpectedNotFound(List<Map<String, Object>> values) {
        this.expectedNotFound.addAll(values);
        return this;
    }

    public ColumnAwareSolutionDifference addUnexpectedFound(List<Map<String, Object>> values) {
        this.unexpectedFound.addAll(values);
        return this;
    }

    private String formatDiff(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(Map::entrySet)
                .map(entries -> {
                    if (entries.isEmpty()) {
                        return "";
                    }
                    return String.join(",", formatEntries(entries));
                })
                .filter(s -> !s.isEmpty())
                .reduce("", (a, b) -> String.format("%s%n%s", a, b));
    }

    private List<String> formatEntries(Collection<Map.Entry<String, Object>> entries) {
        return entries.stream()
                .map(e -> String.format("\t(column %s, value %s)", e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
