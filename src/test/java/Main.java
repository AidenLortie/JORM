import Entities.Message;
import Entities.User;
import dev.alortie.jorm.core.ORMEngine;
import dev.alortie.jorm.utils.Log;
import dev.alortie.jorm.utils.LogLevel;
import dev.alortie.jorm.utils.SQLUtils;

import java.util.ArrayList;
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
                List.of(User.class, Message.class)
        );

        User user = new User("testUser");
        User user2 = new User("testUser2");
        User user3 = new User("testUser3");

        DB.repository(User.class).insert(user);
        DB.repository(User.class).insert(user2);
        DB.repository(User.class).insert(user3);

        for(User u : DB.repository(User.class).selectAll() ) {
            System.out.println(u.userId + " " + u.username);
        }

    }
}
