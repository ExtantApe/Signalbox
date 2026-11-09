package com.extantape.signalbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Secrets never go to the console or a log (spec: "Secrets"). A secret goes to {@code <dataFolder>/once/<id>.json},
 * folder 0700 and file 0600, written atomically. The reader deletes it after reading; anything left is deleted after
 * five minutes, and the whole folder is cleared at startup.
 */
public final class OnceFiles {
    public static final Duration LIFETIME = Duration.ofMinutes(5);
    private final Path dir;

    public OnceFiles(Path dataFolder) { this.dir = dataFolder.resolve("once"); }

    /** Writes the secret and returns the path to put in the answer, relative to the data folder. */
    public String write(String id, String kind, String value, Long expiresUnixSeconds) throws IOException {
        if (!Request.ID.matcher(id).matches()) throw new IllegalArgumentException("bad id");
        prepareDir();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("kind", kind);
        m.put("value", value);
        m.put("expires", expiresUnixSeconds);
        Path tmp = Files.createTempFile(dir, id, ".tmp", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        Files.writeString(tmp, Json.write(m));
        Files.move(tmp, dir.resolve(id + ".json"), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        return "once/" + id + ".json";
    }

    /** Deletes once-files older than five minutes. Call it now and then, and at startup with {@link #clearAll()}. */
    public int sweep(Instant now) throws IOException {
        if (!Files.isDirectory(dir)) return 0;
        int n = 0;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.toList()) {
                if (Files.getLastModifiedTime(f).toInstant().plus(LIFETIME).isBefore(now)) { Files.deleteIfExists(f); n++; }
            }
        }
        return n;
    }

    public void clearAll() throws IOException {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.toList()) Files.deleteIfExists(f);
        }
    }

    private void prepareDir() throws IOException {
        if (!Files.isDirectory(dir)) {
            Files.createDirectories(dir.getParent());
            Files.createDirectory(dir, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
        } else {
            Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwx------"));
        }
    }

    public Path dir() { return dir; }
}
