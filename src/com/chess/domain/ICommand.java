package com.chess.domain;

public interface ICommand {
    void execute();
    void undo();
}
