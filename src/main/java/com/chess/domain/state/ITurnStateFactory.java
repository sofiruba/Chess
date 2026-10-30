package com.chess.domain.state;

import com.chess.domain.model.*;


public interface ITurnStateFactory {
    IGameState forColor(Color color);
    IGameState checkmate(Color checkedPlayer);
    IGameState stalemate(Color playerWithoutMoves);
    IGameState draw(Color playerToMove);
}
