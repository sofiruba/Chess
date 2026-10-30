package com.chess.domain.rule;



/**
 * Turns a persisted move code ("e2e4", "e7e8q") back into a move request for the game.
 */
public interface IMoveCodeParser {

    /**
     * @throws IllegalArgumentException if the text is not a well-formed move code
     */
    MoveRequest parse(String moveCode);
}
