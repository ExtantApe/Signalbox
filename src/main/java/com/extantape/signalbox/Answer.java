package com.extantape.signalbox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builds the one answer line for a request: {@code ea-result <id> <json>}, plain ASCII, under 8 KB. Send it with
 * {@code sender.sendMessage(line)} to the sender that ran the command, never through a logger (a logger adds the
 * plugin's name before the prefix, and readers only accept the prefix straight after the log header).
 */
public final class Answer {
    public static final String PREFIX = "ea-result ";
    public static final int MAX_LINE = 8 * 1024;

    private Answer() {}

    public static String ok(String id, Map<String, ?> fields) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("ok", true);
        if (fields != null) fields.forEach((k, v) -> { if (!k.equals("id") && !k.equals("ok")) m.put(k, v); });
        return line(id, m);
    }

    /**
     * One page of a list, in the spec's shape: {@code {"id","ok":true,"items":[...],"next":"<cursor>"|null}}. The cursor
     * is an opaque string the caller passes back as-is; null means this is the last page.
     */
    public static String page(String id, List<?> items, String next) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("items", items);
        m.put("next", next);
        return ok(id, m);
    }

    public static String error(String id, String code, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("ok", false);
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("code", code);
        err.put("message", message);
        m.put("error", err);
        return line(id, m);
    }

    /** A confirm request: nothing was changed; re-send with {@code --yes} and every id listed. */
    public static String needs(String id, List<Confirm.Question> questions) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("ok", false);
        m.put("needs", questions.stream().map(q -> { Map<String, Object> qm = new LinkedHashMap<>(); qm.put("id", q.id()); qm.put("question", q.question()); return qm; }).toList());
        return line(id, m);
    }

    /** An answer whose secret went to a once-file. The secret itself never appears here. */
    public static String secret(String id, String relativeFile, Map<String, ?> fields) {
        Map<String, Object> m = new LinkedHashMap<>(fields == null ? Map.of() : fields);
        m.put("secretFile", relativeFile);
        return ok(id, m);
    }

    private static String line(String id, Map<String, Object> m) {
        String line = PREFIX + id + " " + Json.write(m);
        if (line.length() > MAX_LINE) {
            line = error(id, Errors.INTERNAL, "The answer was too long to send in one line. Ask for fewer items with limit.");
        }
        return line;
    }

    /** Sends one line to whoever asked. */
    public static void send(Consumer<String> to, String line) {
        to.accept(line);
    }
}
