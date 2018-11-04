package net.biville.florent.repl.exercises;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResultDifference {

    private final List<ResultRowDifference> differences;

    public ResultDifference(int size) {
        differences = new ArrayList<>(size);
    }

    public boolean isEmpty() {
        return differences.stream().allMatch(ResultRowDifference::isEmpty);
    }

    public String getReport(boolean singleColumnResult) {
        String expectedNotFound = formatDiff(singleColumnResult, differences, ResultRowDifference::getExpectedNotFound);
        String unexpectedFound = formatDiff(singleColumnResult, differences, ResultRowDifference::getUnexpectedFound);
        if (expectedNotFound.isEmpty() && unexpectedFound.isEmpty()) {
            return "All good!";
        }

        if (singleColumnResult) {
            return String.format("Expected values not found:%s%nUnexpected values found:%s",
                    expectedNotFound.isEmpty() ? "\tNone" : expectedNotFound,
                    unexpectedFound.isEmpty() ? "\tNone" : unexpectedFound);
        }

        return String.format("Expected records not found:%s%nUnexpected records found:%s",
                expectedNotFound.isEmpty() ? "\tNone" : expectedNotFound,
                unexpectedFound.isEmpty() ? "\tNone" : unexpectedFound);
    }

    public void add(ResultRowDifference resultRowDifference) {
        differences.add(resultRowDifference);
    }

    private String formatDiff(boolean singleColumnResult,
                              List<ResultRowDifference> differences,
                              Function<ResultRowDifference, List<Map.Entry<String, Object>>> extractor) {

        return differences.stream()
                .map(extractor)
                .map(entries -> {
                    if (entries.isEmpty()) {
                        return "";
                    }
                    return String.join(",", formatEntries(singleColumnResult, entries));
                })
                .filter(s -> !s.isEmpty())
                .reduce("", (a, b) -> String.format("%s%n%s", a, b));
    }

    private List<String> formatEntries(boolean singleColumnResult, List<Map.Entry<String, Object>> entries) {
        return entries.stream()
                .map(e -> {
                    if (singleColumnResult) {
                        return String.format("\t%s", e.getValue().toString());
                    }
                    else {
                        return String.format("\t(column %s, value %s)", e.getKey(), e.getValue());
                    }
                })
                .collect(Collectors.toList());
    }
}
