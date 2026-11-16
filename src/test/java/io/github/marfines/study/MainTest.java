package io.github.marfines.study;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    @TempDir Path directory;

    @Test
    void printsSummaryForValidFile() throws Exception {
        Path file = directory.resolve("sessions.json");
        Files.writeString(file, "[{\"subject\":\"Java\",\"minutes\":25}]");
        var output = new ByteArrayOutputStream();
        assertEquals(0, Main.run(new String[]{file.toString()}, new PrintStream(output), System.err));
        assertEquals("Sessions: 1\nTotal minutes: 25\nJava: 25 min\n", output.toString());
    }

    @Test
    void reportsMissingArgument() {
        var errors = new ByteArrayOutputStream();
        assertEquals(2, Main.run(new String[]{}, System.out, new PrintStream(errors)));
        assertTrue(errors.toString().contains("Usage:"));
    }

    @Test
    void reportsMissingFile() {
        var errors = new ByteArrayOutputStream();
        assertEquals(1, Main.run(new String[]{directory.resolve("missing.json").toString()},
                System.out, new PrintStream(errors)));
        assertTrue(errors.toString().startsWith("Error:"));
    }

    @Test
    void reportsInvalidData() throws Exception {
        Path file = directory.resolve("invalid.json");
        Files.writeString(file, "{}");
        var errors = new ByteArrayOutputStream();
        assertEquals(1, Main.run(new String[]{file.toString()}, System.out, new PrintStream(errors)));
        assertTrue(errors.toString().contains("Expected an array"));
    }
}
