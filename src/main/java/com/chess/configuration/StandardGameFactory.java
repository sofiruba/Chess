package com.chess.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.Board;
import com.chess.domain.CastlingRule;
import com.chess.domain.CheckDetector;
import com.chess.domain.Color;
import com.chess.domain.DefaultGameEndEvaluator;
import com.chess.domain.DiagonalMoveStrategy;
import com.chess.domain.EnPassantRule;
import com.chess.domain.FiftyMoveRule;
import com.chess.domain.Game;
import com.chess.domain.ICheckDetector;
import com.chess.domain.IDrawRule;
import com.chess.domain.IGameFactory;
import com.chess.domain.IGameService;
import com.chess.domain.IMoveCommandFactory;
import com.chess.domain.IMoveRule;
import com.chess.domain.IMoveStrategy;
import com.chess.domain.IPieceFactory;
import com.chess.domain.ITurnStateFactory;
import com.chess.domain.InsufficientMaterialRule;
import com.chess.domain.LShapeMoveStrategy;
import com.chess.domain.LinearMoveStrategy;
import com.chess.domain.MoveCodeParser;
import com.chess.domain.MoveGenerator;
import com.chess.domain.MoveHistory;
import com.chess.domain.OneStepMoveStrategy;
import com.chess.domain.PawnMoveStrategy;
import com.chess.domain.PieceFactory;
import com.chess.domain.PieceType;
import com.chess.domain.PromotionRule;
import com.chess.domain.StandardBoardSetup;
import com.chess.domain.ThreefoldRepetitionRule;
import com.chess.domain.TurnStateFactory;

/**
 * Wires a standard game. Pieces, back rank, move rules and draw rules are registered here, without touching Game
 * or the existing rules. The board size is the length of the back rank.
 */
public final class StandardGameFactory implements IGameFactory {

    private static final int FIFTY_MOVE_LIMIT = 50;

    private final IMoveCommandFactory commandFactory;
    private final List<IMoveRule> additionalRules;
    private final IPieceFactory pieceFactory;
    private final StandardBoardSetup boardSetup;
    private final PromotionRule promotionRule;
    private final ITurnStateFactory stateFactory = new TurnStateFactory();
    private final ICheckDetector checkDetector = new CheckDetector();

    public StandardGameFactory(IMoveCommandFactory commandFactory) {
        this(commandFactory, List.of());
    }

    public StandardGameFactory(IMoveCommandFactory commandFactory, List<IMoveRule> additionalRules) {
        this(commandFactory, standardPieceDefinitions(), standardBackRank(), additionalRules);
    }

    public StandardGameFactory(
            IMoveCommandFactory commandFactory,
            Map<PieceType, List<IMoveStrategy>> pieceDefinitions,
            List<PieceType> backRank,
            List<IMoveRule> additionalRules) {
        this(commandFactory, pieceDefinitions, backRank, additionalRules, standardPromotionTypes());
    }

    /**
     * @param pieceDefinitions registry of piece type to movement strategies
     * @param backRank pieces of the back rank from column 0; its length is the board size
     * @param promotionTypes pieces a pawn can become, in the order they are offered; the first is the default
     */
    public StandardGameFactory(
            IMoveCommandFactory commandFactory,
            Map<PieceType, List<IMoveStrategy>> pieceDefinitions,
            List<PieceType> backRank,
            List<IMoveRule> additionalRules,
            List<PieceType> promotionTypes) {
        this.commandFactory = Objects.requireNonNull(commandFactory, "commandFactory must not be null");
        this.additionalRules = List.copyOf(Objects.requireNonNull(additionalRules, "additionalRules must not be null"));
        this.pieceFactory = new PieceFactory(pieceDefinitions);
        this.boardSetup = new StandardBoardSetup(pieceFactory, backRank);
        this.promotionRule = new PromotionRule(pieceFactory, promotionTypes);
    }

    public static Map<PieceType, List<IMoveStrategy>> standardPieceDefinitions() {
        Map<PieceType, List<IMoveStrategy>> definitions = new LinkedHashMap<>();
        definitions.put(PieceType.KING, List.of(new OneStepMoveStrategy()));
        definitions.put(PieceType.QUEEN, List.of(new LinearMoveStrategy(), new DiagonalMoveStrategy()));
        definitions.put(PieceType.ROOK, List.of(new LinearMoveStrategy()));
        definitions.put(PieceType.BISHOP, List.of(new DiagonalMoveStrategy()));
        definitions.put(PieceType.KNIGHT, List.of(new LShapeMoveStrategy()));
        definitions.put(PieceType.PAWN, List.of(new PawnMoveStrategy()));
        return definitions;
    }

    public static List<PieceType> standardPromotionTypes() {
        return List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT);
    }

    public static List<PieceType> standardBackRank() {
        return List.of(
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK);
    }

    @Override
    public IGameService create() {
        return create(boardSetup.create(), Color.WHITE);
    }

    public IGameService create(Board board, Color firstPlayer) {
        return new Game(
                board,
                stateFactory.forColor(firstPlayer),
                checkDetector,
                new MoveHistory(),
                new DefaultGameEndEvaluator(checkDetector, stateFactory, drawRules()),
                new MoveGenerator(commandFactory, checkDetector, moveRules()),
                new MoveCodeParser(promotionRule.promotionOptions()));
    }

    private List<IMoveRule> moveRules() {
        List<IMoveRule> rules = new ArrayList<>();
        rules.add(new CastlingRule());
        rules.add(new EnPassantRule());
        rules.add(promotionRule);
        rules.addAll(additionalRules);
        return rules;
    }

    private List<IDrawRule> drawRules() {
        return List.of(
                new InsufficientMaterialRule(),
                new ThreefoldRepetitionRule(),
                new FiftyMoveRule(FIFTY_MOVE_LIMIT));
    }
}
