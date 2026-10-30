package com.chess.domain.command;


/**
 * Una jugada que puede ejecutarse y deshacerse en el tablero. Todo lo que solo lee una jugada usa {@link IMoveInfo}.
 */
public interface ICommand extends IMoveInfo {

    void execute();

    void undo();
}
