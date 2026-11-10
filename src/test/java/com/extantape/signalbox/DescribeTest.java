package com.extantape.signalbox;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DescribeTest {
    @Test
    void carriesTheFormatAndTypedArguments() {
        var cmd = new Describe.Command("invite create", "latchkey.invite.create",
                List.of(Describe.Arg.of("player", "player"), Describe.Arg.listOf("profiles", "enum", List.of("kid", "builder"))),
                List.of("two-circles"), true);
        String line = Answer.ok("d1", Describe.body("Latchkey", "0.2.0", List.of(cmd)));
        assertTrue(line.contains("\"format\":1"), line);
        assertTrue(line.contains("\"type\":\"list\",\"of\":\"enum\",\"values\":[\"kid\",\"builder\"]"), line);
        assertTrue(line.contains("\"confirms\":[\"two-circles\"],\"secret\":true"), line);
    }

    @Test
    void optionalArgumentsAreMarkedAndOnlyThoseAre() {
        var cmd = new Describe.Command("apply", "console", List.of(Describe.Arg.of("uuid", "uuid"),
                Describe.Arg.of("whitelist", "bool").asOptional(), Describe.Arg.of("via", "string").asOptional()), List.of(), false);
        String line = Answer.ok("d2", Describe.body("Usher", "0.4.0", List.of(cmd)));
        assertTrue(line.contains("{\"name\":\"uuid\",\"type\":\"uuid\"}"), "required: no optional key at all, so older readers see nothing new: " + line);
        assertTrue(line.contains("{\"name\":\"whitelist\",\"type\":\"bool\",\"optional\":true}"), line);
        assertTrue(line.contains("{\"name\":\"via\",\"type\":\"string\",\"optional\":true}"), line);
        assertTrue(line.contains("\"format\":1"), "still format 1");
    }

    @Test
    void anOptionalArgumentCantComeBeforeARequiredOne() {
        var bad = new Describe.Command("apply", "console", List.of(Describe.Arg.of("whitelist", "bool").asOptional(),
                Describe.Arg.of("via", "string")), List.of(), false);
        assertThrows(IllegalArgumentException.class, () -> Describe.body("Usher", "0.4.0", List.of(bad)));
    }
}
