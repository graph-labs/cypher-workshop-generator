package net.biville.florent.repl.exercises;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static net.biville.florent.repl.Maps.mutableMap;
import static org.assertj.core.api.Assertions.assertThat;

public class SolutionsTest {

    @Test
    public void computes_record_difference() {
        List<Map<String, Object>> expectedResult = asList(
                mutableMap("foo", "bar"),
                mutableMap("baz", "bah")
        );
        List<Map<String, Object>> actualResult = asList(
                mutableMap("foo2", "bar2"),
                mutableMap("baz", "bah")
        );

        SolutionDifference difference = Solutions.difference(actualResult, expectedResult);

        assertThat(difference.getReport())
                .isEqualTo(
                        "Expected values not found:\n" +
                                "\tbar\n" +
                                "Unexpected values found:\n" +
                                "\tbar2");
    }

    @Test
    public void reports_no_difference() {
        List<Map<String, Object>> expectedResult = Collections.singletonList(mutableMap("foo", "bar"));
        List<Map<String, Object>> actualResult = Collections.singletonList(mutableMap("foo", "bar"));

        SolutionDifference difference = Solutions.difference(actualResult, expectedResult);

        assertThat(difference.getReport()).isEqualTo("All good!");
    }

    @Test
    public void reports_no_difference_for_single_column_results_with_different_columns() {
        List<Map<String, Object>> expectedResult = Collections.singletonList(
                mutableMap("first_name", "James")
        );
        List<Map<String, Object>> actualResult = Collections.singletonList(
                mutableMap("result.first_name", "James")
        );

        SolutionDifference difference = Solutions.difference(actualResult, expectedResult);

        assertThat(difference.getReport()).isEqualTo("All good!");
    }

    @Test
    public void reports_only_value_difference_for_single_column_results() {
        List<Map<String, Object>> expectedResult = Collections.singletonList(
                mutableMap("first_name", "Jamie")
        );
        List<Map<String, Object>> actualResult = Collections.singletonList(
                mutableMap("result.first_name", "James")
        );

        SolutionDifference difference = Solutions.difference(actualResult, expectedResult);

        assertThat(difference.getReport()).isEqualTo("Expected values not found:" +
                "\n\tJamie" +
                "\nUnexpected values found:" +
                "\n\tJames");
    }

    @Test
    public void reports_only_value_difference_for_expected_single_column_results() {
        List<Map<String, Object>> expectedResult = asList(
                mutableMap("first_name", "Jamie"),
                mutableMap("first_name", "Mary")
        );

        List<Map<String, Object>> actualResult = asList(
                mutableMap("result.first_name", "James", "result.last_name", "Dean"),
                mutableMap("result.first_name", "Mariah", "result.last_name", "Carey")
        );

        SolutionDifference difference = Solutions.difference(actualResult, expectedResult);

        assertThat(difference.getReport())
                .contains("Expected values not found:" +
                        "\n\tJamie" +
                        "\n\tMary")
                .contains("\nUnexpected values found:",
                        "\tJames",
                        "\tDean",
                        "\tMariah",
                        "\tCarey");

    }

    @Test
    public void does_not_report_difference_for_different_row_ordering() {
        List<Map<String, Object>> expectedResult = asList(
                of("prop1", 42L, "prop2", "value1"),
                of("prop1", 84L, "prop2", "value2"),
                of("prop1", 21L, "prop2", "value3")
        );
        List<Map<String, Object>> actualResult = new ArrayList<>(asList(
                newHashMap(of("prop1", 21L, "prop2", "value3")),
                newHashMap(of("prop1", 84L, "prop2", "value2")),
                newHashMap(of("prop1", 42L, "prop2", "value1"))
        ));

        SolutionDifference difference = Solutions.difference(actualResult, expectedResult);

        assertThat(difference.getReport()).isEqualTo("All good!");
    }
}
