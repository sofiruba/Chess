package com.chess.domain;

/**
 * A move that can be executed and undone on the board. Everything that only reads a move uses {@link IMoveInfo}.
 */
public interface ICommand extends IMoveInfo {

    void execute();

    void undo();
}