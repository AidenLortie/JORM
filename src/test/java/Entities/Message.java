package Entities;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.PrimaryKey;

@Entity(tableName = "message")
public class Message {

    @PrimaryKey
    @Column(name = "id", nullable = false, autoIncrement = true)
    public int id;

    @Column(name = "sender_id", nullable = false)
    public String senderId;

    @Column(name = "receiver_id", nullable = false)
    public String receiverId;

    @Column(name = "content", nullable = false)
    public String content;

    public Message(){}
}
