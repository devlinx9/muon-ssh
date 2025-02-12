package com.jediterm.terminal;

/** Current cursor shape as described by <a href="https://vt100.net/docs/vt510-rm/DECSCUSR.html">...</a>. */
public enum CursorShape {
    BLINK_BLOCK,
    STEADY_BLOCK,
    BLINK_UNDERLINE,
    STEADY_UNDERLINE,
    BLINK_VERTICAL_BAR,
    STEADY_VERTICAL_BAR
}
