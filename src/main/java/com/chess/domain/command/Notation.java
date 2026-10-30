package com.chess.domain.command;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

final class Notation {

    private Notation() {
    }

    static String square(Position position) {
        return String.valueOf((char) ('a' + position.x())) + (position.y() + 1);
    }
}