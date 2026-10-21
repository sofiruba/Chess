package com.chess.domain;

public final class TurnStateFactory implements ITurnStateFactory {

    @Override
    public IGameState forColor(Color color) {
        return color == Color.WHITE ? new WhiteTurnState(this) : new BlackTurnState(this);
    }

    @Override
    public IGameState checkmate(Color checkedPlayer) {
        return new CheckmateState(checkedPlayer);
    }

    @Override
    public IGameState stalemate(Color playerWithoutMoves) {
        return new StalemateState(playerWithoutMoves);
    }

    @Override
    public IGameState draw(Color playerToMove) {
        return new DrawState(playerToMove);
    }
}
