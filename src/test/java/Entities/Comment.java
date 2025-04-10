package Entities;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.ManyToOne;
import dev.alortie.jorm.annotations.PrimaryKey;

@Entity(tableName = "comment")
public class Comment {
    @PrimaryKey
    @Column(autoIncrement = true)
    public int id;

    @ManyToOne(foreignKey = "post_id")
    @Column(name = "post_id", nullable = false)
    public Post post;

    @Column
    public String content;

    public Comment() {
    }

    public Comment(Post post, String content) {
        this.post = post;
        this.content = content;
    }
}

