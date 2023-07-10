package model;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

@Entity
public class Move implements HasId<Long> {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;

    private int value;

    public Move() {
        value = -1;
    }

    public Move(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
