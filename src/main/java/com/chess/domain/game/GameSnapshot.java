package com.chess.domain.game;

import java.util.Objects;

import com.chess.domain.command.IMoveInfo;
import com.chess.domain.draw.DrawCounters;
import com.chess.domain.draw.PositionKey;
import com.chess.domain.model.Board;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.power.PowerState;
import com.chess.domain.rule.CastlingRights;
import com.chess.domain.state.IGameState;

/**
 * Todo lo que cambia con cada jugada y debe recuperarse al deshacer: el estado de la partida, los derechos de
 * enroque, los contadores de tablas y los poderes (cajas, poderes en mano, piezas congeladas e inmunes). Es inmutable, por lo que el historial guarda una por jugada y deshacer solo
 * tiene que restaurarla.
 */
public record GameSnapshot(
        IGameState state,
        CastlingRights castlingRights,
        DrawCounters drawCounters,
        PowerState powers) {

    public GameSnapshot {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
        Objects.requireNonNull(drawCounters, "drawCounters must not be null");
        Objects.requireNonNull(powers, "powers must not be null");
    }

    /** Una posición sin cajas ni poderes, como en el ajedrez clásico. */
    public GameSnapshot(IGameState state, CastlingRights castlingRights, DrawCounters drawCounters) {
        this(state, castlingRights, drawCounters, PowerState.none());
    }

    public static GameSnapshot initial(Board board, IGameState state, PieceRoles roles) {
        return initial(board, state, roles, PowerState.none());
    }

    public static GameSnapshot initial(Board board, IGameState state, PieceRoles roles, PowerState powers) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(roles, "roles must not be null");
        Objects.requireNonNull(powers, "powers must not be null");
        CastlingRights rights = CastlingRights.initial(board.size(), roles);
        PositionKey start = PositionKey.initial(board, state.activePlayerColor(), rights, powers.signature(board));
        return new GameSnapshot(state, rights, DrawCounters.initial(start, roles), powers);
    }

    /**
     * @param move la jugada ya ejecutada en el tablero
     * @param board el tablero después de esa jugada
     * @param nextState el estado que gestionó la jugada
     */
    public GameSnapshot after(IMoveInfo move, Board board, IGameState nextState) {
        Objects.requireNonNull(nextState, "nextState must not be null");
        CastlingRights rights = castlingRights.after(move);
        PowerState nextPowers = powers.after(move, board);
        PositionKey position = PositionKey.of(
                board, nextState.activePlayerColor(), rights, move, nextPowers.signature(board));
        return new GameSnapshot(nextState, rights, drawCounters.after(move, position), nextPowers);
    }

    public GameSnapshot withState(IGameState newState) {
        return new GameSnapshot(newState, castlingRights, drawCounters, powers);
    }
}