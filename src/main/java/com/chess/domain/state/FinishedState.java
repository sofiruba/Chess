package com.chess.domain.state;

import java.util.List;
import java.util.Objects;

import com.chess.domain.command.ICommand;
import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.model.Position;
import com.chess.domain.rule.MoveRuleContext;

/**
 * La partida terminó. El jaque mate, las tablas por ahogado y las tablas por regla solo difieren en el resultado y
 * la razón, por lo que comparten esta clase.
 */
public final class FinishedState implements IGameState {

    private final Color player;
    private final GameResult result;
    private final String reason;

    /**
     * @param player el jugador que habría movido
     * @param reason por qué terminó la partida, en inglés, usado en el error de una jugada posterior ("checkmate")
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
