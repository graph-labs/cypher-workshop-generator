package net.biville.florent.repl.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;

import java.io.File;
import java.util.Collection;

@Mojo(name = "generate-cypher-file", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true, requiresProject = false)
public class Main extends AbstractMojo {

    @Parameter(property = "exercise-input", required = true)
    private File exerciseDefinition;

    @Parameter(property = "cypher-output", required = true)
    private File outputFile;

    @Parameter(property = "bolt-uri", defaultValue = "bolt://localhost:7687")
    private String boltUri;

    @Parameter(property = "username", defaultValue = "neo4j")
    private String username;

    @Parameter(property = "password", required = true)
    private String password;

    @Override
    public void execute() {
        System.out.println("A word of warning:");
        System.out.println("Please make sure the configured database contains **only** the required data for the exercises.");
        System.out.println("In the end, the database content relied upon by the generator must be exactly the same as the one in user databases.");


        Collection<JsonExercise> exercises = new ExerciseParser().apply(exerciseDefinition);
        new ExerciseExporter(boltUri, authTokens(username, password))
                .accept(outputFile, exercises);
    }

    private static AuthToken authTokens(String username, String password) {
        if (username.isEmpty()) {
            return AuthTokens.none();
        }
        return AuthTokens.basic(username, password);
    }
}
