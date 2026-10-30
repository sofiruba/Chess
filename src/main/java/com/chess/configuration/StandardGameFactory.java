package com.chess.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.chess.domain.command.IMoveCommandFactory;
import com.chess.domain.draw.FiftyMoveRule;
import com.chess.domain.draw.IDrawRule;
import com.chess.domain.draw.InsufficientMaterialRule;
import com.chess.domain.draw.ThreefoldRepetitionRule;
import com.chess.domain.game.Game;
import com.chess.domain.game.GameSnapshot;
import com.chess.domain.game.GameTimeline;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.IPieceFactory;
import com.chess.domain.model.PieceFactory;
import com.chess.domain.model.StandardBoardSetup;
import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameService;
import com.chess.domain.rule.CastlingRule;
import com.chess.domain.rule.CheckDetector;
import com.chess.domain.rule.EnPassantRule;
import com.chess.domain.rule.ICheckDetector;
import com.chess.domain.rule.IMoveGenerator;
import com.chess.domain.rule.IMoveRule;
import com.chess.domain.rule.MoveCodeParser;
import com.chess.domain.rule.MoveGenerator;
import com.chess.domain.rule.PromotionRule;
import com.chess.domain.state.DefaultGameEndEvaluator;
import com.chess.domain.state.ITurnStateFactory;
import com.chess.domain.state.TurnStateFactory;

/**
 * Arma una partida a partir de un {@link GameSetup}. Es el único lugar que conoce las reglas concretas: cambiar
 * piezas, tamaño del tablero, promoción o reglas extra es cambiar el GameSetup, sin tocar Game ni las reglas.
 */
public final class StandardGameFactory implements IGameFactory {

    private static final int FIFTY_MOVE_LIMIT = 50;

    private final GameSetup setup;
    private final IPieceFactory pieceFactory;
    private final StandardBoardSetup boardSetup;
    private final ICheckDetector checkDetector;
    private final IMoveGenerator moveGenerator;
    private final ITurnStateFactory stateFactory;

    public StandardGameFactory(IMoveCommandFactory commandFactory) {
        this(commandFactory, GameSetup.standard());
    }

    public StandardGameFactory(IMoveCommandFactory commandFactory, GameSetup setup) {
        Objects.requireNonNull(commandFactory, "commandFactory must not be null");
        this.setup = Objects.requireNonNull(setup, "setup must not be null");
        this.pieceFactory = new PieceFactory(setup.pieceDefinitions());
        this.boardSetup = new StandardBoardSetup(pieceFactory, setup.backRank(), setup.roles());
        this.checkDetector = new CheckDetector(setup.roles());
        this.moveGenerator = new MoveGenerator(commandFactory, checkDetector, moveRules());
        this.stateFactory = new TurnStateFactory(moveGenerator);
    }

    @Override
    public IGameService create() {
        return create(boardSetup.create(), Color.WHITE);
    }

    public IGameService create(Board board, Color firstPlayer) {
        GameSnapshot initial = GameSnapshot.initial(board, stateFactory.forColor(firstPlayer), setup.roles());
        return new Game(
                board,
                checkDetector,
                new GameTimeline(initial),
                new DefaultGameEndEvaluator(checkDetector, stateFactory, drawRules()),
                moveGenerator,
                new MoveCodeParser(setup.promotionTypes()));
    }

    private List<IMoveRule> moveRules() {
        List<IMoveRule> rules = new ArrayList<>();
        rules.add(new CastlingRule(setup.roles()));
        rules.add(new EnPassantRule(setup.roles()));
        rules.add(new PromotionRule(pieceFactory, setup.promotionTypes(), setup.roles()));
        rules.addAll(setup.additionalMoveRules());
        return rules;
    }

    private List<IDrawRule> drawRules() {
        List<IDrawRule> rules = new ArrayList<>();
        rules.add(new InsufficientMaterialRule(setup.roles()));
        rules.add(new ThreefoldRepetitionRule());
        rules.add(new FiftyMoveRule(FIFTY_MOVE_LIMIT));
        rules.addAll(setup.additionalDrawRules());
        return rules;
    }
}