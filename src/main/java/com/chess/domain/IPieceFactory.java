package com.chess.domain;

public interface IPieceFactory {

    Piece create(Color color, PieceType type);
}
