package at.rseiler.spbee.demo.entity;

import at.rseiler.spbee.core.annotation.Entity;
import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.demo.McName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Entity
public class User {

    private int id;
    private String name;
    private List<Permission> permissions;

    private User() {
    }

    @MappingConstructor
    public User(int id, String name) {
        this(id, name, new ArrayList<>());
    }

    @MappingConstructor(McName.SIMPLE_USER)
    public User(int id) {
        this(id, "-", new ArrayList<>());
    }

    public User(int id, String name, List<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", permissions=" + permissions +
                '}';
    }

    public static class Builder {

        private final User user;

        public Builder() {
            this.user = new User();
        }

        public Builder(User user) {
            this.user = new User();
            this.user.id = user.id;
            this.user.name = user.name;
            this.user.permissions = user.permissions;
        }

        public Builder id(int id) {
            user.id = id;
            return this;
        }

        public Builder name(String name) {
            user.name = name;
            return this;
        }

        public Builder permissions(List<Permission> permissions) {
            user.permissions = permissions;
            return this;
        }

        public User build() {
            return user;
        }
    }

}
