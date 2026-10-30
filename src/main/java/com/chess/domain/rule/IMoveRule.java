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

/**
 * A special move (castling, en passant, promotion, ...). Each rule owns its own command, so adding a
 * rule means adding a class and registering it in the wiring, without touching Game or other rules.
 */
public interface IMoveRule {

    /** Destinations this rule makes available to the piece at {@code from}, before king-safety filtering. */
    List<Position> extraMoves(MoveRuleContext context, Position from);

    /** True when the requested move must be executed with this rule's command instead of a plain move. */
    boolean appliesTo(MoveRuleContext context, MoveRequest request);

    ICommand createCommand(MoveRuleContext context, MoveRequest request);

    default boolean requiresPromotion(MoveRuleContext context, Position from, Position to) {
        return false;
    }

    /** Pieces a pawn can become under this rule. Only the promotion rule offers any. */
    default List<PieceType> promotionOptions() {
        return List.of();
    }
}