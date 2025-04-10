package Entities;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey
    @Column(name = "id", nullable = false, autoIncrement = true)
    public Integer userId = 0;

    @Column
    public String username;

    public User(){}

    public User(String username) {
        this.username = username;
    }
}
