package com.chess.domain;

/**
 * Creates the command for a plain move. Special moves are created by their own {@link IMoveRule}.
 */
public interface IMoveCommandFactory {

    ICommand create(Board board, Position from, Position to);
}