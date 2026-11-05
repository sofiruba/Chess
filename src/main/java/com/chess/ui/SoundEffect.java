package com.chess.ui;

public enum SoundEffect {
    MOVE("move.wav"),
    CAPTURE("capture.wav"),
    CHECK("check.wav"),
    FINISH("finish.wav"),
    POWER("power.wav"),
    BOX("box.wav"),
    PROMOTION("promotion.wav"),
    UNDO("undo.wav"),
    REDO("redo.wav"),
    ERROR("error.wav"),
    SELECT("select.wav"),
    RULES("rules.wav"),
    MENU("menu.wav"),
    NAVIGATE("navigate.wav"),
    SAVE("save.wav"),
    HOME("home.wav");

    private final String fileName;
    
    SoundEffect(String fileName) {
        this.fileName = fileName;
    }

    public String resourcePath() {
        return "/com/chess/ui/assets/sounds/" + fileName;
    }
}
