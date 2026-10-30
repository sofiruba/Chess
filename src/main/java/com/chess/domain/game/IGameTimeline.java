package com.chess.domain.game;

import java.util.List;
import java.util.Optional;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Board;
import com.chess.domain.state.IGameState;

/**
 * La línea de tiempo de una partida: la posición actual y el camino que llevó hasta ella. Quien la usa avanza,
 * deshace y rehace jugadas sin tener que sincronizar a mano el historial con la posición.
 */
public interface IGameTimeline {

    /** La posición en la que está la partida ahora. */
    GameSnapshot current();

    /**
     * Ejecuta el comando en la posición actual y hace actual a la posición resultante. Las jugadas deshechas que
     * todavía se podían rehacer se descartan.
     */
    void advance(ICommand command, Board board);

    /**
     * Revierte la última jugada y recupera la posición anterior.
     *
     * @throws IllegalStateException si no hay jugadas para deshacer
     */
    void undo();

    /**
     * Vuelve a jugar la última jugada deshecha.
     *
     * @throws IllegalStateException si no hay jugadas para rehacer
     */
    void redo(Board board);

    /** Cambia solo el estado de la posición actual, por ejemplo cuando la partida termina. */
    void replaceState(IGameState state);

    /** Vista de solo lectura de la última jugada ejecutada; vacía antes de la primera jugada. */
    Optional<IMoveInfo> lastMove();

    /** Vista de solo lectura de las jugadas ejecutadas, primero las más antiguas. */
    List<IMoveInfo> moves();
}