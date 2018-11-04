package net.biville.florent.repl.exercises;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;

class ResultOperations {

    public static ResultDifference difference(List<Map<String, Object>> actualResult,
                                              List<Map<String, Object>> expectedResult) {

        int rowCount = expectedResult.size();
        ResultDifference difference = new ResultDifference(rowCount);
        IntStream.range(0, rowCount)
                .mapToObj(i -> rowDifference(actualResult.get(i), expectedResult.get(i)))
                .forEach(difference::add);
        return difference;
    }

    private static ResultRowDifference rowDifference(Map<String, Object> actualRow,
                                                     Map<String, Object> expectedRow) {

        if (actualRow.equals(expectedRow)) {
            return ResultRowDifference.empty();
        }

        if (singleColumnResults(actualRow, expectedRow)) {
            Object expectedValue = expectedRow.values().iterator().next();
            Object actualValue = actualRow.values().iterator().next();
            if (expectedValue.equals(actualValue)) {
                return ResultRowDifference.empty();
            }
        }

        return computeDifference(actualRow, expectedRow);
    }

    private static ResultRowDifference computeDifference(Map<String, Object> actualRow, Map<String, Object> expectedRow) {
        Set<Map.Entry<String, Object>> actualEntries = actualRow.entrySet();
        Set<Map.Entry<String, Object>> expectedEntries = expectedRow.entrySet();

        return new ResultRowDifference(
                expectedEntries.stream().sorted(comparing(Map.Entry::getKey)).filter(e -> !actualEntries.contains(e)).collect(Collectors.toList()),
                actualEntries.stream().sorted(comparing(Map.Entry::getKey)).filter(a -> !expectedEntries.contains(a)).collect(Collectors.toList())
        );
    }

    private static boolean singleColumnResults(Map<String, Object> actualRow, Map<String, Object> expectedRow) {
        return expectedRow.size() == 1 && actualRow.size() == expectedRow.size();
    }
}
