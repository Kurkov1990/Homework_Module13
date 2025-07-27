package task1;

import task1.model.User;

public class App {

    public static void main(String[] args) {
        UserApiClient client = new UserApiClientImpl();

        System.out.println("List of users:");
        client.getAllUsers().forEach(System.out::println);

        printSeparator();

        User newUser = new User(0, "John Smith", "John.Smith", "smith@example.com");
        User createdUser = client.createUser(newUser);
        System.out.println("Created: " + createdUser);

        if (createdUser == null) {
            System.out.println("User creation failed, skipping update and delete.");
            return;
        }

        printSeparator();

        createdUser.setName("Steve Goldman");
        User updatedUser = client.updateUser(createdUser);
        System.out.println("Updated: " + updatedUser);

        printSeparator();

        boolean deleted = client.deleteUser(createdUser.getId());
        System.out.println("Deleted successfully: " + deleted);
    }

    private static void printSeparator() {
        System.out.println("####################################################################################################\n");
    }
}
