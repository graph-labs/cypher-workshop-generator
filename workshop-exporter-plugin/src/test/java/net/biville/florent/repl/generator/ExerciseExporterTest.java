package net.biville.florent.repl.generator;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.harness.junit.Neo4jRule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.LogManager;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.lines;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ExerciseExporterTest {

    static {
        LogManager.getLogManager().reset();
    }

    private static final Kryo KRYO = new Kryo();

    @Rule
    public Neo4jRule graphDb = new Neo4jRule();
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File dump;
    private BiConsumer<File, Collection<JsonExercise>> exporter;

    @Before
    public void prepare() throws IOException {
        dump = folder.newFile("dump.cypher");
        exporter = new ExerciseExporter(graphDb.boltURI().toString(), AuthTokens.none());
    }

    @Test
    public void exports_single_exercise() throws IOException {
        exporter.accept(dump, singletonList(exercise("Crazy query!", "MATCH (n) RETURN COUNT(n) AS count")));

        String expectedBase64 = "AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAWNvdW70CQA=";
        assertThat(dump).hasContent(
                String.format("MERGE (e:Exercise {instructions: 'Crazy query!'}) ON CREATE SET e.rank = 1, e.result = '%s' ON MATCH SET e.result = '%1$s'", expectedBase64)
        );
        assertThatDeserializedResult(expectedBase64, result -> {
            assertThat(result).hasSize(1);
            Map<String, Object> row = result.iterator().next();
            assertThat(row).containsExactly(MapEntry.entry("count", 0L));
        });
    }

    @Test
    public void exports_write_exercises() {
        exporter.accept(dump, singletonList(exercise(
                "Create a node Person whose name is foobar",
                "MATCH (n:Person {name:'foobar'}) RETURN n.name",
                "CREATE (:Person {name:'foobar'})")));

        String expectedBase64 = "AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAW4ubmFt5QMBZm9vYmHy";
        assertThat(dump).hasContent(
                String.format("MERGE (e:Exercise {instructions: 'Create a node Person whose name is foobar'}) ON CREATE SET e.rank = 1, e.validationQuery = 'MATCH (n:Person {name:\\'foobar\\'}) RETURN n.name', e.result = '%s' ON MATCH SET e.rank = 1, e.validationQuery = 'MATCH (n:Person {name:\\'foobar\\'}) RETURN n.name', e.result = '%1$s'", expectedBase64)
        );
        try (Driver driver = GraphDatabase.driver(graphDb.boltURI(), config()); Session session = driver.session()) {
            StatementResult result = session.run("MATCH (n:Person {name:'foobar'}) RETURN n.name");
            assertThat(result.list()).isEmpty(); //rollbacks against remote DB
        }
    }

    @Test
    public void exports_several_exercises() {
        exporter.accept(dump, asList(
                exercise("foo", "MATCH (n:Foo) RETURN COUNT(n) AS foo"),
                exercise("bar", "MATCH (n:Bar) RETURN COUNT(n) AS bar")));

        assertThat(dump).hasContent(
                "MERGE (e:Exercise {instructions: 'foo'}) ON CREATE SET e.rank = 1, e.result = 'AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAWZv7wkA' ON MATCH SET e.result = 'AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAWZv7wkA'\n" +
                "MERGE (e:Exercise {instructions: 'bar'}) ON CREATE SET e.rank = 2, e.result = 'AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAWJh8gkA' ON MATCH SET e.result = 'AQEBAGphdmEudXRpbC5IYXNoTWHwAQEDAWJh8gkA'\n" +
                "MATCH (e:Exercise) WHERE EXISTS(e.rank) WITH e ORDER BY e.rank ASC WITH collect(e) AS exercises FOREACH (i IN range(0, length(exercises)-2) | FOREACH (first IN [exercises[i]] | FOREACH (second IN [exercises[i+1]] | MERGE (first)-[:NEXT]->(second) REMOVE first.rank REMOVE second.rank)))");
    }

    @Test
    public void runs_executable_dump_in_an_idempotent_way() throws IOException {
        exporter.accept(dump, asList(
                exercise("foo", "MATCH (n:Foo) RETURN COUNT(n) AS foo_count"),
                exercise("bar", "MATCH (n:Bar) RETURN COUNT(n) AS bar_count"),
                exercise("baz", "MATCH (n:Baz) RETURN COUNT(n) AS baz_count", "CREATE (:Baz {name:'meh'})")));
        URI boltUri = graphDb.boltURI();

        executeExportedDump(dump.toPath(), boltUri);
        executeExportedDump(dump.toPath(), boltUri);

        try (Driver driver = GraphDatabase.driver(boltUri, config()); Session session = driver.session()) {
            StatementResult result = session.run(
                    "MATCH p=(e1:Exercise)-[:NEXT*]->(e2:Exercise) WITH p ORDER BY LENGTH(p) DESC LIMIT 1 " +
                            "RETURN EXTRACT(exercise IN NODES(p) | exercise.instructions) AS instructions");
            assertThat(result.single().get("instructions").asList(Value::asString)).containsExactly("foo", "bar", "baz");
            result = session.run(
                    "MATCH (e:Exercise) WHERE EXISTS(e.validationQuery) RETURN e.instructions AS instruction");
            assertThat(result.single().get("instruction").asString()).isEqualTo("baz");
        }
    }

    private static void executeExportedDump(Path dump, URI uri) throws IOException {
        lines(dump, UTF_8).forEachOrdered(line -> {
            try (Driver driver = GraphDatabase.driver(uri, config()); Session session = driver.session()) {
                session.run(line);
            }
        });
    }

    private static void assertThatDeserializedResult(String expectedBase64, Consumer<List<Map<String, Object>>> asserts) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(expectedBase64);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = KRYO.readObject(new Input(inputStream), ArrayList.class);
            asserts.accept(result);
        }
    }

    private JsonExercise exercise(String instructions, String solutionQuery) {
        return exercise(instructions, solutionQuery, null);
    }

    private JsonExercise exercise(String instructions, String solutionQuery, String writeQuery) {
        JsonExercise exercise = new JsonExercise();
        exercise.setInstructions(instructions);
        exercise.setSolutionQuery(solutionQuery);
        exercise.setWriteQuery(writeQuery);
        return exercise;
    }

    private static Config config() {
        return Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig();
    }
}
