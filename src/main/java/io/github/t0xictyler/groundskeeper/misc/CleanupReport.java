package io.github.t0xictyler.groundskeeper.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class CleanupReport {

    @Getter @Setter(AccessLevel.PROTECTED)
    private int stacksCleared = 0, totalItemsCleared = 0;

    public boolean isEmpty() {
        return stacksCleared == 0 && totalItemsCleared == 0;
    }
}
