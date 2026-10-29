package com.chess.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.Color;
import com.chess.domain.PieceType;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 * Shows one button per promotion option. The options come from the game and the symbols from {@link PieceSymbols},
 * so a new promotion piece needs no change here.
 */
public final class PromotionDialog implements IPromotionChooser {

    private static final String BUTTON_STYLE = "-fx-font-size: 28;";

    private final PieceSymbols symbols;

    public PromotionDialog(PieceSymbols symbols) {
        this.symbols = Objects.requireNonNull(symbols, "symbols must not be null");
    }

    @Override
    public PieceType choosePiece(Color color, List<PieceType> options) {
        Objects.requireNonNull(color, "color must not be null");
        Objects.requireNonNull(options, "options must not be null");
        if (options.isEmpty()) {
            throw new IllegalArgumentException("There are no promotion options to choose from");
        }
        Map<ButtonType, PieceType> choices = new LinkedHashMap<>();
        Dialog<PieceType> dialog = createDialog();
        for (PieceType option : options) {
            addButton(dialog, choices, option, color);
        }
        dialog.setResultConverter(choices::get);
        return dialog.showAndWait().orElse(options.get(0));
    }

    private Dialog<PieceType> createDialog() {
        Dialog<PieceType> dialog = new Dialog<>();
        dialog.setTitle("Promoción");
        dialog.setHeaderText("Elegí la pieza para promocionar");
        return dialog;
    }

    private void addButton(Dialog<PieceType> dialog, Map<ButtonType, PieceType> choices, PieceType option,
            Color color) {
        ButtonType button = new ButtonType(symbols.symbolFor(option, color), ButtonData.OK_DONE);
        choices.put(button, option);
        dialog.getDialogPane().getButtonTypes().add(button);
        dialog.getDialogPane().lookupButton(button).setStyle(BUTTON_STYLE);
    }
}