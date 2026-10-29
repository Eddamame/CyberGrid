package io.github.cybergrid.simulation.app.game.ui;

import java.util.List;

public final class ModalContent {
    private final String title;
    private final List<String> lines;

    public ModalContent(String title, List<String> lines) {
        this.title = title;
        this.lines = List.copyOf(lines);
    }

    public String getTitle() {
        return title;
    }

    public List<String> getLines() {
        return lines;
    }
}


