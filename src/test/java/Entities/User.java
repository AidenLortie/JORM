package Entities;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @Column(autoIncrement = true, name = "user_id", unique = true, nullable = false)
    public int id;

    @Column(length = 50, nullable = false)
    public String username;

    public User() {}
    public User(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}

