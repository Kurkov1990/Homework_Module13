package task3;

import com.google.gson.reflect.TypeToken;
import task3.model.Todo;
import util.ApiConstants;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public class TodoPrinter {

    public static void printOpenTodosForUser(int userId) {
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
