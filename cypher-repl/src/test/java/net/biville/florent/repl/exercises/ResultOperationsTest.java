package net.biville.florent.repl.exercises;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultOperationsTest {

    @Test
    public void computes_record_difference() {
        List<Map<String, Object>> expectedResult = List.of(
                Map.of("foo", "bar"),
                Map.of("baz", "bah")
        );
        List<Map<String, Object>> actualResult = List.of(
                Map.of("foo2", "bar2"),
                Map.of("baz", "bah")
        );

        ResultDifference difference = ResultOperations.difference(actualResult, expectedResult);

        assertThat(difference.getReport(false))
                .isEqualTo(
                        "Expected records not found:" +
                        "\n\t(column foo, value bar)" +
                        "\nUnexpected records found:" +
                        "\n\t(column foo2, value bar2)");
    }

    @Test
    public void reports_no_difference() {
        List<Map<String, Object>> expectedResult = List.of(Map.of("foo", "bar"));
        List<Map<String, Object>> actualResult = List.of(Map.of("foo", "bar"));

        ResultDifference difference = ResultOperations.difference(actualResult, expectedResult);

        assertThat(difference.getReport(false)).isEqualTo("All good!");
    }

    @Test
    public void reports_no_difference_for_single_column_results_with_different_columns() {
        List<Map<String, Object>> expectedResult = List.of(
                Map.of("first_name", "James")
        );
        List<Map<String, Object>> actualResult = List.of(
                Map.of("result.first_name", "James")
        );

        ResultDifference difference = ResultOperations.difference(actualResult, expectedResult);

        assertThat(difference.getReport(true)).isEqualTo("All good!");
    }

    @Test
    public void reports_only_value_difference_for_single_column_results() {
        List<Map<String, Object>> expectedResult = List.of(
                Map.of("first_name", "Jamie")
        );
        List<Map<String, Object>> actualResult = List.of(
                Map.of("result.first_name", "James")
        );

        ResultDifference difference = ResultOperations.difference(actualResult, expectedResult);

        assertThat(difference.getReport(true)).isEqualTo("Expected values not found:" +
                "\n\tJamie" +
                "\nUnexpected values found:" +
                "\n\tJames");
    }

    @Test
    public void reports_only_value_difference_for_expected_single_column_results() {
        List<Map<String, Object>> expectedResult = List.of(
                Map.of("first_name", "Jamie"),
                Map.of("first_name", "Mary")
        );

        List<Map<String, Object>> actualResult = List.of(
                Map.of("result.first_name", "James", "result.last_name", "Dean"),
                Map.of("result.first_name", "Mariah", "result.last_name", "Carey")
        );

        ResultDifference difference = ResultOperations.difference(actualResult, expectedResult);

        assertThat(difference.getReport(true)).isEqualTo("Expected values not found:" +
                "\n\tJamie" +
                "\n\tMary" +
                "\nUnexpected values found:" +
                "\n\tJames,\tDean" +
                "\n\tMariah,\tCarey");
    }
}
