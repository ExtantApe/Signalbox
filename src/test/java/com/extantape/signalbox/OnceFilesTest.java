package com.extantape.signalbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OnceFilesTest {
    @Test
    void writesAnOwnerOnlyFileAndPointsToIt(@TempDir Path data) throws Exception {
        OnceFiles once = new OnceFiles(data);
        String rel = once.write("abc", "invite-code", "K7QX4M", 1_800_000_000L);
        assertEquals("once/abc.json", rel);
        Path f = data.resolve(rel);
        assertEquals("{\"id\":\"abc\",\"kind\":\"invite-code\",\"value\":\"K7QX4M\",\"expires\":1800000000}", Files.readString(f));
        assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(f)));
        assertEquals("rwx------", PosixFilePermissions.toString(Files.getPosixFilePermissions(once.dir())));
        try (var left = Files.list(once.dir())) { assertEquals(1, left.count(), "no temp file left behind"); }
    }

    @Test
    void oldFilesAreSweptAndStartupClearsAll(@TempDir Path data) throws Exception {
        OnceFiles once = new OnceFiles(data);
        once.write("old", "invite-code", "A", null);
        once.write("new", "invite-code", "B", null);
        Files.setLastModifiedTime(data.resolve("once/old.json"), FileTime.from(Instant.now().minusSeconds(301)));
        assertEquals(1, once.sweep(Instant.now()));
        assertFalse(Files.exists(data.resolve("once/old.json")));
        assertTrue(Files.exists(data.resolve("once/new.json")));
        once.clearAll();
        try (var left = Files.list(once.dir())) { assertEquals(0, left.count()); }
    }

    @Test
    void refusesAnIdThatCouldEscapeTheFolder(@TempDir Path data) {
        assertThrows(IllegalArgumentException.class, () -> new OnceFiles(data).write("../x", "k", "v", null));
    }
}
