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
        Log.setInstance(LogLevel.ERROR);
        ORMEngine DB = ORMEngine.InitInstance(
                new SQLUtils(
                        "jdbc:mysql://localhost:3306",
                        "root",
                        "root",
                        "test_db"),
                List.of(User.class, Message.class)
        );


        for (User u : DB.repository(User.class).selectAll()) {
            Log.o().info("User: " + u.UserId, u.username);
        }
    }
}
