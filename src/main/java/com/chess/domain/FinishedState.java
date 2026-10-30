package com.chess.domain;

import java.util.List;
import java.util.Objects;

/**
 * The game is over. Checkmate, stalemate and draw by rule differ only in the result and the reason, so they share
 * this class.
 */
public final class FinishedState implements IGameState {

    private final Color player;
    private final GameResult result;
    private final String reason;

    /**
     * @param player the player who would have moved
     * @param reason why the game ended, in English, used in the error for a move after the end ("checkmate")
     */
    public FinishedState(Color player, GameResult result, String reason) {
        this.player = Objects.requireNonNull(player, "player must not be null");
        this.result = Objects.requireNonNull(result, "result must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        if (result == GameResult.IN_PROGRESS) {
            throw new IllegalArgumentException("A finished game cannot be in progress");
        }
    }

    @Override
    public Color activePlayerColor() {
        return player;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public GameResult result() {
        return result;
    }

    @Override
    public IGameState handleMove(ICommand move) {
        throw new IllegalStateException("The game is over by " + reason);
    }

    @Override
    public List<Position> legalMoves(MoveRuleContext context, Position from) {
        return List.of();
    }
}
