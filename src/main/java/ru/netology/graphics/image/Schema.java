package ru.netology.graphics.image;

public class Schema implements TextColorSchema {
    private final char[] symbols;

    public Schema(char[] symbols) {
        this.symbols = symbols;
    }

    @Override
    public char convert(int color) {
        int i = (int) Math.round((double)(color * (symbols.length - 1) / 255));
        return symbols[i];
    }
}
