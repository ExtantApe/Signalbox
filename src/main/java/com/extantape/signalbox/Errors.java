package com.extantape.signalbox;

/** The base error codes every plugin uses where they fit (spec: "Base error codes"). Plugins may add their own. */
public final class Errors {
    private Errors() {}
    public static final String UNKNOWN_COMMAND = "unknown-command";
    public static final String BAD_ARGUMENT = "bad-argument";
    public static final String NO_PERMISSION = "no-permission";
    public static final String NOT_FOUND = "not-found";
    public static final String NEEDS_ONLINE_PLAYER = "needs-online-player";
    public static final String BUSY = "busy";
    public static final String INTERNAL = "internal";
}
