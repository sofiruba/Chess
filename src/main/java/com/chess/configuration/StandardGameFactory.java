package com.chess.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Random;
import java.util.random.RandomGenerator;

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
import com.chess.domain.model.Position;
import com.chess.domain.model.IPieceFactory;
import com.chess.domain.model.PieceFactory;
import com.chess.domain.model.StandardBoardSetup;
import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameService;
import com.chess.domain.power.BoxLayout;
import com.chess.domain.power.FeatherPower;
import com.chess.domain.power.IBoxSpawner;
import com.chess.domain.power.IPower;
import com.chess.domain.power.IceFlowerPower;
import com.chess.domain.power.MirroredBoxSpawner;
import com.chess.domain.power.PowerRules;
import com.chess.domain.power.PowerSetup;
import com.chess.domain.power.PowerState;
import com.chess.domain.power.ShellPower;
import com.chess.domain.power.StarPower;
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
 * Arma una partida a partir de un {@link GameSetup}. Es el único lugar que conoce las reglas y los poderes
 * concretos: cambiar piezas, tamaño del tablero, promoción, cajas misteriosas o reglas extra es cambiar el
 * GameSetup, sin tocar Game ni las reglas. El contenido de las cajas se sortea con el {@link RandomGenerator}
 * inyectado, por lo que las pruebas pueden fijarlo.
 */
public final class StandardGameFactory implements IGameFactory {

    private static final int FIFTY_MOVE_LIMIT = 50;

    private final GameSetup setup;
    private final IPieceFactory pieceFactory;
    private final StandardBoardSetup boardSetup;
    private final ICheckDetector checkDetector;
    private final IMoveGenerator moveGenerator;
    private final ITurnStateFactory stateFactory;
    private final PowerRules powerRules;
    private final RandomGenerator random;

    public StandardGameFactory(IMoveCommandFactory commandFactory) {
        this(commandFactory, GameSetup.standard());
    }

    public StandardGameFactory(IMoveCommandFactory commandFactory, GameSetup setup) {
        this(commandFactory, setup, new Random());
    }

    public StandardGameFactory(IMoveCommandFactory commandFactory, GameSetup setup, RandomGenerator random) {
        Objects.requireNonNull(commandFactory, "commandFactory must not be null");
        this.setup = Objects.requireNonNull(setup, "setup must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.pieceFactory = new PieceFactory(setup.pieceDefinitions());
        this.boardSetup = new StandardBoardSetup(pieceFactory, setup.backRank(), setup.roles());
        this.checkDetector = new CheckDetector(setup.roles());
        this.moveGenerator = new MoveGenerator(commandFactory, checkDetector, moveRules());
        this.powerRules = new PowerRules(powers());
        this.stateFactory = new TurnStateFactory(moveGenerator, powerRules);
    }

    @Override
    public IGameService create() {
        return create(boardSetup.create(), Color.WHITE);
    }

    @Override
    public IGameService createWithSetup(String setupCode) {
        return create(boardSetup.create(), Color.WHITE, setupCode);
    }

    /** Una partida desde una posición dada, con el contenido de las cajas sorteado. */
    public IGameService create(Board board, Color firstPlayer) {
        return create(board, firstPlayer, "");
    }

    /**
     * Una partida desde una posición dada.
     *
     * @param setupCode contenido de las cajas ("a5=i,c5=s"); vacío para sortearlo en las casillas del GameSetup
     */
    public IGameService create(Board board, Color firstPlayer, String setupCode) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(setupCode, "setupCode must not be null");
        PowerSetup powerSetup = powerSetup(setupCode);
        BoxLayout boxes = powerSetup.boxes();
        requireBoxesInside(board, boxes);
        PowerState powers = PowerState.initial(boxes.contents(), board, spawner(powerSetup));
        GameSnapshot initial = GameSnapshot.initial(
                board, stateFactory.forColor(firstPlayer), setup.roles(), powers);
        return new Game(
                board,
                checkDetector,
                new GameTimeline(initial),
                new DefaultGameEndEvaluator(checkDetector, stateFactory, drawRules()),
                moveGenerator,
                new MoveCodeParser(setup.promotionTypes()),
                powerRules,
                powerSetup.code());
    }

    /**
     * Un código vacío sortea todo: el contenido de las cajas iniciales y, si aparecen cajas nuevas, su semilla. Un
     * código guardado sin semilla (de antes de que aparecieran cajas nuevas) usa la semilla 0.
     */
    private PowerSetup powerSetup(String setupCode) {
        if (setupCode.isBlank()) {
            BoxLayout boxes = BoxLayout.random(setup.mysteryBoxes(), powerRules.types(), random);
            return new PowerSetup(boxes, setup.boxSpawning().enabled()
                    ? OptionalLong.of(random.nextLong())
                    : OptionalLong.empty());
        }
        PowerSetup parsed = PowerSetup.parse(setupCode, powerRules.types());
        if (setup.boxSpawning().enabled() && parsed.seed().isEmpty()) {
            return new PowerSetup(parsed.boxes(), OptionalLong.of(0));
        }
        return parsed;
    }

    private IBoxSpawner spawner(PowerSetup powerSetup) {
        BoxSpawning spawning = setup.boxSpawning();
        if (!spawning.enabled()) {
            return IBoxSpawner.NONE;
        }
        return new MirroredBoxSpawner(
                spawning.everyTurns(),
                spawning.boxesPerSide(),
                spawning.maxOnBoard(),
                powerRules.types(),
                powerSetup.seed().orElse(0));
    }

    private static void requireBoxesInside(Board board, BoxLayout boxes) {
        for (Position box : boxes.contents().keySet()) {
            if (!board.contains(box)) {
                throw new IllegalArgumentException("Mystery box outside the board: " + box);
            }
        }
    }

    private List<IPower> powers() {
        List<IPower> powers = new ArrayList<>();
        powers.add(new IceFlowerPower(setup.roles()));
        powers.add(new FeatherPower(setup.roles()));
        powers.add(new StarPower(setup.roles()));
        powers.add(new ShellPower(setup.roles()));
        powers.addAll(setup.additionalPowers());
        return powers;
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