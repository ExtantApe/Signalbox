package com.extantape.signalbox;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnswerTest {
    @Test
    void oneAsciiLineWithThePrefix() {
        String line = Answer.ok("r1", Map.of("name", "Zoë ☃\nnext"));
        assertTrue(line.startsWith("ea-result r1 {"), line);
        assertFalse(line.contains("\n"));
        assertTrue(line.chars().allMatch(c -> c >= 0x20 && c <= 0x7e), "plain ASCII only: " + line);
        assertTrue(line.contains("Zo\\u00eb \\u2603\\u000anext"), line);
    }

    @Test
    void errorsHaveACodeAndPlainWords() {
        assertEquals("ea-result r2 {\"id\":\"r2\",\"ok\":false,\"error\":{\"code\":\"not-found\",\"message\":\"Nobody called Bo.\"}}",
                Answer.error("r2", Errors.NOT_FOUND, "Nobody called Bo."), "fixed key order, every run");
        assertTrue(Answer.error("r2", Errors.NOT_FOUND, "Nobody called Bo.").contains("\"code\":\"not-found\""));
    }

    @Test
    void tooLongBecomesAnError() {
        String line = Answer.ok("r3", Map.of("big", "x".repeat(9000)));
        assertTrue(line.length() <= Answer.MAX_LINE);
        assertTrue(line.contains("\"ok\":false"));
    }

    @Test
    void needsListsTheQuestionsAndChangesNothing() {
        var qs = List.of(new Confirm.Question("two-circles", "Put them in two chat circles?"), new Confirm.Question("adults-world", "Open an adults-only world?"));
        assertEquals(List.of(qs.get(1)), Confirm.missing(qs, java.util.Set.of("two-circles")));
        assertTrue(Confirm.missing(qs, java.util.Set.of("two-circles", "adults-world")).isEmpty());
        String line = Answer.needs("r4", qs);
        assertTrue(line.contains("\"needs\":[{") && line.contains("two-circles"), line);
    }

    @Test
    void aSecretAnswerOnlyPointsToTheFile() {
        String line = Answer.secret("r5", "once/r5.json", Map.of("player", "Bo"));
        assertTrue(line.contains("\"secretFile\":\"once/r5.json\""), line);
    }
}
