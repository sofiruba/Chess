package com.chess.domain.port;

import java.util.Objects;

import com.chess.domain.model.Piece;

/** Read-only data needed to display one played move. */
public record MoveHistoryEntry(Piece movingPiece, String notation) {

    public MoveHistoryEntry {
        Objects.requireNonNull(movingPiece, "movingPiece must not be null");
        Objects.requireNonNull(notation, "notation must not be null");
    }
}
