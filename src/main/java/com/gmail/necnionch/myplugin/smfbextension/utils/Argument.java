package com.gmail.necnionch.myplugin.smfbextension.utils;

public class Argument {
    private String[] args;
    public final int length;

    public Argument(String[] args) {
        this.args = args;
        this.length = args.length;
    }

    public String get(int index, String def) {
        if (0 <= index && index < args.length)
            return args[index];
        return def;
    }

    public boolean equal(int index, String value) {
        return value.equalsIgnoreCase(get(index, null));
    }

}
