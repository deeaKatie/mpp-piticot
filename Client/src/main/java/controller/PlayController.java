package controller;

import dto.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Configuration;
import model.User;
import services.IObserver;
import services.IServices;
import services.ServiceException;
import utils.MessageAlert;

import java.io.IOException;
import java.util.Random;
import java.util.TimerTask;


public class PlayController implements IObserver {

    private IServices service;
    private User loggedUser;
    @FXML
    Label usernameLabel;
    @FXML
    Label statusLabel;
    @FXML
    Label gameConfigLabel;
    @FXML
    Button logOutButton;
    @FXML
    Button makeActionButton;
    @FXML
    Button startGameButton;
    @FXML
    Label otherPlayerLabel;


    public void setService(IServices service) {
        this.service = service;
    }

    public void setUser(User user) {
        this.loggedUser = user;
    }


    public void init_WaitScreen() {
        statusLabel.setVisible(true);
        statusLabel.setText("Waiting for other players...");
        makeActionButton.setVisible(false);
        startGameButton.setVisible(false);
        gameConfigLabel.setVisible(false);
    }

    public void init_PlayScreen() {
        statusLabel.setVisible(false);
        startGameButton.setVisible(false);
        makeActionButton.setVisible(true);
        gameConfigLabel.setVisible(true);
    }

    public void init_StartScreen() {
        usernameLabel.setText("Hi, " + loggedUser.getUsername() + " " + loggedUser.getId());
        statusLabel.setVisible(true);
        statusLabel.setText("Click Button to Start!");
        startGameButton.setVisible(true);
        makeActionButton.setVisible(false);
        gameConfigLabel.setVisible(false);
    }

    public void setConfig(Configuration config) {
        gameConfigLabel.setText(config.getConfig());
        gameConfigLabel.setVisible(true);
    }

    @FXML
    public void logOutHandler() throws IOException {
        System.out.println("Logging out!\n");
        try {
            service.logout(loggedUser);
        } catch (ServiceException ex) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Error logging out", ex.getMessage());
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/LogInView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) logOutButton.getScene().getWindow();
        LogInController logCtrl = fxmlLoader.getController();

        logCtrl.setService(service);
        stage.setScene(scene);
    }

    @Override
    public void update(UpdateDTO updateDTO) {
        Platform.runLater(() -> {
            setConfig(updateDTO.getConfig());
            statusLabel.setText("rolled: " + updateDTO.getRolledNumber());
            statusLabel.setVisible(true);
        });
    }

    public void makeAction(ActionEvent actionEvent) {
        try {
            ActionDTO action = new ActionDTO();
            action.setUser(loggedUser);
            Random ran = new Random();
            int x = ran.nextInt(3) + 1;
            System.out.println("Rolled number: " + x);
            action.setRolledNumber(x);
            makeActionButton.setVisible(false);
            service.madeAction(action);
        } catch (ServiceException e) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Error making action", e.getMessage());
        }
    }

    public void startGame(ActionEvent actionEvent) {
        try {
            StartGameDTO startGameDTO = new StartGameDTO();
            startGameDTO.setUser(loggedUser);

            Boolean status = service.startGame(startGameDTO);
            if (!status) {
                init_WaitScreen();
            }
        } catch (ServiceException e) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Error starting game", e.getMessage());
        }


    }

    @Override
    public void gameStarted(GameDTO gameDTO) {
        Platform.runLater(() -> {
            init_PlayScreen();
            setConfig(gameDTO.getConfig());
            otherPlayerLabel.setText("Other player: " + gameDTO.getOtherPlayerId());
            makeActionButton.setVisible(false);
        });

    }

    @Override
    public void yourTurn() {
        Platform.runLater(() -> {
            System.out.println("Your turn!");
            makeActionButton.setVisible(true);
        });
    }

    @Override
    public void gameEndedLost(GameDTO gameDTO) {

        // Do game ended stuff

        Platform.runLater(() -> {

            statusLabel.setText("You lost!");

            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // Return to start screen after 1 second
                    Platform.runLater(() -> {
                        init_StartScreen();
                    });
                }
            }, 2000);
        });
    }

    @Override
    public void gameEndedWon(GameDTO data) {
        // Do game ended stuff

        Platform.runLater(() -> {

            statusLabel.setText("You won!");

            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // Return to start screen after 1 second
                    Platform.runLater(() -> {
                    init_StartScreen();
                    });
                }
            }, 2000);
        });
    }
}
