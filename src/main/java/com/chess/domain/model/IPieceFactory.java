package com.chess.domain.model;



public interface IPieceFactory {

    Piece create(Color color, PieceType type);
}
