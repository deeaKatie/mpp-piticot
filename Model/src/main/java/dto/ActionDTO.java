package dto;

import model.HasId;
import model.User;


// what user sends server after each move
public class ActionDTO implements HasId<Long> {

    private User user;
    private int rolledNumber;

    public ActionDTO() {
    }

    public int getRolledNumber() {
        return rolledNumber;
    }

    public void setRolledNumber(int rolledNumber) {
        this.rolledNumber = rolledNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long aLong) {

    }
}
