package com.extantape.signalbox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What a plugin offers, for {@code <plugin> describe --json <id>} (spec: "describe"). */
public final class Describe {
    public static final int FORMAT = 1;

    private Describe() {}

    /**
     * One positional argument. Optional ones may be left out, and only from the end: a reader fills slots in order, so
     * an optional argument can never come before a required one ({@link #body} refuses that).
     */
    public record Arg(String name, String type, List<String> values, String of, boolean optional) {
        public static Arg of(String name, String type) { return new Arg(name, type, List.of(), null, false); }
        public static Arg choice(String name, List<String> values) { return new Arg(name, "enum", values, null, false); }
        public static Arg listOf(String name, String of, List<String> values) { return new Arg(name, "list", values, of, false); }

        /** The same argument, marked as one that may be left out. */
        public Arg asOptional() { return new Arg(name, type, values, of, true); }
    }

    public record Command(String name, String permission, List<Arg> args, List<String> confirms, boolean secret) {}

    public static Map<String, Object> body(String plugin, String version, List<Command> commands) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("format", FORMAT);
        m.put("plugin", plugin);
        m.put("version", version);
        List<Object> cmds = new ArrayList<>();
        for (Command c : commands) {
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("name", c.name());
            cm.put("permission", c.permission());
            List<Object> args = new ArrayList<>();
            boolean seenOptional = false;
            for (Arg a : c.args()) {
                if (seenOptional && !a.optional())
                    throw new IllegalArgumentException(c.name() + ": required argument " + a.name() + " comes after an optional one");
                seenOptional |= a.optional();
                Map<String, Object> am = new LinkedHashMap<>();
                am.put("name", a.name());
                am.put("type", a.type());
                if (a.of() != null) am.put("of", a.of());
                if (!a.values().isEmpty()) am.put("values", a.values());
                if (a.optional()) am.put("optional", true); // left out when false, so format 1 readers see nothing new
                args.add(am);
            }
            cm.put("args", args);
            cm.put("confirms", c.confirms());
            cm.put("secret", c.secret());
            cmds.add(cm);
        }
        m.put("commands", cmds);
        return m;
    }
}
