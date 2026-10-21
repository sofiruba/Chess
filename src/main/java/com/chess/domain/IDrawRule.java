package com.chess.domain;

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
