package com.chess;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.StandardGameFactory;
import com.chess.persistence.SqliteGameRepository;
import com.chess.ui.ChessApplication;
import com.chess.ui.PieceSymbols;
import com.chess.ui.PromotionDialog;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {

    @Override
    public void start(Stage stage) {
        SqliteGameRepository repository = new SqliteGameRepository("chess.db");
        StandardGameFactory gameFactory = new StandardGameFactory(new DefaultMoveCommandFactory());
        PieceSymbols symbols = PieceSymbols.standard();
        ChessApplication application = new ChessApplication(
                repository,
                gameFactory,
                symbols,
                new PromotionDialog(symbols));
        stage.setTitle("Ajedrez");
        stage.setScene(new Scene(application.createView()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
