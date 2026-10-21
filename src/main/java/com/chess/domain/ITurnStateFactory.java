package com.chess.domain;

public interface ITurnStateFactory {
    IGameState forColor(Color color);
    IGameState checkmate(Color checkedPlayer);
    IGameState stalemate(Color playerWithoutMoves);
    IGameState draw(Color playerToMove);
}
