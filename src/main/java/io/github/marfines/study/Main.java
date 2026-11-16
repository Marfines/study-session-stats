package io.github.marfines.study;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 1) {
            err.println("Usage: java -jar study-session-stats.jar <sessions.json>");
            return 2;
        }
        try (var reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            out.print(StudyStats.format(StudyStats.analyze(reader)));
            return 0;
        } catch (IOException | IllegalArgumentException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
