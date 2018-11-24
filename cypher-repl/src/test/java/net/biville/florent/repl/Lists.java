package net.biville.florent.repl;

import java.util.ArrayList;
import java.util.List;

public class Lists {

    private Lists() {
        throw new RuntimeException("static");
    }

    @SafeVarargs
    public static <T> List<T> mutableList(T... elements) {
        return org.assertj.core.util.Lists.newArrayList(elements);
    }
}
