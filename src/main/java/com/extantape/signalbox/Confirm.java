package com.extantape.signalbox;

import java.util.List;
import java.util.Set;

/** Confirm questions as data (spec: "Confirm questions"). */
public final class Confirm {
    private Confirm() {}

    public record Question(String id, String question) {}

    /** The questions still unanswered by {@code yes}. If this isn't empty, the command must change nothing. */
    public static List<Question> missing(List<Question> needed, Set<String> yes) {
        return needed.stream().filter(q -> !yes.contains(q.id())).toList();
    }
}
