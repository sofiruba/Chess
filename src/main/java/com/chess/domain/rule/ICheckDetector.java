package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

public interface ICheckDetector {

    boolean isInCheck(Board board, Color color);

    /** Returns the position of the king of the given color if it is in check, or null otherwise. */
    Position kingInCheckPosition(Board board, Color color);

    boolean isSquareAttacked(Board board, Position position, Color attackingColor);
}