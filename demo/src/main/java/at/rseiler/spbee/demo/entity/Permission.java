package at.rseiler.spbee.demo.entity;

import at.rseiler.spbee.core.annotation.Entity;

/**
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Entity
public class Permission {

    private final String name;
    private final int value;

    public Permission(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

}
