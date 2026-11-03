package com.chess.domain.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;
import com.chess.domain.port.HeldPower;
import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameService;
import com.chess.domain.port.MoveHistoryEntry;
import com.chess.domain.power.IPowerRules;
import com.chess.domain.power.PowerCode;
import com.chess.domain.power.PowerContext;
import com.chess.domain.power.PowerState;
import com.chess.domain.rule.ICheckDetector;
import com.chess.domain.rule.IMoveCodeParser;
import com.chess.domain.rule.IMoveGenerator;
import com.chess.domain.rule.MoveRequest;
import com.chess.domain.rule.MoveRuleContext;
import com.chess.domain.state.IGameEndEvaluator;

public final class Game implements IGameService {

    private final Board board;
    private final ICheckDetector checkDetector;
    private final IGameTimeline timeline;
    private final IGameEndEvaluator endEvaluator;
    private final IMoveGenerator moveGenerator;
    private final IMoveCodeParser codeParser;
    private final IPowerRules powerRules;
    private final String setupCode;
    private final CopyOnWriteArrayList<IGameListener> listeners = new CopyOnWriteArrayList<>();

    public Game(
            Board board,
            ICheckDetector checkDetector,
            IGameTimeline timeline,
            IGameEndEvaluator endEvaluator,
            IMoveGenerator moveGenerator,
            IMoveCodeParser codeParser,
            IPowerRules powerRules,
            String setupCode) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.checkDetector = Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        this.timeline = Objects.requireNonNull(timeline, "timeline must not be null");
        this.endEvaluator = Objects.requireNonNull(endEvaluator, "endEvaluator must not be null");
        this.moveGenerator = Objects.requireNonNull(moveGenerator, "moveGenerator must not be null");
        this.codeParser = Objects.requireNonNull(codeParser, "codeParser must not be null");
        this.powerRules = Objects.requireNonNull(powerRules, "powerRules must not be null");
        this.setupCode = Objects.requireNonNull(setupCode, "setupCode must not be null");
        updateEndState();
    }

    @Override
    public void playMove(Position from, Position to) {
        play(new MoveRequest(from, to, null));
    }

    @Override
    public void playMove(Position from, Position to, PieceType promotionType) {
        play(new MoveRequest(from, to, promotionType));
    }

    @Override
    public void playMoveCode(String moveCode) {
        if (PowerCode.isPowerCode(moveCode)) {
            playPowerCode(moveCode);
            return;
        }
        MoveRequest request = codeParser.parse(moveCode);
        requirePromotionMatches(request, moveCode);
        play(request);
    }

    @Override
    public void usePower(Position holder, Position target) {
        Objects.requireNonNull(holder, "holder must not be null");
        Objects.requireNonNull(target, "target must not be null");
        requireOwnPieceAt(holder);
        if (!powerTargets(holder).contains(target)) {
            throw new IllegalArgumentException("Illegal power use from " + holder + " on " + target);
        }
        ICommand command = powerRules.createCommand(powerContext(), holder, target);
        timeline.advance(command, board);
        updateEndState();
        notifyListeners(IGameListener::powerUsed);
        notifyIfFinished();
    }

    @Override
    public boolean requiresPromotion(Position from, Position to) {
        return moveGenerator.requiresPromotion(ruleContext(), from, to);
    }

    @Override
    public List<PieceType> promotionOptions() {
        return moveGenerator.promotionOptions();
    }

    @Override
    public void undoLastMove() {
        timeline.undo();
        notifyListeners(IGameListener::moveUndone);
    }

    @Override
    public void redoLastMove() {
        timeline.redo(board);
        updateEndState();
        notifyListeners(IGameListener::moveRedone);
        notifyIfFinished();
    }

    @Override
    public void addListener(IGameListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        listeners.addIfAbsent(listener);
    }

    @Override
    public void removeListener(IGameListener listener) {
        listeners.remove(listener);
    }

    @Override
    public List<Position> legalMoves(Position from) {
        return timeline.current().state().legalMoves(ruleContext(), from);
    }

    @Override
    public int boardSize() {
        return board.size();
    }

    @Override
    public Position kingInCheckPosition() {
        return checkDetector.kingInCheckPosition(board, turn());
    }

    @Override
    public Piece pieceAt(Position position) {
        return board.pieceAt(position);
    }

    @Override
    public Color turn() {
        return timeline.current().state().activePlayerColor();
    }

    @Override
    public boolean belongsToActivePlayer(Position position) {
        Piece piece = board.pieceAt(position);
        return piece != null && piece.color() == turn();
    }

    @Override
    public boolean isInCheck(Color color) {
        return checkDetector.isInCheck(board, color);
    }

    @Override
    public boolean isFinished() {
        return timeline.current().state().isFinished();
    }

    @Override
    public GameResult result() {
        return timeline.current().state().result();
    }

    @Override
    public List<String> moveNotation() {
        return timeline.moves().stream().map(IMoveInfo::notation).toList();
    }

    @Override
    public List<String> moveCodes() {
        return timeline.moves().stream().map(IMoveInfo::moveCode).toList();
    }

    @Override
    public List<MoveHistoryEntry> moveHistory() {
        return timeline.moves().stream()
                .map(move -> new MoveHistoryEntry(move.movingPiece(), move.notation()))
                .toList();
    }

    @Override
    public List<Piece> capturedPieces() {
        return timeline.moves().stream()
                .flatMap(move -> move.capturedPieces().stream())
                .map(IMoveInfo.CapturedPiece::piece)
                .toList();
    }

    @Override
    public List<Position> mysteryBoxes() {
        return powers().boxPositions();
    }

    @Override
    public PowerType powerAt(Position position) {
        Piece piece = board.pieceAt(position);
        return piece == null ? null : powers().powerOf(piece);
    }

    @Override
    public List<HeldPower> heldPowers(Color color) {
        Objects.requireNonNull(color, "color must not be null");
        List<HeldPower> held = new ArrayList<>();
        for (Position position : board.positions()) {
            Piece piece = board.pieceAt(position);
            PowerType power = piece == null ? null : powers().powerOf(piece);
            if (power != null && piece.color() == color) {
                held.add(new HeldPower(piece, position, power));
            }
        }
        return List.copyOf(held);
    }

    @Override
    public boolean isFrozen(Position position) {
        Piece piece = board.pieceAt(position);
        return piece != null && powers().isFrozen(piece);
    }

    @Override
    public boolean isImmune(Position position) {
        Piece piece = board.pieceAt(position);
        return piece != null && powers().isImmune(piece);
    }

    @Override
    public List<Position> powerTargets(Position holder) {
        return timeline.current().state().powerTargets(powerContext(), holder);
    }

    @Override
    public String setupCode() {
        return setupCode;
    }

    private void play(MoveRequest request) {
        requireOwnPieceAt(request.from());
        if (!legalMoves(request.from()).contains(request.to())) {
            throw new IllegalArgumentException("Illegal movement from " + request.from() + " to " + request.to());
        }
        ICommand command = moveGenerator.createCommand(ruleContext(), request);
        timeline.advance(command, board);
        updateEndState();
        notifyListeners(IGameListener::moveMade);
        notifyIfFinished();
    }

    private void notifyIfFinished() {
        if (isFinished()) {
            notifyListeners(IGameListener::gameFinished);
        }
    }

    private void notifyListeners(Consumer<IGameListener> event) {
        listeners.forEach(event);
    }

    private void requirePromotionMatches(MoveRequest request, String moveCode) {
        boolean promotes = requiresPromotion(request.from(), request.to());
        if (promotes != (request.promotionType() != null)) {
            throw new IllegalArgumentException("The promotion does not match the move code: " + moveCode);
        }
    }

    private void requireOwnPieceAt(Position from) {
        Piece piece = board.pieceAt(from);
        if (piece == null) {
            throw new IllegalArgumentException("There is no piece at " + from);
        }
        if (piece.color() != turn()) {
            throw new IllegalArgumentException("It is " + turn().name().toLowerCase() + "'s turn");
        }
    }

    private void playPowerCode(String moveCode) {
        MoveRequest request = codeParser.parse(PowerCode.squares(moveCode));
        if (request.promotionType() != null) {
            throw new IllegalArgumentException("A power code has no promotion: " + moveCode);
        }
        usePower(request.from(), request.to());
    }

    private PowerState powers() {
        return timeline.current().powers();
    }

    private MoveRuleContext ruleContext() {
        return new MoveRuleContext(
                board, checkDetector, timeline.lastMove(), timeline.current().castlingRights(), powers());
    }

    private PowerContext powerContext() {
        return new PowerContext(ruleContext(), powers());
    }

    /** Todo lo que puede hacer la pieza de esa casilla: sus jugadas y los objetivos de su poder. */
    private List<Position> legalActions(Position from) {
        List<Position> moves = legalMoves(from);
        List<Position> targets = powerTargets(from);
        if (targets.isEmpty()) {
            return moves;
        }
        List<Position> actions = new ArrayList<>(moves);
        actions.addAll(targets);
        return actions;
    }

    /**
     * La partida termina en mate o ahogado solo si el jugador no tiene jugadas ni usos de poder legales: un poder
     * que salva al rey evita el mate.
     */
    private void updateEndState() {
        endEvaluator
                .evaluate(board, turn(), timeline.current().drawCounters(), this::legalActions)
                .ifPresent(timeline::replaceState);
    }
}