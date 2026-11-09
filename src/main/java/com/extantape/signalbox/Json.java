package com.extantape.signalbox;

import java.util.Collection;
import java.util.Map;

/**
 * A tiny JSON writer for answers. Every control character and all non-ASCII is written as a \\uXXXX escape, so an
 * answer is always one line of plain ASCII that no terminal encoding can split (spec: "Line safety").
 */
public final class Json {
    private Json() {}

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(Object v, StringBuilder out) {
        if (v == null) out.append("null");
        else if (v instanceof String s) string(s, out);
        else if (v instanceof Boolean || v instanceof Integer || v instanceof Long) out.append(v);
        else if (v instanceof Number n) out.append(n.toString());
        else if (v instanceof Map<?, ?> m) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) out.append(',');
                first = false;
                string(String.valueOf(e.getKey()), out);
                out.append(':');
                write(e.getValue(), out);
            }
            out.append('}');
        } else if (v instanceof Collection<?> c) {
            out.append('[');
            boolean first = true;
            for (Object o : c) {
                if (!first) out.append(',');
                first = false;
                write(o, out);
            }
            out.append(']');
        } else string(v.toString(), out);
    }

    private static void string(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                default -> {
                    if (c < 0x20 || c > 0x7e) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        out.append('"');
    }
}
