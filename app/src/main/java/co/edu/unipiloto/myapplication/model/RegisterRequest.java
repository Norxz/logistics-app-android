package co.edu.unipiloto.myapplication.model;

public class RegisterRequest {
    public String email;
    public String password;
    public String rol;

    public RegisterRequest(String email, String password, String rol) {
        this.email = email;
        this.password = password;
        this.rol = rol;
    }
}
