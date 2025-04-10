package Entities;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey
    @Column(autoIncrement = true)
    public int id;

    @Column
    public String username;

    public User() {}
    public User(String username) {
        this.username = username;
    }
}

