package com.chess.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds pieces from a registry of definitions (piece type to movement strategies). The registry is built by the
 * wiring code, so adding a piece never requires editing this class.
 */
public final class PieceFactory implements IPieceFactory {

    private final Map<PieceType, List<IMoveStrategy>> definitions;

    public PieceFactory(Map<PieceType, List<IMoveStrategy>> definitions) {
        Objects.requireNonNull(definitions, "definitions must not be null");
        Map<PieceType, List<IMoveStrategy>> copy = new HashMap<>();
        definitions.forEach((type, strategies) -> copy.put(type, List.copyOf(strategies)));
        this.definitions = Map.copyOf(copy);
    }

    @Override
    public Piece create(Color color, PieceType type) {
        Objects.requireNonNull(color, "color must not be null");
        Objects.requireNonNull(type, "type must not be null");
        List<IMoveStrategy> strategies = definitions.get(type);
        if (strategies == null) {
            throw new IllegalArgumentException("There is no definition registered for piece type " + type);
        }
        return new Piece(color, type, strategies);
    }
}
