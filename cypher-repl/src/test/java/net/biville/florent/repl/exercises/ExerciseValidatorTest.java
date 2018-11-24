package net.biville.florent.repl.exercises;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import net.biville.florent.repl.logging.ConsoleLogger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static net.biville.florent.repl.Lists.mutableList;
import static net.biville.florent.repl.Maps.mutableMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExerciseValidatorTest {

    private Kryo serializer = new Kryo();

    private ExerciseValidator validator = new ExerciseValidator(mock(ConsoleLogger.class), serializer);

    @Test
    public void answer_is_valid_regardless_of_row_order() {
        Exercise exercise = mock(Exercise.class);
        when(exercise.getSerializedResult()).thenReturn(serialize(
                mutableList(
                        mutableMap("prop1", 21L, "prop2", "value3"),
                        mutableMap("prop1", 84L, "prop2", "value2"),
                        mutableMap("prop1", 42L, "prop2", "value1")
                )
        ));

        ExerciseValidation validation = validator.validate(mutableList(
                mutableMap("prop1", 42L, "prop2", "value1"),
                mutableMap("prop1", 84L, "prop2", "value2"),
                mutableMap("prop1", 21L, "prop2", "value3")
        ), exercise);

        assertThat(validation.isSuccessful())
                .overridingErrorMessage("Row do not need to be ordered")
                .isTrue();
    }

    @Test
    public void answer_is_valid_regardless_of_property_order() {
        Exercise exercise = mock(Exercise.class);
        when(exercise.getSerializedResult()).thenReturn(serialize(
                mutableList(
                        mutableMap("prop1", 42L, "prop2", "value")
                ))
        );

        ExerciseValidation validation = validator.validate(mutableList(
                mutableMap("prop2", "value", "prop1", 42L)
        ), exercise);

        assertThat(validation.isSuccessful())
                .overridingErrorMessage("Properties do not need to be ordered")
                .isTrue();
    }

    private byte[] serialize(List<Map<String, Object>> object) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             Output output = new Output(outputStream)) {

            serializer.writeObject(output, object);
            output.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}