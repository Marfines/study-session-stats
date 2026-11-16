package io.github.marfines.study;

import static org.junit.jupiter.api.Assertions.*;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StudyStatsTest {
    @Test
    void groupsSubjectsAndSortsOutput() {
        var summary = StudyStats.analyze(new StringReader("""
                [{"subject":"Math","minutes":45},
                 {"subject":" Java ","minutes":60},
                 {"subject":"Math","minutes":30}]
                """));
        assertEquals("Sessions: 3\nTotal minutes: 135\nJava: 60 min\nMath: 75 min\n",
                StudyStats.format(summary));
        assertThrows(UnsupportedOperationException.class, () -> summary.bySubject().put("X", 1L));
    }

    @Test
    void acceptsEmptyInputArray() {
        assertEquals("Sessions: 0\nTotal minutes: 0\n",
                StudyStats.format(StudyStats.analyze(new StringReader("[]"))));
    }

    @Test
    void acceptsBoundaryDurationsAndUnicode() {
        var summary = StudyStats.analyze(new StringReader("""
                [{"subject":"Математика","minutes":1},{"subject":"Математика","minutes":1440}]
                """));
        assertEquals(1441L, summary.bySubject().get("Математика"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{}", "[null]", "[", "[] []", "[{}]",
            "[{\"subject\":\" \",\"minutes\":1}]"})
    void rejectsInvalidDocuments(String json) {
        assertThrows(IllegalArgumentException.class, () -> StudyStats.analyze(new StringReader(json)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1441", "1.5", "2147483648", "null", "\"30\"", "true"})
    void rejectsInvalidDurations(String minutes) {
        String json = "[{\"subject\":\"Java\",\"minutes\":" + minutes + "}]";
        assertThrows(IllegalArgumentException.class, () -> StudyStats.analyze(new StringReader(json)));
    }
}
