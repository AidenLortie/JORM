package Entities;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey
    @Column(name = "id", nullable = false)
    public String UserId;

    @Column(name = "username")
    public String username;

    public User(){}

    public User(String userId, String username) {
        this.UserId = userId;
        this.username = username;
    }
}
