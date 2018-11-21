package net.biville.florent.repl.exercises;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;

class Solutions {

    public static SolutionDifference difference(List<Map<String, Object>> actualResult,
                                                List<Map<String, Object>> expectedResult) {

        if (actualResult.equals(expectedResult)) {
            return ColumnAwareSolutionDifference.empty();
        }

        if (singleColumnResult(expectedResult)) {
            List<Collection<Object>> actualValues = extractValues(actualResult);
            List<Collection<Object>> expectedValues = extractValues(expectedResult);

            return SingleColumnSolutionDifference.empty()
                    .addExpectedNotFound(computeDifference(actualValues, expectedValues))
                    .addUnexpectedFound(computeDifference(expectedValues, actualValues));
        }

        return ColumnAwareSolutionDifference.empty()
                .addExpectedNotFound(computeDifference(actualResult, expectedResult))
                .addUnexpectedFound(computeDifference(expectedResult, actualResult));
    }

    private static boolean singleColumnResult(List<Map<String, Object>> rows) {
        return rows.stream()
                .filter(map -> map.size() > 1)
                .findAny().isEmpty();
    }

    private static List<Collection<Object>> extractValues(List<Map<String, Object>> rows) {
        return rows
                .stream()
                .map(Map::entrySet)
                .map(es -> es.stream().map(Map.Entry::getValue).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private static <T> List<T> computeDifference(List<T> rows1,
                                                 List<T> rows2) {
        return rows2.stream()
                .filter(not(rows1::contains))
                .collect(Collectors.toList());
    }
}
