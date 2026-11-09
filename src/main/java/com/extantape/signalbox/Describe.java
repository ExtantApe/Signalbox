package com.extantape.signalbox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What a plugin offers, for {@code <plugin> describe --json <id>} (spec: "describe"). */
public final class Describe {
    public static final int FORMAT = 1;

    private Describe() {}

    public record Arg(String name, String type, List<String> values, String of) {
        public static Arg of(String name, String type) { return new Arg(name, type, List.of(), null); }
        public static Arg choice(String name, List<String> values) { return new Arg(name, "enum", values, null); }
        public static Arg listOf(String name, String of, List<String> values) { return new Arg(name, "list", values, of); }
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
            for (Arg a : c.args()) {
                Map<String, Object> am = new LinkedHashMap<>();
                am.put("name", a.name());
                am.put("type", a.type());
                if (a.of() != null) am.put("of", a.of());
                if (!a.values().isEmpty()) am.put("values", a.values());
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
