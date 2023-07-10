package dto;

import model.Configuration;
import model.HasId;
import model.User;

public class UpdateDTO implements HasId<Long> {

//    private ListItemsDTO entities;

    private User user; // the user that made the move
    private int rolledNumber; // the number rolled

    private Configuration config; // the new config

    public UpdateDTO() {
        config = new Configuration("");
    }


//    public ListItemsDTO getEntities() {
//        return entities;
//    }
//
//    public void setEntities(ListItemsDTO entities) {
//        this.entities = entities;
//    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getRolledNumber() {
        return rolledNumber;
    }

    public void setRolledNumber(int rolledNumber) {
        this.rolledNumber = rolledNumber;
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
