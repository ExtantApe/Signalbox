package com.extantape.signalbox;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {
    @Test
    void wireForms() throws Exception {
        assertTrue(Args.bool("true"));
        assertThrows(Request.Bad.class, () -> Args.bool("yes"));
        assertEquals(-12, Args.integer("-12"));
        assertThrows(Request.Bad.class, () -> Args.integer("1.5"));
        assertEquals("Steve_1", Args.player("Steve_1"));
        assertThrows(Request.Bad.class, () -> Args.player("no spaces allowed"));
        assertThrows(Request.Bad.class, () -> Args.uuid("069A79F4-44E9-4726-A5BE-FCA90E38AAF5"), "uuids are lowercase");
        assertEquals(Duration.ofMinutes(30), Args.duration("30m"));
        assertEquals(Duration.ofDays(7), Args.duration("7d"));
        assertThrows(Request.Bad.class, () -> Args.duration("1h30m"), "units don't combine");
        assertEquals(List.of("kid", "builder"), Args.list("kid,builder"));
        assertThrows(Request.Bad.class, () -> Args.list("kid, builder"));
        assertThrows(Request.Bad.class, () -> Args.list("kid,,builder"));
        assertEquals("kid", Args.choice("kid", List.of("kid", "builder")));
        assertThrows(Request.Bad.class, () -> Args.choice("admin", List.of("kid", "builder")));
    }
}
