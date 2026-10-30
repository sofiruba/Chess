package com.chess;

import java.util.List;

import com.chess.domain.Color;
import com.chess.domain.ICommand;
import com.chess.domain.IMoveGenerator;
import com.chess.domain.MoveRequest;
import com.chess.domain.MoveRuleContext;
import com.chess.domain.PieceType;
import com.chess.domain.Position;

/**
 * Generator for state tests: it answers a fixed list of destinations and remembers who it was asked for.
 */
final class StubMoveGenerator implements IMoveGenerator {

    private final List<Position> destinations;
    private Color askedPlayer;
    private Position askedFrom;

    StubMoveGenerator(List<Position> destinations) {
        this.destinations = List.copyOf(destinations);
    }

    Color askedPlayer() {
        return askedPlayer;
    }

    Position askedFrom() {
        return askedFrom;
    }

    @Override
    public List<Position> legalMoves(MoveRuleContext context, Color player, Position from) {
        askedPlayer = player;
        askedFrom = from;
        return destinations;
    }

    @Override
    public ICommand createCommand(MoveRuleContext context, MoveRequest request) {
        throw new UnsupportedOperationException("Not needed by the state tests");
    }

    @Override
    public boolean requiresPromotion(MoveRuleContext context, Position from, Position to) {
        return false;
    }

    @Override
    public List<PieceType> promotionOptions() {
        return List.of();
    }
}
