package userapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import userapi.model.Comment;
import userapi.model.Post;
import userapi.model.Todo;
import userapi.model.User;
import userapi.util.ApiConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public void downloadLatestPostComments(int userId) {
        List<Post> posts = fetchUserPosts(userId);
        if (posts == null || posts.isEmpty()) {
            System.out.printf("No posts found for user with id=%d%n", userId);
            return;
        }

        Post latestPost = getLatestPost(posts);
        if (latestPost == null) {
            System.out.printf("No latest post found for user with id=%d%n", userId);
            return;
        }

        int postId = latestPost.getId();
        List<Comment> comments = fetchPostComments(postId);
        if (comments == null) {
            System.out.printf("Failed to fetch comments for post id=%d%n", postId);
            return;
        }

        String filename = String.format("user-%d-post-%d-comments.json", userId, postId);
        try (FileWriter writer = new FileWriter(filename)) {
            ApiConstants.GSON.toJson(comments, writer);
            System.out.printf("Saved to file: %s (%d comments)%n", filename, comments.size());
        } catch (IOException e) {
            System.err.printf("Failed to save comments to file %s: %s%n", filename, e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Post> fetchUserPosts(int userId) {
        String url = ApiConstants.BASE_URL + "/users/" + userId + "/posts";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = ApiConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return ApiConstants.GSON.fromJson(response.body(), new TypeToken<List<Post>>() {
            }.getType());
        } catch (IOException | InterruptedException e) {
            System.err.printf("Failed to fetch posts for user %d: %s%n", userId, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static List<Comment> fetchPostComments(int postId) {
        String url = ApiConstants.BASE_URL + "/posts/" + postId + "/comments";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = ApiConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return ApiConstants.GSON.fromJson(response.body(), new TypeToken<List<Comment>>() {
            }.getType());
        } catch (IOException | InterruptedException e) {
            System.err.printf("Failed to fetch comments for post %d: %s%n", postId, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Post getLatestPost(List<Post> posts) {
        return posts.stream()
                .max(Comparator.comparingInt(Post::getId))
                .orElse(null);
    }

    public void printOpenTodosForUser(int userId) {
        try {
            String url = ApiConstants.BASE_URL + "/users/" + userId + "/todos";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = ApiConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            List<Todo> todos = ApiConstants.GSON.fromJson(response.body(), new TypeToken<List<Todo>>() {
            }.getType());

            List<Todo> openTodos = todos.stream()
                    .filter(todo -> !todo.isCompleted())
                    .collect(Collectors.toList());

            if (openTodos.isEmpty()) {
                System.out.printf("No open todos for user with id=%d%n", userId);
            } else {
                System.out.printf("Open todos for user with id=%d:%n", userId);
                openTodos.forEach(System.out::println);
            }

        } catch (IOException | InterruptedException e) {
            System.err.printf("Failed to fetch todos for user %d: %s%n", userId, e.getMessage());
        }
    }
}
