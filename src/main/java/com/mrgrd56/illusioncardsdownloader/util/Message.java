package com.mrgrd56.illusioncardsdownloader.util;

import java.text.MessageFormat;

public final class Message {
    private Message() {}

    public static String format(String pattern, Object... arguments) {
        return new MessageFormat(pattern).format(arguments);
    }
}
