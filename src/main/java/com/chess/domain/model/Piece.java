package com.chess.domain.model;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class Piece {

    private final Color color;
    private final PieceType type;
    private final List<IMoveStrategy> moveStrategies;

    public Piece(Color color, PieceType type, List<IMoveStrategy> moveStrategies) {
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(moveStrategies, "moveStrategies must not be null");
        if (moveStrategies.isEmpty() || moveStrategies.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("A piece requires non-null movement strategies");
        }
        this.moveStrategies = List.copyOf(moveStrategies);
    }

    public Color color() {
        return color;
    }

    public PieceType type() {
        return type;
    }

    public List<Position> getValidMoves(Board board, Position currentPosition) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(currentPosition, "currentPosition must not be null");
        var moves = new HashSet<Position>();
        for (IMoveStrategy strategy : moveStrategies) {
            moves.addAll(strategy.getValidMoves(board, currentPosition));
        }
        return List.copyOf(new ArrayList<>(moves));
    }

    public List<Position> getAttackedMoves(Board board, Position currentPosition) {
        var moves = new HashSet<Position>();
        for (IMoveStrategy strategy : moveStrategies) {
            moves.addAll(strategy.getAttackedMoves(board, currentPosition));
        }
        return List.copyOf(new ArrayList<>(moves));
    }
}