package task1;

import task1.model.User;

import java.util.List;

public interface UserApiClient {
    User createUser(User user);

    User updateUser(User user);

    boolean deleteUser(int id);

    List<User> getAllUsers();
}
