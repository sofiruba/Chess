package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.List;
import java.util.Objects;

public final class PromotionRule implements IMoveRule {

    private final IPieceFactory pieceFactory;
    private final List<PieceType> promotionTypes;

    /**
     * @param promotionTypes allowed pieces; the first one is used when the player does not choose
     */
    public PromotionRule(IPieceFactory pieceFactory, List<PieceType> promotionTypes) {
        this.pieceFactory = Objects.requireNonNull(pieceFactory, "pieceFactory must not be null");
        Objects.requireNonNull(promotionTypes, "promotionTypes must not be null");
        if (promotionTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one promotion type is required");
        }
        this.promotionTypes = List.copyOf(promotionTypes);
    }

    @Override
    public List<Position> extraMoves(MoveRuleContext context, Position from) {
        return List.of();
    }

    @Override
    public boolean appliesTo(MoveRuleContext context, MoveRequest request) {
        return requiresPromotion(context, request.from(), request.to());
    }

    @Override
    public ICommand createCommand(MoveRuleContext context, MoveRequest request) {
        Piece pawn = context.board().pieceAt(request.from());
        Piece promoted = pieceFactory.create(pawn.color(), chosenType(request));
        return new PromotionCommand(context.board(), request.from(), request.to(), promoted);
    }

    @Override
    public boolean requiresPromotion(MoveRuleContext context, Position from, Position to) {
        Piece piece = context.board().pieceAt(from);
        return piece != null
                && piece.type() == PieceType.PAWN
                && to.y() == lastRank(context.board(), piece.color());
    }

    @Override
    public List<PieceType> promotionOptions() {
        return promotionTypes;
    }

    private PieceType chosenType(MoveRequest request) {
        PieceType requested = request.promotionType();
        if (requested == null) {
            return promotionTypes.get(0);
        }
        if (!promotionTypes.contains(requested)) {
            throw new IllegalArgumentException("A pawn cannot be promoted to " + requested);
        }
        return requested;
    }

    private int lastRank(Board board, Color color) {
        return color == Color.WHITE ? board.size() - 1 : 0;
    }
}