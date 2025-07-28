package userapi.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.http.HttpClient;

public class ApiConstants {

    public static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ApiConstants() {
    }
}

