package com.chess.domain.state;

import com.chess.domain.model.*;

import com.chess.domain.command.*;

import com.chess.domain.rule.*;


import java.util.List;

/**
 * State of a game: either a player's turn or a finished game. Every implementation honors this contract, so the game
 * can use any of them without asking which one it holds.
 */
public interface IGameState {

    /** The player to move, or the player who would have moved when the game is over. Never null. */
    Color activePlayerColor();

    /** True exactly when {@link #result()} is not {@link GameResult#IN_PROGRESS}. */
    boolean isFinished();

    GameResult result();

    /**
     * Executes the move and returns the state that follows it.
     * <ul>
     *   <li>A turn state throws {@link NullPointerException} for a null move and
     *   {@link IllegalArgumentException} when the move belongs to the other player. In both cases the move is not
     *   executed.</li>
     *   <li>A finished state throws {@link IllegalStateException} for any move and never executes it.</li>
     * </ul>
     */
    IGameState handleMove(ICommand move);

    /**
     * Legal destinations for the piece at {@code from}. Never null; empty when the game is over.
     */
    List<Position> legalMoves(MoveRuleContext context, Position from);
}
