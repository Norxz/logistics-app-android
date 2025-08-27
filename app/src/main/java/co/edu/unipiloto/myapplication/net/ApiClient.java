package co.edu.unipiloto.myapplication.net;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    public static Retrofit getClient(Context ctx) {
        if (retrofit == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .build();

            Gson gson = new GsonBuilder().setLenient().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://TU-BACKEND.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
