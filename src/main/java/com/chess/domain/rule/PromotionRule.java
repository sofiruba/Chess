package com.chess.domain.rule;

import java.util.List;
import java.util.Objects;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.PromotionCommand;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.IPieceFactory;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;

public final class PromotionRule implements IMoveRule {

    private final IPieceFactory pieceFactory;
    private final List<PieceType> promotionTypes;
    private final PieceRoles roles;

    public PromotionRule(IPieceFactory pieceFactory, List<PieceType> promotionTypes, PieceRoles roles) {
        this.pieceFactory = Objects.requireNonNull(pieceFactory, "pieceFactory must not be null");
        Objects.requireNonNull(promotionTypes, "promotionTypes must not be null");
        this.roles = Objects.requireNonNull(roles, "roles must not be null");
        if (promotionTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one promotion type is required");
        }
        this.promotionTypes = List.copyOf(promotionTypes);
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
                && piece.type() == roles.pawn()
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