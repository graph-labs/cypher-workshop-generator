package net.biville.florent.repl.exercises;

import net.biville.florent.repl.graph.ReplConfiguration;
import net.biville.florent.repl.graph.cypher.CypherQueryExecutor;
import org.assertj.core.data.MapEntry;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.junit.Neo4jRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ExerciseRepositoryTest {

    static {
        LogManager.getLogManager().reset();
    }

    @Rule
    public Neo4jRule graphDatabase = new Neo4jRule();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ExerciseRepository repository;

    private GraphDatabaseService graph;

    @Before
    public void prepare() {
        ReplConfiguration configuration = new ReplConfiguration(graphDatabase.boltURI());
        repository = new ExerciseRepository(new CypherQueryExecutor(configuration));
        graph = graphDatabase.getGraphDatabaseService();
    }

    @Test
    public void imports_exercises() throws IOException {
        File file = folder.newFile();
        Files.write(file.toPath(),
                Arrays.asList(
                        "MERGE (e:Exercise {order:1, instructions: 'Trouvez le nombre de films', result: 'AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAWNvdW70CfzIAQ=='})",
                        "MERGE (e:Exercise {order:2, instructions: 'Trouvez le nombre de films d\\'action', result: 'AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAWNvdW70CbQm'})",
                        "MATCH (e:Exercise) WITH e ORDER BY e.order ASC WITH COLLECT(e) AS exercises FOREACH (i IN RANGE(0, length(exercises)-2) | FOREACH (first IN [exercises[i]] | FOREACH (second IN [exercises[i+1]] | MERGE (first)-[:NEXT]->(second) REMOVE first.rank REMOVE second.rank)))"),
                StandardCharsets.UTF_8
        );

        try (InputStream inputStream = new FileInputStream(file)) {
            repository.importExercises(inputStream);
        }

        try (Transaction ignored = graph.beginTx();
            Result result = graph.execute("MATCH (:TraineeSession)-[:CURRENTLY_AT]->(first:Exercise)-[:NEXT]->(last:Exercise) RETURN first.instructions, last.instructions")) {
            assertThat(result.hasNext()).overridingErrorMessage("Result should contain a first row").isTrue();
            Map<String, Object> row = result.next();
            assertThat(row).containsOnly(
                    MapEntry.entry("first.instructions", "Trouvez le nombre de films"),
                    MapEntry.entry("last.instructions", "Trouvez le nombre de films d\'action")
            );
            assertThat(result.hasNext()).overridingErrorMessage("Result should not contain extra rows").isFalse();
        }
    }

    @Test
    public void returns_current_exercise() {
        String solution = "whatever works";
        write(format("CREATE (:TraineeSession)-[:CURRENTLY_AT]->(:Exercise {instructions:'Do something', result:'%s'})",
                        encode(solution)));

        Exercise exercise = repository.findCurrentExercise();

        assertThat(exercise.getInstructions()).isEqualTo("Do something");
        assertThat(exercise.getPosition()).isEqualTo(1);
        assertThat(exercise.getTotal()).isEqualTo(1);
        assertThat(exercise.getSerializedResult()).isEqualTo(solution.getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    public void moves_to_next() {
        String nextSolution = "whatever works again";
        write(format("CREATE (:TraineeSession)-[:CURRENTLY_AT]->(:Exercise {instructions:'Do something', result:'%s'})-[:NEXT]->(:Exercise {instructions:'Next one!', result:'%s'})",
                        encode("whatever works"),
                        encode(nextSolution)));

        repository.moveToNextExercise();

        List<Map<String, Object>> rows = read("MATCH (e:Exercise)<-[:CURRENTLY_AT]-(:TraineeSession) " +
                "RETURN e.instructions AS instructions," +
                "       e.result AS result");

        assertThat(rows)
                .containsExactly(map(
                        tuple("instructions", "Next one!"),
                        tuple("result", encode((nextSolution)))
                ));
    }

    @Test
    public void resets_progression() {
        String resetExerciseSolution = "previous soon-to-be current solution";
        String cql = "CREATE (:TraineeSession)-[:CURRENTLY_AT]->(current:Exercise {instructions:'Next one!', result:'%s'})," +
                "(:Exercise {instructions:'Do something', result:'%s'})-[:NEXT]->(current)";
        write(format(cql, encode("ahah"), encode(resetExerciseSolution)));

        repository.resetProgression();

        List<Map<String, Object>> rows = read("MATCH (e:Exercise)<-[:CURRENTLY_AT]-(:TraineeSession) " +
                "RETURN e.instructions AS instructions," +
                "       e.result AS result");

        assertThat(rows)
                .hasSize(1)
                .containsExactly(map(
                        tuple("instructions", "Do something"),
                        tuple("result", encode(resetExerciseSolution))
                ));
    }

    private List<Map<String, Object>> read(String cql) {
        GraphDatabaseService gds = graphDatabase.getGraphDatabaseService();
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Transaction transaction = gds.beginTx();
             Result result = gds.execute(cql)) {

            while (result.hasNext()) {
                rows.add(result.next());
            }
            transaction.success();
        }
        return rows;
    }

    private void write(String cql) {
        GraphDatabaseService graphDatabaseService = graphDatabase.getGraphDatabaseService();
        try (Transaction tx = graphDatabaseService.beginTx()) {
            graphDatabaseService.execute(cql);
            tx.success();
        }
    }

    private Map<String, Object> map(Tuple... tuples) {
        HashMap<String, Object> map = new HashMap<>();
        for (Tuple tuple : tuples) {
            Object[] tupleArray = tuple.toArray();
            map.put((String)tupleArray[0], tupleArray[1]);
        }
        return map;
    }

    private static String encode(String solution) {
        return Base64.getEncoder().encodeToString(solution.getBytes(StandardCharsets.US_ASCII));
    }
}
