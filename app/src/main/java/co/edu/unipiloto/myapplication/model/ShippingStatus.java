package co.edu.unipiloto.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class ShippingStatus {
    // Mapea los campos reales de la API (JSONPlaceholder) a tus nombres lógicos:
    @SerializedName("id")
    private String id; // El ID de la guía

    @SerializedName("title")
    private String status; // Usamos 'title' del JSON como el 'status'

    @SerializedName("body")
    private String destinationAddress; // Usamos 'body' del JSON como la 'dirección'

    // Estos campos no existen en JSONPlaceholder, los dejamos null/vacío para la prueba.
    private String estimatedDate = "2025-12-31";
    private String timeFranja = "PM";

    // Getters necesarios:
    public String getId() { return id; }
    public String getStatus() { return status; }
    public String getDestinationAddress() { return destinationAddress; }
    public String getEstimatedDate() { return estimatedDate; }
    public String getTimeFranja() { return timeFranja; }
}