package dto;

import model.Configuration;
import model.HasId;

// what server sends to user at start game & end game
public class GameDTO implements HasId<Long> {


    private Long otherPlayerId;
    private Configuration config;
    private int rolledNumber;

    public GameDTO() {
        config = new Configuration("");
    }

    public GameDTO(Long otherPlayerId, Configuration config) {
        this.otherPlayerId = otherPlayerId;
        this.config = config;
    }


    public int getRolledNumber() {
        return rolledNumber;
    }

    public void setRolledNumber(int rolledNumber) {
        this.rolledNumber = rolledNumber;
    }

    public Long getOtherPlayerId() {
        return otherPlayerId;
    }

    public void setOtherPlayerId(Long otherPlayerId) {
        this.otherPlayerId = otherPlayerId;
    }


    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long aLong) {

    }
}
