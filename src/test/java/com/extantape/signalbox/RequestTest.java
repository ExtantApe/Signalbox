package com.extantape.signalbox;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {
    static Request parse(String line) throws Request.Bad { return Request.parse(line.isEmpty() ? new String[0] : line.split(" ")); }

    @Test
    void flagsComeLastInEitherOrder() throws Exception {
        Request r = parse("note Alice \"some text\" --yes a,b --json abc-1");
        assertEquals("abc-1", r.id());
        assertEquals(Set.of("a", "b"), r.yes());
        assertEquals(List.of("note", "Alice"), r.tokens());
        assertEquals("some text", r.freeText());
        assertEquals("abc-1", parse("note Alice --json abc-1 --yes a").id());
    }

    @Test
    void withoutJsonItsForPeople() throws Exception {
        Request r = parse("list");
        assertFalse(r.json());
        assertNull(r.id());
    }

    @Test
    void freeTextEscapesAreOnlyQuoteAndBackslash() throws Exception {
        assertEquals("say \"hi\" \\ ok", parse("note \"say \\\"hi\\\" \\\\ ok\" --json x").freeText());
        assertThrows(Request.Bad.class, () -> parse("note \"bad \\n escape\" --json x"));
        assertThrows(Request.Bad.class, () -> parse("note \"unclosed --json x"));
        assertThrows(Request.Bad.class, () -> parse("note \"inner \" quote\" --json x"));
    }

    @Test
    void aValueCantSmuggleInAFlag() {
        Request.Bad bad = assertThrows(Request.Bad.class, () -> parse("add -yes --json x"));
        assertEquals(Errors.BAD_ARGUMENT, bad.code);
        assertEquals("x", bad.id, "the error can still be matched to the request");
        assertThrows(Request.Bad.class, () -> parse("add --json x Alice"), "flags must come last");
    }

    @Test
    void negativeWholeNumbersAreValues() throws Exception {
        Request r = parse("move gate1 world 45 -60 -12 --json m1");
        assertEquals(List.of("move", "gate1", "world", "45", "-60", "-12"), r.tokens());
        assertThrows(Request.Bad.class, () -> parse("move -60x --json m2"), "only whole numbers");
        assertThrows(Request.Bad.class, () -> parse("move - --json m3"), "a lone dash isn't a number");
        assertThrows(Request.Bad.class, () -> parse("move --60 --json m4"), "two dashes are a flag");
    }

    @Test
    void idsAreChecked() {
        assertThrows(Request.Bad.class, () -> parse("list --json Has_Caps"));
        assertThrows(Request.Bad.class, () -> parse("list --json " + "a".repeat(37)));
        assertDoesNotThrow(() -> parse("list --json " + "0123456789abcdef0123456789abcdef"));
    }
}
