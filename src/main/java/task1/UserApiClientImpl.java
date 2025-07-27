package task1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import task1.model.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class UserApiClientImpl implements UserApiClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";
    private final HttpClient client;
    private final Gson gson;

    public UserApiClientImpl() {
        client = HttpClient.newHttpClient();
        gson = new Gson();
    }

    @Override
    public User createUser(User user) {
        try {
            String body = gson.toJson(user);
            HttpRequest request = buildRequest(BASE_URL, "POST", body);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccessful(response.statusCode())) {
                return gson.fromJson(response.body(), User.class);
            } else {
                System.err.println("Failed to create user. Status code: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Failed to create user: " + e.getMessage());
            return null;
        }
    }

    @Override
    public User updateUser(User user) {
        try {
            if (user.getId() <= 0) {
                System.err.println("Invalid user id for update: " + user.getId());
                return null;
            }

            String body = gson.toJson(user);
            HttpRequest request = buildRequest(BASE_URL + "/" + user.getId(), "PATCH", body);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccessful(response.statusCode())) {
                return gson.fromJson(response.body(), User.class);
            } else {
                System.err.println("Failed to update user. Status code: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteUser(int id) {
        try {
            HttpRequest request = buildRequest(BASE_URL + "/" + id, "DELETE", null);
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            if (!isSuccessful(response.statusCode())) {
                System.err.println("Failed to delete user. Status code: " + response.statusCode());
            }

            return isSuccessful(response.statusCode());
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            HttpRequest request = buildRequest(BASE_URL, "GET", null);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (isSuccessful(response.statusCode())) {
                return gson.fromJson(response.body(), new TypeToken<List<User>>() {
                }.getType());
            } else {
                System.err.println("Failed to fetch users. Status code: " + response.statusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private HttpRequest buildRequest(String url, String method, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");

        switch (method.toUpperCase()) {
            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofString(body));
                break;
            case "PATCH":
                builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body));
                break;
            case "DELETE":
                builder.DELETE();
                break;
            case "GET":
                builder.GET();
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return builder.build();
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
