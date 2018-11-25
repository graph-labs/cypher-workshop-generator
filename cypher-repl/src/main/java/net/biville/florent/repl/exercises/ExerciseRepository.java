package net.biville.florent.repl.exercises;

import net.biville.florent.repl.graph.cypher.CypherQueryExecutor;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ExerciseRepository {

    private final CypherQueryExecutor executor;
    private final Base64.Decoder decoder;

    public ExerciseRepository(CypherQueryExecutor executor) {
        this.executor = executor;
        this.decoder = Base64.getDecoder();
    }

    public void importExercises(InputStream dataset) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataset, UTF_8))) {
            executor.commit(tx -> {
                tx.run("CREATE CONSTRAINT ON (exercise:Exercise) ASSERT exercise.id IS UNIQUE");
                tx.run("CREATE CONSTRAINT ON (exercise:Exercise) ASSERT exercise.rank IS UNIQUE");
            });
            Random random = new Random(upsertSeed());
            executor.commit(tx -> {
                reader.lines().forEachOrdered(line ->
                        tx.run(line.replaceAll("\\\\n", "\n"), map("id", random.nextLong()))
                );
            });
            executor.commit(tx -> {
                tx.run("MATCH path=(first_exercise:Exercise)-[NEXT*]->(:Exercise) \n" +
                        "WITH first_exercise, path\n" +
                        "ORDER BY length(path) DESC LIMIT 1 \n" +
                        "MATCH (s:TraineeSession) WHERE NOT((s)-[:CURRENTLY_AT]->(:Exercise)) \n" +
                        "MERGE (s)-[:CURRENTLY_AT]->(first_exercise)");
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Exercise findCurrentExercise() {
        List<Map<String, Object>> rows = executor.rollback(tx -> {
            return tx.run("MATCH (:TraineeSession)-[:CURRENTLY_AT]->(e:Exercise)<-[p:NEXT*0..]-(:Exercise), (all:Exercise) " +
                    "WITH e,p, count(all) AS total " +
                    "ORDER BY length(p) DESC LIMIT 1 " +
                    "RETURN e.instructions AS instructions," +
                    "       e.result AS result," +
                    "       e.validationQuery AS validationQuery," +
                    "       1+length(p) AS position," +
                    "       total"
            ).list(Record::asMap);
        });

        int count = rows.size();
        if (count != 1) {
            throw new RuntimeException(String.format("Expected 1 current exercise, got %d.", count));
        }

        Map<String, Object> row = rows.iterator().next();
        Object validationQuery = row.get("validationQuery");
        return new Exercise(
                row.get("instructions").toString(),
                validationQuery == null ? null : validationQuery.toString(),
                decoder.decode(row.get("result").toString()),
                parseInt(row.get("position").toString(), 10),
                parseInt(row.get("total").toString(), 10));
    }

    public boolean moveToNextExercise() {
        String query =
                "MATCH (t:TraineeSession)-[c:CURRENTLY_AT]->(:Exercise)-[:NEXT]->(e:Exercise) " +
                        "DELETE c " +
                        "CREATE (t)-[:CURRENTLY_AT]->(e) " +
                        "RETURN true";

        return !executor.commit(tx -> {
            return tx.run(query).list(Record::asMap);
        }).isEmpty();
    }

    public void resetProgression() {
        executor.commit(tx -> {
            tx.run("MATCH (s:TraineeSession)-[r:CURRENTLY_AT]->(current:Exercise), (first:Exercise)" +
                    "WHERE NOT((:Exercise)-[:NEXT]->(first)) AND current <> first " +
                    "DELETE r " +
                    "CREATE (s)-[:CURRENTLY_AT]->(first)");
        });
    }

    private long upsertSeed() {
        return executor.commit(tx -> {
            Record record = tx.run("MERGE (session:TraineeSession) " +
                    "ON CREATE SET session.seed = {seed} " +
                    "RETURN session.seed AS seed", randomSeedValue()).single();
            return record.get("seed").asLong();
        });
    }

    private Map<String, Object> randomSeedValue() {
        return map("seed", new Random().nextLong());
    }

    private Map<String, Object> map(String key, Object value) {
        Map<String, Object> result = new HashMap<>(2);
        result.put(key, value);
        return result;
    }
}
