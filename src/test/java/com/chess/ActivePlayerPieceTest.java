package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.chess.domain.port.IGameService;

final class ActivePlayerPieceTest {

    @Test
    void onlyThePiecesOfThePlayerToMoveBelongToThem() {
        IGameService game = TestGames.standard();

        assertTrue(game.belongsToActivePlayer(at("e2")));
        assertFalse(game.belongsToActivePlayer(at("e7")));
    }

    @Test
    void anEmptySquareBelongsToNobody() {
        IGameService game = TestGames.standard();

        assertFalse(game.belongsToActivePlayer(at("e4")));
    }

    @Test
    void theAnswerChangesWithTheTurnAndFollowsUndo() {
        IGameService game = TestGames.standard();
        TestGames.play(game, "e2e4");

        assertTrue(game.belongsToActivePlayer(at("e7")));
        assertFalse(game.belongsToActivePlayer(at("e2")));

        game.undoLastMove();

        assertTrue(game.belongsToActivePlayer(at("e2")));
    }
}