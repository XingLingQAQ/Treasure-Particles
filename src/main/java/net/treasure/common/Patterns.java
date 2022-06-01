package net.treasure.common;

import java.util.regex.Pattern;

public final class Patterns {

    // GENERAL
    public static final Pattern SPACE = Pattern.compile(" ");
    public static final Pattern TILDE = Pattern.compile("~");

    // EFFECTS
    public static final Pattern VARIABLE = Pattern.compile("(?<name>[a-zA-Z\\d]+)(?:=)(?<default>[\\d.-]+)");
    public static final Pattern EVAL = Pattern.compile("^([a-zA-Z\\d]+)(\\X?)=(.+)$");
    public static final Pattern SCRIPT = Pattern.compile("(?:\\[|(?<=\\,))(?<type>\\w+)(?:=)(?<value>[a-zA-Z0-9{}=*.;_-]+)(?:(?=\\,)|\\])");
    public static final Pattern OFFSET = Pattern.compile("(\\{|(?<=;))(?<type>x|y|z)(?:=)(?<value>[a-zA-Z\\d{}.]+)(?:(?=;)|})");
    public static final Pattern COLOR = Pattern.compile("(|(?<=;))(?<type>name|revertWhenDone|speed)(?:=)(?<value>[a-zA-Z\\d{}.]+)(?:(?=;)|)");

    // CONDITIONS
    public static final Pattern CONDITIONAL = Pattern.compile("(?<condition>.+) (?:\\?) (?<first>.+) (?:\\:) (?<second>.+)");
}