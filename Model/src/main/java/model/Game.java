package model;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Game implements HasId<Long> {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;
    @OneToMany
    private List<User> players;
    @OneToOne
    private Configuration initConfig;
    @OneToOne
    private Configuration currentConfig;
    @OneToMany
    private List<PlayerMoves> playerMoves;
    @OneToOne
    private User currentPLayer;


    public Game() {
        initConfig = new Configuration("");
        currentConfig = new Configuration("");
        players = new ArrayList<>();
        playerMoves = new ArrayList<>();
        currentPLayer = new User();
    }

    public Game(Configuration initConfig) {
        this.initConfig = initConfig;
        players = new ArrayList<>();
        playerMoves = new ArrayList<>();
        currentPLayer = new User();
    }

    public User getCurrentPLayer() {
        return currentPLayer;
    }

    public void setCurrentPLayer(User currentPLayer) {
        this.currentPLayer = currentPLayer;
    }

    public Configuration getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(Configuration currentConfig) {
        this.currentConfig = currentConfig;
    }

    public void addPlayerPosition(PlayerMoves playerPosition) {
        playerMoves.add(playerPosition);
    }

    public List<PlayerMoves> getPlayerMoves() {
        return playerMoves;
    }

    public void setPlayerMoves(List<PlayerMoves> playerMoves) {
        this.playerMoves = playerMoves;
    }


    public Configuration getInitConfig() {
        return initConfig;
    }

    public void setInitConfig(Configuration initConfig) {
        this.initConfig = initConfig;
    }

    public void addPlayer(User player) {
        players.add(player);
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setPlayers(List<User> players) {
        this.players = players;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
