package com.chess.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Game implements IGameService {

    private final Board board;
    private final ICheckDetector checkDetector;
    private final IMoveHistory moveHistory;
    private final IGameEndEvaluator endEvaluator;
    private final IMoveGenerator moveGenerator;
    private final IMoveCodeParser codeParser;
    private IGameState currentState;
    private CastlingRights castlingRights;
    private DrawCounters drawCounters;

    public Game(
            Board board,
            IGameState initialState,
            ICheckDetector checkDetector,
            IMoveHistory moveHistory,
            IGameEndEvaluator endEvaluator,
            IMoveGenerator moveGenerator,
            IMoveCodeParser codeParser) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.currentState = Objects.requireNonNull(initialState, "initialState must not be null");
        this.checkDetector = Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        this.moveHistory = Objects.requireNonNull(moveHistory, "moveHistory must not be null");
        this.endEvaluator = Objects.requireNonNull(endEvaluator, "endEvaluator must not be null");
        this.moveGenerator = Objects.requireNonNull(moveGenerator, "moveGenerator must not be null");
        this.codeParser = Objects.requireNonNull(codeParser, "codeParser must not be null");
        this.castlingRights = CastlingRights.initial(board.size());
        this.drawCounters = DrawCounters.initial(currentPositionKey(null));
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
        MoveRequest request = codeParser.parse(moveCode);
        requirePromotionMatches(request, moveCode);
        play(request);
    }

    @Override
    public boolean requiresPromotion(Position from, Position to) {
        return moveGenerator.requiresPromotion(ruleContext(), from, to);
    }

    @Override
    public void undoLastMove() {
        IMoveHistory.Entry entry = moveHistory.undoLast();
        currentState = entry.previousState();
        castlingRights = entry.previousCastlingRights();
        drawCounters = entry.previousDrawCounters();
    }

    @Override
    public void redoLastMove() {
        IMoveHistory.Entry entry = moveHistory.redoLast();
        applyMove(entry.command());
        updateEndState();
    }

    @Override
    public List<Position> legalMoves(Position from) {
        if (isFinished()) {
            return List.of();
        }
        return moveGenerator.legalMoves(ruleContext(), turn(), from);
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
        return currentState.activePlayerColor();
    }

    @Override
    public boolean isInCheck(Color color) {
        return checkDetector.isInCheck(board, color);
    }

    @Override
    public boolean isFinished() {
        return currentState.isFinished();
    }

    @Override
    public GameResult result() {
        return currentState.result();
    }

    @Override
    public List<String> moveNotation() {
        return moveHistory.commands().stream().map(ICommand::notation).toList();
    }

    @Override
    public List<String> moveCodes() {
        return moveHistory.commands().stream().map(ICommand::moveCode).toList();
    }

    @Override
    public List<Piece> capturedPieces() {
        return moveHistory.commands().stream().map(ICommand::capturedPiece).filter(Objects::nonNull).toList();
    }

    private void play(MoveRequest request) {
        requireOwnPieceAt(request.from());
        if (!legalMoves(request.from()).contains(request.to())) {
            throw new IllegalArgumentException("Illegal movement from " + request.from() + " to " + request.to());
        }
        ICommand command = moveGenerator.createCommand(ruleContext(), request);
        IGameState previousState = currentState;
        CastlingRights previousRights = castlingRights;
        DrawCounters previousCounters = drawCounters;
        applyMove(command);
        moveHistory.record(command, previousState, previousRights, previousCounters);
        updateEndState();
    }

    private void requirePromotionMatches(MoveRequest request, String moveCode) {
        boolean promotes = requiresPromotion(request.from(), request.to());
        if (promotes != (request.promotionType() != null)) {
            throw new IllegalArgumentException("The promotion does not match the move code: " + moveCode);
        }
    }

    private void applyMove(ICommand command) {
        currentState = currentState.handleMove(command);
        castlingRights = castlingRights.after(command);
        drawCounters = drawCounters.after(command, currentPositionKey(command));
    }

    private PositionKey currentPositionKey(ICommand lastMove) {
        return PositionKey.of(board, turn(), castlingRights, lastMove);
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

    private MoveRuleContext ruleContext() {
        return new MoveRuleContext(board, checkDetector, moveHistory.lastCommand(), castlingRights);
    }

    private void updateEndState() {
        Optional<IGameState> endState = endEvaluator.evaluate(board, turn(), drawCounters, this::legalMoves);
        endState.ifPresent(state -> currentState = state);
    }
}