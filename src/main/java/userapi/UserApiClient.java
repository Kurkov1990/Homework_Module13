package userapi;

import userapi.model.User;

import java.util.List;

public interface UserApiClient {
    User createUser(User user);

    User updateUser(User user);

    boolean deleteUser(int id);

    List<User> getAllUsers();

    void downloadLatestPostComments(int userId);

    void printOpenTodosForUser(int userId);
}
