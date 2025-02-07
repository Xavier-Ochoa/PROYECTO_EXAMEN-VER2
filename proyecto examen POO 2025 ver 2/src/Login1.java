import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.util.logging.*;

public class Login1 {
    public JPanel panel1;
    private JButton iniciarSesionButton;
    private JPasswordField contraseñaPasswordField;
    private JTextField correoElectronicoTextField;
    private JButton volverButton;

    private static final Logger logger = Logger.getLogger(Login1.class.getName());

    public Login1(JFrame frame) {
        // Acción del botón de iniciar sesión
        iniciarSesionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener los valores de los campos
                String correoElectronico = correoElectronicoTextField.getText();
                String contraseña = new String(contraseñaPasswordField.getPassword());

                // Validación de campos vacíos
                if (correoElectronico.isEmpty() || contraseña.isEmpty()) {
                    JOptionPane.showMessageDialog(panel1, "Por favor, completa ambos campos.");
                    return;
                }

                // Validación del correo electrónico
                if (!validarCorreoElectronico(correoElectronico)) {
                    JOptionPane.showMessageDialog(panel1, "Por favor, ingrese un correo electrónico válido.");
                    return;
                }

                // Validación de la contraseña
                if (!validarContraseña(contraseña)) {
                    JOptionPane.showMessageDialog(panel1, "La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, un número y un carácter especial.");
                    return;
                }

                // Intentar iniciar sesión
                try {
                    // Verificar las credenciales
                    User user = verificarUsuario(correoElectronico, contraseña);
                    if (user != null) {
                        JOptionPane.showMessageDialog(panel1, "¡Inicio de sesión exitoso!");

                        // Redirigir según el rol
                        String rol = user.getRol(); // Obtener el rol directamente desde la base de datos
                        if ("Usuario".equals(rol)) {
                            // Redirigir a la pantalla 'Usuario' después de iniciar sesión
                            Ussuario usuarioPantalla = new Ussuario(user); // Pasamos el usuario
                            frame.setContentPane(usuarioPantalla.panel1); // Cambiar el panel del frame
                            frame.revalidate(); // Revalidar el frame para reflejar el cambio
                            frame.repaint(); // Volver a dibujar el frame
                            frame.setSize(800, 600);
                            frame.setLocationRelativeTo(null);
                        } else if ("Admin".equals(rol)) {
                            // Redirigir a la pantalla 'Admin'
                            Admin adminPantalla = new Admin(); // Pasamos el usuario
                            frame.setContentPane(adminPantalla.panel1); // Cambiar el panel del frame
                            frame.revalidate(); // Revalidar el frame para reflejar el cambio
                            frame.repaint(); // Volver a dibujar el frame
                            frame.setSize(800, 600);
                            frame.setLocationRelativeTo(null);
                        }
                    } else {
                        JOptionPane.showMessageDialog(panel1, "Correo o contraseña incorrectos.");
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error al verificar el usuario", ex);
                    JOptionPane.showMessageDialog(panel1, "Error al iniciar sesión: " + ex.getMessage());
                }
            }
        });

        // Acción del botón de volver
        volverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Redirigir a la pantalla 'Pantalla_p'
                Pantalla_p pantalla = new Pantalla_p();
                frame.setContentPane(pantalla.panel1); // Cambiar el panel del frame
                frame.revalidate(); // Revalidar el frame para reflejar el cambio
                frame.repaint(); // Volver a dibujar el frame
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);
            }
        });
    }

    // Método para validar el correo electrónico
    private boolean validarCorreoElectronico(String correoElectronico) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return correoElectronico.matches(regex);
    }

    // Método para validar la contraseña
    private boolean validarContraseña(String contraseña) {
        // Expresión regular para comprobar contraseña
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return contraseña.matches(regex);
    }

    // Método para verificar las credenciales del usuario (por ejemplo, consultando la base de datos)
    private User verificarUsuario(String correoElectronico, String contraseña) throws Exception {
        // Conexión a la base de datos
        try (Connection connection = Conexion_DB.getConnection()) {  // Usamos try-with-resources para cerrar la conexión automáticamente
            // SQL para verificar las credenciales del usuario
            String query = "SELECT * FROM CLIENTE WHERE CorreoElectronico = ? AND Contrasena = ?";

            // Preparar el statement
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                // Establecer los valores
                stmt.setString(1, correoElectronico);
                stmt.setString(2, Registro.encriptarContraseña(contraseña));  // Encriptamos la contraseña

                // Ejecutar la consulta
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // Obtener los datos del usuario, incluyendo el rol
                    String nombreCompleto = rs.getString("NombreCompleto");
                    String rol = rs.getString("Rol");  // Obtener el rol del usuario
                    // Retornar un objeto User con el rol y otros datos
                    return new User(nombreCompleto, correoElectronico, rol);
                } else {
                    return null; // Usuario no encontrado
                }
            }
        } catch (SQLException ex) {
            // Si ocurre alguna excepción, la lanzamos nuevamente para manejarla fuera
            logger.log(Level.SEVERE, "Error en la base de datos", ex);
            throw new Exception("Error en la conexión o al ejecutar la consulta: " + ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        // Crear el JFrame para mostrar el panel de inicio de sesión
        JFrame frame = new JFrame("Inicio de sesión");
        frame.setContentPane(new Login1(frame).panel1); // Pasamos el frame al constructor de Login1
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
