package utils;

import model.Configuration;
import model.Game;
import model.Move;
import model.PlayerMoves;

import java.util.Objects;

public class MoveGenerator {
    private Game game;
    private int rolledNr;
    private String gameStatus;
    private Configuration newConfig;
    private String playerNumber;
    private String oponentNumber;
    private PlayerMoves playerMoves;
    private Move move;
    public MoveGenerator(Game game, int rolledNr) {
        this.game = game;
        this.rolledNr = rolledNr;
        newConfig = new Configuration();
        playerNumber = "";
        playerMoves = new PlayerMoves();
        move = new Move();
    }

    public void makeMove() {
        findPlayerData();
        calculateMove();
        updateConfig();
    }

    private void findPlayerData() {
        if (Objects.equals(game.getCurrentPLayer().getId(), game.getPlayers().get(0).getId())) {
            playerNumber = "1";
            oponentNumber = "2";
            playerMoves = game.getPlayerMoves().get(0);
        } else {
            playerNumber = "2";
            oponentNumber = "1";
            playerMoves = game.getPlayerMoves().get(1);
        }
    }

    private void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    private void updateConfig() {
        game.setCurrentConfig(newConfig);
    }

    private void updatePos() {
        char[] string = newConfig.getConfig().toCharArray();
    }

    private void calculateMove() {
        System.out.println("MOVE -> calculationg move for " + game.getCurrentPLayer());
        System.out.println("Player number " + playerNumber);

//        int oldPosition = playerMoves.getMoves().get(playerMoves.getMoves().size() - 1).getValue();
        int oldPosition = game.getCurrentConfig().getConfig().indexOf(playerNumber);
        int newPosition = oldPosition + rolledNr;

        if (newPosition <= game.getCurrentConfig().getConfig().length() - 1) {
            // game still going
            System.out.println("MOVE -> game still going");
            setGameStatus("InProgress");

            if (game.getCurrentConfig().getConfig().charAt(newPosition) == 'X') {
                // move backwards
                System.out.println("MOVE -> X found at " + newPosition + " moving backwards");
                //remove me first from board then find empty pos
                if (oldPosition != -1)
                    removePlayerFromBoard(oldPosition, playerNumber);
                newPosition = findEmptyPosition(newPosition);

                 if (newPosition != -1)  {
                    movePlayerToPosition(newPosition, playerNumber);
                }

            } else if (game.getCurrentConfig().getConfig().charAt(newPosition) == oponentNumber.charAt(0)) {
                // move opponent backwards

                System.out.println("MOVE -> opponent found at " + newPosition + " moving him backwards");

                // move me first
                movePlayerToPosition(newPosition, playerNumber);
                if (oldPosition != -1)
                    removePlayerFromBoard(oldPosition, playerNumber);

                System.out.println("MOVE -> moved me to " + newPosition);
                System.out.println("Config " + newConfig.getConfig());


                // move it
                int opponentNewPosition = findEmptyPosition(newPosition);

                System.out.println("move opponent backwards from " + newPosition + " to " + opponentNewPosition);

                if (opponentNewPosition != -1) {
                    movePlayerToPosition(opponentNewPosition, oponentNumber);
                }

                System.out.println("Config " + newConfig.getConfig());


            } else {
                // move here cause empty
                movePlayerToPosition(newPosition, playerNumber);
                System.out.println("MOVE -> moved me to " + newPosition);
                System.out.println("Config " + newConfig.getConfig());
                // remove my last position
                if (oldPosition != -1) {
                    removePlayerFromBoard(oldPosition, playerNumber);
                    System.out.println("MOVE -> remove my last pos " + oldPosition);
                    System.out.println("Config " + newConfig.getConfig());
                }
            }

        } else {
            // game ended, player won
            setGameStatus("Won");
            removePlayerFromBoard(oldPosition, playerNumber);
        }

        move.setValue(newPosition);
        playerMoves.getMoves().add(move);

    }

    // move me to this posiotn
    private void movePlayerToPosition(int newPosition, String playerNumber) {
        char[] positions = game.getCurrentConfig().getConfig().toCharArray();
        positions[newPosition] = playerNumber.charAt(0);
        newConfig = new Configuration(new String(positions));
        game.setCurrentConfig(newConfig);

//        newConfig = new Configuration(game.getCurrentConfig().getConfig().substring(0, Math.max(0, oldPosition))
//                + "_"
//                + game.getCurrentConfig().getConfig().substring(oldPosition + 1, newPosition)
//                + playerNumber + game.getCurrentConfig().getConfig().substring(newPosition + 1));
    }

    // removes my postion
    private void removePlayerFromBoard(int newPosition, String playerNumber) {
//        newConfig = new Configuration(game.getCurrentConfig().getConfig().substring(0, Math.max(0, oldPosition))
//                + "_"
//                + game.getCurrentConfig().getConfig().substring(oldPosition + 1));
        char[] positions = game.getCurrentConfig().getConfig().toCharArray();
        positions[newPosition] = '_';
        newConfig = new Configuration(new String(positions));
        game.setCurrentConfig(newConfig);
    }

    private int findEmptyPosition(int newPosition) {
        for (int i = newPosition - 1; i >= -1; i--) {
            if (i == -1) {
                // if no empty position found, move to start
                return -1;
            }
            if (game.getCurrentConfig().getConfig().charAt(i) == '_') {
                // if empty position found, move here
                return i;
            }
        }
        return -1;
    }


    public String getGameStatus() {
        return gameStatus;
    }

    public Move getMove() {
        return move;
    }

    public Configuration getConfig() {
        return newConfig;
    }
}
