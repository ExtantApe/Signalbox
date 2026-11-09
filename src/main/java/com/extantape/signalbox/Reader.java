package com.extantape.signalbox;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The reading side of the format, for Java readers and for the tests that pin the rule down (Leaf Juice has its own,
 * the same rule). A line counts only if {@code ea-result } comes straight after the log header's {@code ]: }. Player
 * chat is logged as {@code ]: <Name> …}, so a player can't forge an answer by typing one. Only the first answer for an
 * id counts, and only for ids this reader sent.
 */
public final class Reader {
    private static final Pattern LINE = Pattern.compile("^\\[[^\\]]*\\](?: \\[[^\\]]*\\])?: ea-result ([a-z0-9-]{1,36}) (\\{.*\\})$");
    private final Set<String> waiting = new HashSet<>();

    /** Call before sending a request with this id. */
    public void expect(String id) { waiting.add(id); }

    public record Reply(String id, String json) {}

    /** The reply in this console or log line, if it's a genuine, first answer to an id we sent. */
    public Optional<Reply> accept(String line) {
        var m = LINE.matcher(line);
        if (!m.matches()) return Optional.empty();
        String id = m.group(1);
        if (!waiting.remove(id)) return Optional.empty();
        return Optional.of(new Reply(id, m.group(2)));
    }
}
