package com.chess.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceType;

import javafx.scene.image.Image;

final class PieceArtwork {
    private static final Map<PieceType, String> PIECE_NAMES = Map.of(
            PieceType.KING, "king",
            PieceType.QUEEN, "queen",
            PieceType.ROOK, "rook",
            PieceType.BISHOP, "bishop",
            PieceType.KNIGHT, "knight",
            PieceType.PAWN, "pawn");

    private final Map<String, Image> images = new HashMap<>();

    Optional<Image> imageFor(Piece piece) {
        String pieceName = PIECE_NAMES.get(piece.type());
        if (pieceName == null) {
            return Optional.empty();
        }

        String colorName = piece.color() == Color.WHITE ? "white" : "black";
        String path = "/com/chess/ui/assets/" + colorName + "-" + pieceName + ".png";
        Image image = images.computeIfAbsent(path, this::loadImage);
        return Optional.of(image);
    }

    private Image loadImage(String path) {
        var resource = PieceArtwork.class.getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Missing chess piece artwork: " + path);
        }
        Image image = new Image(resource.toExternalForm(), 58, 58, true, true);
        if (image.isError()) {
            throw new IllegalStateException("Could not load chess piece artwork: " + path, image.getException());
        }
        return image;
    }
}
