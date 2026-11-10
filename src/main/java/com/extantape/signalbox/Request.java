package com.extantape.signalbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 * One command's arguments, split the way the spec says: flags last ({@code --yes a,b} and {@code --json <id>}, in either
 * order, after every argument and any quoted free text), then plain tokens, then optional free text in double quotes.
 * A request with {@code --json} is machine-readable and gets exactly one answer line; without it the command answers
 * people as it always did.
 */
public final class Request {
    public static final Pattern ID = Pattern.compile("[a-z0-9-]{1,36}");
    private static final Pattern NEGATIVE_INT = Pattern.compile("-[0-9]{1,18}");
    private static final Pattern YES_ID = Pattern.compile("[a-z0-9-]{1,64}");

    private final String id;
    private final List<String> tokens;
    private final String freeText;
    private final Set<String> yes;

    private Request(String id, List<String> tokens, String freeText, Set<String> yes) {
        this.id = id;
        this.tokens = tokens;
        this.freeText = freeText;
        this.yes = yes;
    }

    /** The caller's id, or null when the request isn't machine-readable. */
    public String id() { return id; }
    public boolean json() { return id != null; }
    public List<String> tokens() { return tokens; }
    /** The quoted free text, unescaped, or null if there was none. */
    public String freeText() { return freeText; }
    public Set<String> yes() { return yes; }

    /** Thrown with a base error code and plain words, ready to answer with. */
    public static final class Bad extends Exception {
        public final String code;
        public final String id; // the id, if one could be read, so the error can still be matched
        Bad(String code, String message, String id) {
            super(message);
            this.code = code;
            this.id = id;
        }
    }

    public static Request parse(String[] args) throws Bad {
        List<String> rest = new ArrayList<>(List.of(args));
        String id = null;
        Set<String> yes = new LinkedHashSet<>();
        // Flags come last, in either order.
        for (int round = 0; round < 2; round++) {
            int n = rest.size();
            if (n >= 2 && rest.get(n - 2).equals("--json")) {
                if (id != null) throw new Bad(Errors.BAD_ARGUMENT, "--json was given twice.", id);
                String candidate = rest.get(n - 1);
                if (!ID.matcher(candidate).matches()) throw new Bad(Errors.BAD_ARGUMENT, "The request id must be 1 to 36 of a-z, 0-9 and -.", null);
                id = candidate;
                rest.subList(n - 2, n).clear();
            } else if (n >= 2 && rest.get(n - 2).equals("--yes")) {
                if (!yes.isEmpty()) throw new Bad(Errors.BAD_ARGUMENT, "--yes was given twice.", id);
                for (String y : rest.get(n - 1).split(",", -1)) {
                    if (!YES_ID.matcher(y).matches()) throw new Bad(Errors.BAD_ARGUMENT, "--yes takes question ids separated by commas, with no spaces.", id);
                    yes.add(y);
                }
                rest.subList(n - 2, n).clear();
            }
        }
        // Free text: from the first token that starts with a quote to the end.
        String free = null;
        int q = -1;
        for (int i = 0; i < rest.size(); i++) if (rest.get(i).startsWith("\"")) { q = i; break; }
        if (q >= 0) {
            String raw = String.join(" ", rest.subList(q, rest.size()));
            free = unquote(raw, id);
            rest = new ArrayList<>(rest.subList(0, q));
        }
        for (String t : rest) {
            // A negative whole number is a value (the spec's int type); anything else starting with "-" could be a flag.
            if (t.startsWith("-") && !NEGATIVE_INT.matcher(t).matches()) throw new Bad(Errors.BAD_ARGUMENT, "A value can't start with \"-\": " + t, id);
            if (t.equals("--json") || t.equals("--yes")) throw new Bad(Errors.BAD_ARGUMENT, "--json and --yes go after every other argument.", id);
        }
        return new Request(id, List.copyOf(rest), free, Set.copyOf(yes));
    }

    private static String unquote(String raw, String id) throws Bad {
        if (raw.length() < 2 || !raw.endsWith("\"")) throw new Bad(Errors.BAD_ARGUMENT, "Free text must be in double quotes and come last.", id);
        StringBuilder out = new StringBuilder();
        for (int i = 1; i < raw.length() - 1; i++) {
            char c = raw.charAt(i);
            if (c == '\\') {
                if (i + 1 >= raw.length() - 1) throw new Bad(Errors.BAD_ARGUMENT, "Free text ends with a lone backslash.", id);
                char next = raw.charAt(++i);
                if (next != '"' && next != '\\') throw new Bad(Errors.BAD_ARGUMENT, "In free text, only \\\" and \\\\ are escapes.", id);
                out.append(next);
            } else if (c == '"') {
                throw new Bad(Errors.BAD_ARGUMENT, "A quote inside free text must be written \\\".", id);
            } else if (c < 0x20) {
                throw new Bad(Errors.BAD_ARGUMENT, "Free text can't contain control characters.", id);
            } else out.append(c);
        }
        return out.toString();
    }
}
