import Entities.Comment;
import Entities.Post;
import Entities.User;
import dev.alortie.jorm.core.ORMEngine;
import dev.alortie.jorm.utils.Log;
import dev.alortie.jorm.utils.LogLevel;
import dev.alortie.jorm.utils.SQLUtils;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Log.setInstance(LogLevel.DEBUG);
        ORMEngine DB = ORMEngine.InitInstance(
                new SQLUtils(
                        "jdbc:mysql://localhost:3306",
                        "root",
                        "root",
                        "test_db"),
                List.of( Comment.class, Post.class, User.class)
        );


}
