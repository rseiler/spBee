package at.rseiler.spbee.demo.entity;

import at.rseiler.spbee.core.annotation.Entity;
import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.demo.McName;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Reinhard Seiler {@literal <rseiler.developer@gmail.com>}
 */
@Entity
public class User {

    private int id;
    private String name;
    private LocalDateTime created;
    private List<Permission> permissions;

    private User() {
    }

    @MappingConstructor
    public User(int id, String name, Timestamp created) {
        this(id, name, created, new ArrayList<>());
    }

    @MappingConstructor(McName.SIMPLE_USER)
    public User(int id) {
        this(id, "-", null, new ArrayList<>());
    }

    public User(int id, String name, Timestamp created, List<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.created = created != null ? created.toLocalDateTime().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        this.permissions = permissions;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
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
