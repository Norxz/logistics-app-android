package co.edu.unipiloto.myapplication.model;

public class Solicitud {
    public long id;
    public long userId;
    public String direccion;
    public String fecha;
    public String franja;
    public String notas;
    public String estado;
    public String zona;
    public long createdAt;

    public Solicitud(long id, long userId, String direccion, String fecha,
                     String franja, String notas, String estado, String zona, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.direccion = direccion;
        this.fecha = fecha;
        this.franja = franja;
        this.notas = notas;
        this.estado = estado;
        this.zona = zona;
        this.createdAt = createdAt;
    }
}
