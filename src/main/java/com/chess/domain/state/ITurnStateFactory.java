package com.chess.domain.state;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

public interface ITurnStateFactory {
    IGameState forColor(Color color);
    IGameState checkmate(Color checkedPlayer);
    IGameState stalemate(Color playerWithoutMoves);
    IGameState draw(Color playerToMove);
}
