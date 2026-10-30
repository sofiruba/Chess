package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.GameSetup;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.draw.IDrawRule;
import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.port.IGameService;

/**
 * Una regla de tablas que existe solo en este archivo se suma desde el GameSetup, sin tocar la fábrica ni el
 * evaluador.
 */
final class DrawRuleExtensionTest {

    private static final IDrawRule PIECE_ON_E4 = (board, counters) -> board.pieceAt(at("e4")) != null;

    @Test
    void aDrawRuleDefinedOnlyInTheTestEndsTheGame() {
        IGameService game = gameWithPieceOnE4Rule();

        game.playMove(at("e2"), at("e4"));

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    @Test
    void theGameKeepsGoingWhileTheExtraRuleDoesNotApply() {
        IGameService game = gameWithPieceOnE4Rule();

        game.playMove(at("d2"), at("d4"));

        assertFalse(game.isFinished());
        assertEquals(GameResult.IN_PROGRESS, game.result());
    }

    @Test
    void theStandardDrawRulesStillApplyNextToTheExtraOne() {
        IGameService game = TestGames.fromDiagram(
                Color.WHITE,
                "k.......",
                "........",
                "........",
                "........",
                "........",
                "........",
                "........",
                "K.......");

        assertEquals(GameResult.DRAW, game.result());
    }

    private IGameService gameWithPieceOnE4Rule() {
        GameSetup setup = GameSetup.standard().withAdditionalDrawRules(List.of(PIECE_ON_E4));
        return new StandardGameFactory(new DefaultMoveCommandFactory(), setup).create();
    }
}
