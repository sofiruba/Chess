package com.chess.ui;

import javafx.scene.control.ChoiceDialog;
import com.chess.domain.PieceType;

import java.util.List;

public final class PromotionDialog {
    public PieceType choosePiece() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Reina", List.of("Reina", "Torre", "Alfil", "Caballo"));
        dialog.setTitle("Promoción");
        dialog.setHeaderText("Elegí la pieza para promocionar");
        return switch (dialog.showAndWait().orElse("Reina")) {
            case "Torre" -> PieceType.ROOK;
            case "Alfil" -> PieceType.BISHOP;
            case "Caballo" -> PieceType.KNIGHT;
            default -> PieceType.QUEEN;
        };
    }
}
