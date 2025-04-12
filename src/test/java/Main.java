import Entities.Comment;
import Entities.Post;
import Entities.User;
import com.google.protobuf.Message;
import dev.alortie.jorm.core.JORM;
import dev.alortie.jorm.utils.Log;
import dev.alortie.jorm.utils.LogLevel;
import dev.alortie.jorm.utils.SQLBuilder;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Log.setInstance(LogLevel.DEBUG);
        JORM db = JORM.InitInstance(
                new SQLBuilder(
                        "jdbc:mysql://localhost:3306",
                        "root",
                        "root",
                        "test_db"),
                List.of(Comment.class, Post.class, User.class)
        );


        // Create 5 users
        User user1 = new User("Alphabeta");
        User user2 = new User("Betagamma");
        User user3 = new User("Gammabeta");
        User user4 = new User("Deltabeta");
        User user5 = new User("Epsilonbeta");

        db.repository(User.class).insert(user1);
        db.repository(User.class).insert(user2);
        db.repository(User.class).insert(user3);
        db.repository(User.class).insert(user4);
        db.repository(User.class).insert(user5);

        List<User> users = db.repository(User.class)
                .query()
                .where("username", "LIKE", "%beta%")
                .limit(12)
                .findAll();

        for (User user : users) {
            System.out.println(user);
        }




    }


}
