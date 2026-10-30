package com.chess.domain.draw;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

/**
 * Draw when the position just reached has occurred three times. It is applied automatically, without a claim.
 */
public final class ThreefoldRepetitionRule implements IDrawRule {

    private static final int REQUIRED_OCCURRENCES = 3;

    @Override
    public boolean isDraw(Board board, DrawCounters counters) {
        return counters.timesCurrentPositionSeen() >= REQUIRED_OCCURRENCES;
    }
}
