package co.edu.unipiloto.myapplication.rest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface PostService {
    String API_ROUTE = "/posts"; // Defines the relative URI path [cite: 401, 408]

    @GET(API_ROUTE) // Indicates this method performs an HTTP GET request [cite: 410]
    Call<List<Post>> getPost(); // Returns a Call for a List of Post objects [cite: 412, 404]
}