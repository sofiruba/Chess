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
 * A condition that ends the game in a draw. Each rule is a class of its own and is registered in the wiring,
 * so adding one never touches the evaluator.
 */
public interface IDrawRule {

    /**
     * @param board the position right after the last move
     * @param counters the 50-move clock and the positions seen so far, already updated for that move
     */
    boolean isDraw(Board board, DrawCounters counters);
}
