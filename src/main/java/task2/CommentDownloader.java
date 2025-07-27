package task2;

import com.google.gson.reflect.TypeToken;
import task2.model.Comment;
import task2.model.Post;
import util.ApiConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;

public class CommentDownloader {

    public static void downloadLatestPostComments(int userId) {
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
}
