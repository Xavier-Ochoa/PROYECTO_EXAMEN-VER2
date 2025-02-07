public class User {
    private String nombreCompleto;
    private String correoElectronico;
    private String rol;  // Atributo para almacenar el rol del usuario

    // Constructor para inicializar la información del usuario, incluyendo el rol
    public User(String nombreCompleto, String correoElectronico, String rol) {
        this.nombreCompleto = nombreCompleto;
        this.correoElectronico = correoElectronico;
        this.rol = rol;  // Asignamos el rol al objeto
    }

    // Métodos getter para acceder a los datos del usuario
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getRol() {
        return rol;  // Retorna el rol del usuario
    }
}
