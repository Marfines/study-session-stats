package io.github.marfines.study;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Validates study sessions and totals their durations by subject. */
public final class StudyStats {
    private StudyStats() { }

    public record Summary(int sessions, long totalMinutes, Map<String, Long> bySubject) { }

    public static Summary analyze(Reader input) {
        JsonReader reader = new JsonReader(input);
        reader.setStrictness(Strictness.STRICT);
        JsonElement root;
        try {
            root = new Gson().fromJson(reader, JsonElement.class);
            if (reader.peek() != com.google.gson.stream.JsonToken.END_DOCUMENT) {
                throw new IllegalArgumentException("Only one JSON document is allowed");
            }
        } catch (JsonParseException | java.io.IOException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
        if (root == null || !root.isJsonArray()) {
            throw new IllegalArgumentException("Expected an array of sessions");
        }
        JsonArray sessions = root.getAsJsonArray();
        Map<String, Long> totals = new TreeMap<>();
        long totalMinutes = 0;
        for (JsonElement element : sessions) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Each session must be an object");
            }
            JsonObject session = element.getAsJsonObject();
            JsonElement subjectValue = session.get("subject");
            JsonElement minutesValue = session.get("minutes");
            if (subjectValue == null || !subjectValue.isJsonPrimitive()
                    || !subjectValue.getAsJsonPrimitive().isString()
                    || subjectValue.getAsString().isBlank()) {
                throw new IllegalArgumentException("Subject must be a non-empty string");
            }
            if (minutesValue == null || !minutesValue.isJsonPrimitive()
                    || !minutesValue.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException("Minutes must be an integer from 1 to 1440");
            }
            int minutes;
            try {
                minutes = minutesValue.getAsBigDecimal().intValueExact();
            } catch (ArithmeticException | NumberFormatException e) {
                throw new IllegalArgumentException("Minutes must be an integer from 1 to 1440", e);
            }
            if (minutes < 1 || minutes > 1440) {
                throw new IllegalArgumentException("Minutes must be an integer from 1 to 1440");
            }
            totals.merge(subjectValue.getAsString().strip(), (long) minutes, Long::sum);
            totalMinutes += minutes;
        }
        return new Summary(sessions.size(), totalMinutes, Collections.unmodifiableMap(totals));
    }

    public static String format(Summary summary) {
        StringBuilder text = new StringBuilder("Sessions: ").append(summary.sessions())
                .append("\nTotal minutes: ").append(summary.totalMinutes()).append('\n');
        summary.bySubject().forEach((subject, minutes) ->
                text.append(subject).append(": ").append(minutes).append(" min\n"));
        return text.toString();
    }
}
