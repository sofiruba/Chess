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
    private GameSnapshot snapshot;

    public Game(
            Board board,
            GameSnapshot initialSnapshot,
            ICheckDetector checkDetector,
            IMoveHistory moveHistory,
            IGameEndEvaluator endEvaluator,
            IMoveGenerator moveGenerator,
            IMoveCodeParser codeParser) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.snapshot = Objects.requireNonNull(initialSnapshot, "initialSnapshot must not be null");
        this.checkDetector = Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        this.moveHistory = Objects.requireNonNull(moveHistory, "moveHistory must not be null");
        this.endEvaluator = Objects.requireNonNull(endEvaluator, "endEvaluator must not be null");
        this.moveGenerator = Objects.requireNonNull(moveGenerator, "moveGenerator must not be null");
        this.codeParser = Objects.requireNonNull(codeParser, "codeParser must not be null");
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
    public List<PieceType> promotionOptions() {
        return moveGenerator.promotionOptions();
    }

    @Override
    public void undoLastMove() {
        snapshot = moveHistory.undoLast().previous();
    }

    @Override
    public void redoLastMove() {
        applyMove(moveHistory.redoLast().command());
        updateEndState();
    }

    @Override
    public List<Position> legalMoves(Position from) {
        return snapshot.state().legalMoves(ruleContext(), from);
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
        return snapshot.state().activePlayerColor();
    }

    @Override
    public boolean isInCheck(Color color) {
        return checkDetector.isInCheck(board, color);
    }

    @Override
    public boolean isFinished() {
        return snapshot.state().isFinished();
    }

    @Override
    public GameResult result() {
        return snapshot.state().result();
    }

    @Override
    public List<String> moveNotation() {
        return moveHistory.moves().stream().map(IMoveInfo::notation).toList();
    }

    @Override
    public List<String> moveCodes() {
        return moveHistory.moves().stream().map(IMoveInfo::moveCode).toList();
    }

    @Override
    public List<Piece> capturedPieces() {
        return moveHistory.moves().stream().map(IMoveInfo::capturedPiece).filter(Objects::nonNull).toList();
    }

    private void play(MoveRequest request) {
        requireOwnPieceAt(request.from());
        if (!legalMoves(request.from()).contains(request.to())) {
            throw new IllegalArgumentException("Illegal movement from " + request.from() + " to " + request.to());
        }
        ICommand command = moveGenerator.createCommand(ruleContext(), request);
        GameSnapshot previous = snapshot;
        applyMove(command);
        moveHistory.record(command, previous);
        updateEndState();
    }

    private void requirePromotionMatches(MoveRequest request, String moveCode) {
        boolean promotes = requiresPromotion(request.from(), request.to());
        if (promotes != (request.promotionType() != null)) {
            throw new IllegalArgumentException("The promotion does not match the move code: " + moveCode);
        }
    }

    private void applyMove(ICommand command) {
        IGameState nextState = snapshot.state().handleMove(command);
        snapshot = snapshot.after(command, board, nextState);
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
        return new MoveRuleContext(board, checkDetector, moveHistory.lastMove(), snapshot.castlingRights());
    }

    private void updateEndState() {
        Optional<IGameState> endState =
                endEvaluator.evaluate(board, turn(), snapshot.drawCounters(), this::legalMoves);
        endState.ifPresent(state -> snapshot = snapshot.withState(state));
    }
}