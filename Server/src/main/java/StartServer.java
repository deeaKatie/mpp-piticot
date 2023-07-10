import model.User;
import repository.*;
import service.Service;
import services.IServices;
import utils.AbstractServer;
import utils.RpcConcurrentServer;
import utils.ServerException;

import java.io.IOException;
import java.util.Properties;

public class StartServer {
    private static int defaultPort = 55555;

    public static void main(String[] args) {

        // Server properties (port)
        Properties serverProps=new Properties();
        try {
            serverProps.load(StartServer.class.getResourceAsStream("server.properties"));
            System.out.println("Server properties set. ");
            serverProps.list(System.out);
        } catch (IOException var21) {
            System.err.println("Cannot find server.properties " + var21);
            return;
        }
        int serverPort = defaultPort;
        try {
            serverPort = Integer.parseInt(serverProps.getProperty("server.port"));
        } catch (NumberFormatException ex) {
            System.err.println("Wrong  Port Number" + ex.getMessage());
            System.err.println("Using default port " + defaultPort);
        }


        // Initialize repositories
        IUserRepository userRepository = new UserDBRepository();
        IGameDBRepository gameDBRepository = new GameDBRepository();
        IMoveRepository positionRepository = new MoveDBRepository();
        IConfigurationRepository configurationRepository = new ConfigurationDBRepository();
        IPlayerMovesRepository playerPositionsRepository = new PlayerMovesDBRepository();
        // Add / Show data
        //addData(userRepository);
        showData(userRepository);

        // Initialize service
        IServices service=new Service(userRepository, gameDBRepository, positionRepository,
                configurationRepository, playerPositionsRepository);

        // Start server
        System.out.println("Starting server on port: " + serverPort);
        AbstractServer server = new RpcConcurrentServer(serverPort, service);
        try {
            server.start();
        } catch (ServerException ex) {
            System.err.println("Error starting the server" + ex.getMessage());
        } finally {
            try {
                server.stop();
            } catch (ServerException ex) {
                System.err.println("Error stopping server " + ex.getMessage());
            }

        }
    }

    private static void addData(IUserRepository userRepository) {
        System.out.println("SERVER -> Adding data");
        userRepository.add(new User("sam", "sam"));
        userRepository.add(new User("a", "a"));
        userRepository.add(new User("b", "b"));
    }

    private static void showData(IUserRepository userRepository) {
        System.out.println("SERVER -> Showing data");
        System.out.println("Users:");
        for (User user : userRepository.getAll()) {
            System.out.println(user);
        }
        System.out.println("Done printing users");
    }


}
