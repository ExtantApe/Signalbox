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
}
