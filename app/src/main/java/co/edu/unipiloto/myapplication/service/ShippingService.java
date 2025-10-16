package co.edu.unipiloto.myapplication.service;

// ShippingService.java

// ShippingService.java

import co.edu.unipiloto.myapplication.model.ShippingStatus;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ShippingService {
    // REEMPLAZAMOS "shipping/{guideId}" por "posts/{guideId}" para usar JSONPlaceholder
    @GET("posts/{guideId}")
    Call<ShippingStatus> getShippingStatus(
            @Path("guideId") String guideCode
    );
}