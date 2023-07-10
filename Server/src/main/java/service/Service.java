package service;

import dto.*;
import exception.RepositoryException;
import model.*;
import repository.*;
import services.IObserver;
import services.IServices;
import services.ServiceException;
import utils.MoveGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Service implements IServices {

    private IUserRepository userRepository;
    private IGameDBRepository gameDBRepository;
    private IMoveRepository moveRepository;
    private IConfigurationRepository configurationRepository;
    private IPlayerMovesRepository playerPositionsRepository;
    private Map<Long, IObserver> loggedClients; // all logged clients
    private Map<Long, StartGameDTO> waitingClients; // just clients waiting to be matched
    private Map<Long, Long> playingClients; // client id and game id
    private int noOfPlayersInAGame;
    private final int defaultThreadsNo = 5;

    public Service(IUserRepository userRepository, IGameDBRepository gameDBRepository,
                   IMoveRepository moveRepository, IConfigurationRepository configurationRepository,
                   IPlayerMovesRepository playerPositionsRepository) {
        this.userRepository = userRepository;
        this.gameDBRepository = gameDBRepository;
        this.moveRepository = moveRepository;
        this.configurationRepository = configurationRepository;
        this.playerPositionsRepository = playerPositionsRepository;
        this.loggedClients = new ConcurrentHashMap<>();
        this.waitingClients = new ConcurrentHashMap<>();
        this.playingClients = new ConcurrentHashMap<>();
        noOfPlayersInAGame = 2;
    }

    public synchronized User checkLogIn(User user, IObserver client) throws ServiceException {
        //find user
        User userToFind;
        System.out.println("SERVER -> checkLogIn -> " + user);
        try {
            userToFind = userRepository.findUserByUsername(user.getUsername());
        } catch (RepositoryException re) {
            throw new ServiceException(re.getMessage());
        }
        // check if user is already logged in
        if (loggedClients.containsKey(userToFind.getId())) {
            throw new ServiceException("User already logged in.");
        }
        // check if password is correct
        if (Objects.equals(userToFind.getPassword(), user.getPassword())) {
            user.setId(userToFind.getId());
            loggedClients.put(user.getId(), client);
            return userToFind;
        } else {
            throw new ServiceException("Incorrect Password");
        }
    }

    @Override
    public synchronized void logout(User user) throws ServiceException {
        System.out.println("SERVER -> logout");

        //todo during game
        if (loggedClients.containsKey(user.getId())) {
            loggedClients.remove(user.getId());
        } else {
            throw new ServiceException("User not logged in");
        }

    }

    // generate a config for a game
    public Configuration generateConfiguration() {
        Random ran = new Random();
        // we have values between 1 and 9 -> 0 represents start position
        int x = ran.nextInt(9) + 1; //pos of X
        String config = "";
        for (var pos = 1; pos <= 9; pos++) {
            if (pos == x) {
                config += "X";
            } else {
                config += "_";
            }
        }
        return new Configuration(config);
    }

    // Add clients to game
    private void addClientsToGame(List<Long> clientsForThisGame, Game game) {
        for (var clientId : clientsForThisGame) {
            try {
                game.addPlayer(userRepository.findById(clientId));
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    // add the starting positions for the players aka 0
    private void addStartingPositions(Game game, List<Long> clientsForThisGame) {
        Move startPos1 = new Move(-1);
        Move startPos2 = new Move(-1);

        startPos1 = moveRepository.add(startPos1);
        startPos2 = moveRepository.add(startPos2);

        PlayerMoves postionsPlayer1 = new PlayerMoves();
        postionsPlayer1.addPosition(startPos1);
        postionsPlayer1.setUser(game.getPlayers().get(0));

        PlayerMoves postionsPlayer2 = new PlayerMoves();
        postionsPlayer2.addPosition(startPos2);
        postionsPlayer2.setUser(game.getPlayers().get(1));

        playerPositionsRepository.add(postionsPlayer1);
        playerPositionsRepository.add(postionsPlayer2);

        game.addPlayerPosition(postionsPlayer1);
        game.addPlayerPosition(postionsPlayer2);
    }

    @Override
    public Boolean startGame(StartGameDTO startGameDTO) throws ServiceException {
        System.out.println("SERVER -> startGame");

        // if we have enough waiting clients to start a match
        if (waitingClients.size() >= noOfPlayersInAGame - 1) {
            System.out.println("SERVER -> We have players to start a match");

            // get first noOfPlayersInAGame clients
            List<Long> clientsForThisGame = new ArrayList<>();
            for (var client : waitingClients.entrySet()) {
                clientsForThisGame.add(client.getKey());
            }
            clientsForThisGame.add(startGameDTO.getUser().getId());

            //create game config
            Configuration config = generateConfiguration();

            Configuration currentConfig = new Configuration(config.getConfig());

            config = configurationRepository.add(config);
            currentConfig = configurationRepository.add(currentConfig);

            // create game
            Game game = new Game();
            game.setInitConfig(config);
            game.setCurrentConfig(currentConfig);

            // add clients to game
            addClientsToGame(clientsForThisGame, game);

            // add start positions
            addStartingPositions(game, clientsForThisGame);

            // get player ids
            final Long idPlayer1 = game.getPlayers().get(0).getId();
            final Long idPlayer2 = game.getPlayers().get(1).getId();

            try {
            game.setCurrentPLayer(userRepository.findById(idPlayer1));
            } catch (RepositoryException e) {
                e.printStackTrace();
            }

            // save game
            game = gameDBRepository.add(game);

            // add clients to playing list
            for (var clientId : clientsForThisGame) {
                playingClients.put(clientId, game.getId());
                System.out.println("SERVER -> clientId, gameId = " + clientId + ", " + game.getId());
            }

            //remove from waiting list
            waitingClients.clear();

            // notify clients
            ExecutorService executor = Executors.newFixedThreadPool(defaultThreadsNo);



            Configuration finalConfig = config;
            executor.execute(() -> {
                try {
                    System.out.println("SERVER -> Notifying clients mathc has started");

                    GameDTO gameDTO = new GameDTO();
                    gameDTO.setConfig(finalConfig);

                    gameDTO.setOtherPlayerId(idPlayer1);
                    loggedClients.get(idPlayer2).gameStarted(gameDTO);

                    gameDTO.setOtherPlayerId(idPlayer2);
                    loggedClients.get(idPlayer1).gameStarted(gameDTO);

                    loggedClients.get(idPlayer1).yourTurn();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return true;

        }

        // we don't have enough clients to start a match
        // add client to waiting list
        waitingClients.put(startGameDTO.getUser().getId(), startGameDTO);
        return false;

    }


    // gets data from repository in list form
    public synchronized Iterable<User> getListData() throws ServiceException {
        return userRepository.getAll();
    }

    // encapsulates data in DTO
    public synchronized ListItemsDTO getData(User user) throws ServiceException {
        System.out.println("SERVER -> getData");
        ListItemsDTO listItemsDTO = new ListItemsDTO();
        for (var item : getListData()) {
            ListItemDTO listItemDTO = new ListItemDTO();
            listItemDTO.setUser(item);
            listItemsDTO.addItem(listItemDTO);
        }
        return listItemsDTO;


    }

    public synchronized void madeAction(ActionDTO action) throws ServiceException {
        System.out.println("SERVER -> madeAction");

        // Do smth for action
        int rolledNr = action.getRolledNumber();
        System.out.println("SERVER -> rolledNr = " + rolledNr);
        Long userId = action.getUser().getId();
        Long gameId = playingClients.get(userId);
        Game game = null;
        User user = null;

        try {
            game = gameDBRepository.findById(gameId);
            user = userRepository.findById(userId);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage());
        }

        // set current player
        game.setCurrentPLayer(user);

        MoveGenerator moveGen = new MoveGenerator(game, rolledNr);

        moveGen.makeMove();
        switch (moveGen.getGameStatus()) {
            case "InProgress" -> {
                // notify other player
                notifyNextTurnPlayer(game);
                // update all player of new game config
                updatePlayersConfigs(game, rolledNr);
            }
            case "Won" -> {
                //notify winner
                notifyWinner(game, rolledNr);
                //notify losers
                notifyLosers(game, rolledNr);
            }
        }

        // save the new move
        Move move = moveGen.getMove();
        move = moveRepository.add(move);

        //save new config
        Configuration config = moveGen.getConfig();
        configurationRepository.add(config);

        //update game
        gameDBRepository.update(game);



//
//        // calculate new postion
//        if (game.getPlayers().get(0).getId() == userId) {
//            // HE IS PLAYER1
//            System.out.println("SERVER -> Player1 made action");
//            PlayerMoves player1Positions = game.getPlayerMoves().get(0);
//
//            int curretPos = player1Positions.getMoves().get(player1Positions.getMoves().size() - 1).getValue();
//            int newPos = curretPos + rolledNr;
//
//            if (game.getCurrentConfig().getConfig().length() - 1 >= newPos) {
//                // we are still in the game
//
//                if (game.getCurrentConfig().getConfig().charAt(newPos) == 'X') {
//                    // we move to first empty spot
//                    for (var pos = newPos - 1; pos >= -1; pos--) {
//                        if (game.getCurrentConfig().getConfig().charAt(pos) == '_') {
//                            // we move here
//                            Configuration newC = new Configuration(game.getCurrentConfig().getConfig().substring(0, pos) + "X"
//                                    + game.getCurrentConfig().getConfig().substring(pos+1, newPos) + "_"
//                                    + game.getCurrentConfig().getConfig().substring(newPos+1));
//                            configurationRepository.add(newC);
//                            game.setCurrentConfig(newC);
//                            Move move = new Move(newPos);
//                            positionRepository.add(move);
//                            player1Positions.addPosition(move);
//                            gameDBRepository.update(game);
//                            //send next turn to other player
//                            loggedClients.get(game.getPlayers().get(1).getId()).yourTurn();
//                            return;
//                        }
//                        if (pos == -1) {
//                            // we move to first spot
//                            Configuration newC = new Configuration(game.getCurrentConfig().getConfig().substring(0, curretPos) + "_"
//                                    + game.getCurrentConfig().getConfig().substring(curretPos+1));
//                            configurationRepository.add(newC);
//                            game.setCurrentConfig(newC);
//                            Move move = new Move(0);
//                            positionRepository.add(move);
//                            player1Positions.addPosition(move);
//                            gameDBRepository.update(game);
//                            //send next turn to other player
//                            loggedClients.get(game.getPlayers().get(1).getId()).yourTurn();
//                            return;
//                        }
//                    }
//
//                } else if (game.getCurrentConfig().getConfig().charAt(newPos) == '2') {
//                    // other players moves to first empty spot
//
//
//
//                } else if (game.getCurrentConfig().getConfig().charAt(newPos) == '_') {
//                    // we move here
//                    Configuration newC = new
//                            Configuration(game.getCurrentConfig().getConfig().substring(0, newPos) + "1"
//                            + game.getCurrentConfig().getConfig().substring(newPos + 1));
//                    configurationRepository.add(newC);
//                    game.setCurrentConfig(newC);
//                    Move move = new Move(newPos);
//                    positionRepository.add(move);
//                    player1Positions.addPosition(move);
//                    gameDBRepository.update(game);
//                    //send next turn to other player
//                    loggedClients.get(game.getPlayers().get(1).getId()).yourTurn();
//                    return;
//
//                }
//
//            } else {
//                // game ended, we won
//
//            }
//
//        } else {
//            // HE IS PLAYER2
//
//        }


    }




    private void notifyNextTurnPlayer(Game game) {
        boolean found = false;
        for (var player : game.getPlayers()) {
            if (found) {
                loggedClients.get(player.getId()).yourTurn();
                return;
            }
            if (Objects.equals(player.getId(), game.getCurrentPLayer().getId())) {
                found = true;
            }
        }
        // if we are here, we are at the end of the list
        loggedClients.get(game.getPlayers().get(0).getId()).yourTurn();
    }

    private void updatePlayersConfigs(Game game, int rolledNr) {
        ExecutorService executor = Executors.newFixedThreadPool(defaultThreadsNo);
        for (var player : game.getPlayers()) {
            executor.execute(() -> {
                try {
                    UpdateDTO update = new UpdateDTO();
                    update.setConfig(game.getCurrentConfig());
                    update.setRolledNumber(rolledNr);
                    loggedClients.get(player.getId()).update(update);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void notifyWinner(Game game, int rolledNr) {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setConfig(game.getCurrentConfig());
        gameDTO.setRolledNumber(rolledNr);
        System.out.println("SERVER -> notifyWinner");
        System.out.println("winner: " + game.getCurrentPLayer().getId());
        loggedClients.get(game.getCurrentPLayer().getId()).gameEndedWon(gameDTO);
    }

    private void notifyLosers(Game game, int rolledNr) {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setConfig(game.getCurrentConfig());
        gameDTO.setRolledNumber(rolledNr);
        System.out.println("SERVER -> notifyLosers");
        for (var player : game.getPlayers()) {
            System.out.println("loser: " + player.getId());
            if (!Objects.equals(player.getId(), game.getCurrentPLayer().getId())) {
                loggedClients.get(player.getId()).gameEndedLost(gameDTO);
            }
        }

    }


    public synchronized void endGame(GameDTO gameDTO, Map<Long, Boolean> usersStatus) throws ServiceException {
        //Map<Long, Boolean> usersSTatus -> id, status 1 - winner, 0 - loser
        System.out.println("SERVER -> endGame");

        // remove players from playingClients
        for (var player : usersStatus.entrySet()) {
            playingClients.remove(player.getKey());
        }

        // notify players
        ExecutorService executor = Executors.newFixedThreadPool(defaultThreadsNo);
        for (var player : usersStatus.entrySet()) {
            executor.execute(() -> {
                try {
                    if (player.getValue()) {
                        // WINNER
                        loggedClients.get(player.getKey()).gameEndedWon(gameDTO);
                    } else {
                        // LOSER
                        loggedClients.get(player.getKey()).gameEndedLost(gameDTO);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


}
