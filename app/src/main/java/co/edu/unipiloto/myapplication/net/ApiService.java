package co.edu.unipiloto.myapplication.net;

import co.edu.unipiloto.myapplication.model.LoginRequest;
import co.edu.unipiloto.myapplication.model.LoginResponse;
import co.edu.unipiloto.myapplication.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<Void> register(@Body RegisterRequest request);
}
