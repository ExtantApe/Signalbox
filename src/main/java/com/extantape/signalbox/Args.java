package com.extantape.signalbox;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/** The wire forms of each argument type (spec: "Argument types"). Each parser throws {@link Request.Bad} with plain words. */
public final class Args {
    private Args() {}

    public static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
    public static final Pattern UUID_FORM = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    public static final Pattern ENUM_VALUE = Pattern.compile("[a-z0-9-]+");
    private static final Pattern INT = Pattern.compile("-?[0-9]{1,10}");
    private static final Pattern DURATION = Pattern.compile("([0-9]{1,9})([smhd])");

    public static boolean bool(String v) throws Request.Bad {
        return switch (v) {
            case "true" -> true;
            case "false" -> false;
            default -> throw bad("Use true or false, not " + v + ".");
        };
    }

    public static int integer(String v) throws Request.Bad {
        if (!INT.matcher(v).matches()) throw bad("Use a whole number, not " + v + ".");
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { throw bad("That number is too big: " + v + "."); }
    }

    /** A player: a name or a lowercase hyphenated uuid. The caller looks it up; answers always give the uuid. */
    public static String player(String v) throws Request.Bad {
        if (NAME.matcher(v).matches() || UUID_FORM.matcher(v).matches()) return v;
        throw bad("That isn't a Minecraft name or a uuid: " + v + ".");
    }

    public static UUID uuid(String v) throws Request.Bad {
        if (!UUID_FORM.matcher(v).matches()) throw bad("Use a lowercase uuid with hyphens, not " + v + ".");
        return UUID.fromString(v);
    }

    public static String choice(String v, List<String> values) throws Request.Bad {
        if (!values.contains(v)) throw bad("Use one of " + String.join(", ", values) + ", not " + v + ".");
        return v;
    }

    /** One whole number and one unit, s, m, h or d. Units don't combine. */
    public static Duration duration(String v) throws Request.Bad {
        var m = DURATION.matcher(v);
        if (!m.matches()) throw bad("Use a number and one unit, s, m, h or d, such as 30m or 7d, not " + v + ".");
        long n = Long.parseLong(m.group(1));
        return switch (m.group(2)) {
            case "s" -> Duration.ofSeconds(n);
            case "m" -> Duration.ofMinutes(n);
            case "h" -> Duration.ofHours(n);
            default -> Duration.ofDays(n);
        };
    }

    /** Comma-separated, no spaces, no empty values. */
    public static List<String> list(String v) throws Request.Bad {
        if (v.isEmpty() || v.contains(" ")) throw bad("A list is values separated by commas, with no spaces.");
        List<String> out = Arrays.asList(v.split(",", -1));
        if (out.stream().anyMatch(String::isEmpty)) throw bad("A list can't have an empty value.");
        return out;
    }

    private static Request.Bad bad(String message) {
        return new Request.Bad(Errors.BAD_ARGUMENT, message, null);
    }
}
