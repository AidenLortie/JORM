package Entities;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.ManyToOne;
import dev.alortie.jorm.annotations.PrimaryKey;

@Entity(tableName = "post")
public class Post {
    @PrimaryKey
    @Column(autoIncrement = true)
    public int id;

    @ManyToOne(foreignKey = "user_id")
    @Column(name = "user_id", nullable = false)
    public User author;

    @Column
    public String title;

    public Post() {}
    public Post(User author, String title) {
        this.author = author;
        this.title = title;
    }
}
