import javax.swing.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.logging.*;
import java.util.regex.*;

public class Registro {
    public JPanel panel1;
    private JTextField nombreCompletoTextField;
    private JTextField correoElectronicoTextField;
    private JPasswordField contraseñaPasswordField;
    private JTextField cedulaTextField;
    private JTextField direccionTextField;
    private JButton registrarButton; // Este es el botón que vamos a usar para insertar los datos
    private JButton volver_a_la_pantalla_principal; // Botón para volver a la pantalla principal

    private static final Logger logger = Logger.getLogger(Registro.class.getName());

    public Registro(JFrame frame) {
        // Acción del botón de registrar
        registrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtén los datos de los JTextField
                String nombreCompleto = nombreCompletoTextField.getText();
                String correoElectronico = correoElectronicoTextField.getText();
                String contraseña = new String(contraseñaPasswordField.getPassword());
                String cedula = cedulaTextField.getText();
                String direccion = direccionTextField.getText();

                // Validación de campos vacíos
                if (nombreCompleto.isEmpty() || correoElectronico.isEmpty() || contraseña.isEmpty() || cedula.isEmpty() || direccion.isEmpty()) {
                    JOptionPane.showMessageDialog(panel1, "Por favor, completa todos los campos.");
                    return;
                }

                // Validación del correo electrónico
                if (!correoElectronico.contains("@") || !correoElectronico.contains(".")) {
                    JOptionPane.showMessageDialog(panel1, "Por favor, ingrese un correo electrónico válido.");
                    return;
                }

                // Validación del nombre completo (debe tener 4 palabras)
                if (!validarNombreCompleto(nombreCompleto)) {
                    JOptionPane.showMessageDialog(panel1, "El nombre completo debe tener 4 palabras (2 nombres y 2 apellidos).");
                    return;
                }

                // Validación de la contraseña
                if (!validarContraseña(contraseña)) {
                    JOptionPane.showMessageDialog(panel1, "La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, un número y un carácter especial.");
                    return;
                }

                // Validación de la cédula (solo números, exactamente 10 caracteres)
                if (!validarCedula(cedula)) {
                    JOptionPane.showMessageDialog(panel1, "La cédula debe ser solo números y tener exactamente 10 caracteres.");
                    return;
                }

                // Llama al método para insertar los datos en la base de datos
                try {
                    insertarCliente(nombreCompleto, correoElectronico, contraseña, cedula, direccion);
                    JOptionPane.showMessageDialog(panel1, "Usted se ha registrado exitosamente.\nInicie sesion con correo y contraseña");

                    // Redirigir a la pantalla principal y cerrar la ventana de registro
                    Pantalla_p pantalla = new Pantalla_p();
                    frame.setContentPane(pantalla.panel1); // Cambiar el panel del frame
                    frame.revalidate(); // Revalidar el frame para reflejar el cambio
                    frame.repaint(); // Volver a dibujar el frame
                    frame.setSize(800, 600); // Ajusta el tamaño del frame después de cambiar el contenido
                    frame.setLocationRelativeTo(null); // Pone el JFrame en el centro de la pantalla
                } catch (Exception ex) {
                    // Captura cualquier excepción que se lance desde el método insertarCliente
                    JOptionPane.showMessageDialog(panel1, "Error al registrar: " + ex.getMessage());
                }
            }
        });

        // Acción del botón "Volver a la pantalla principal"
        volver_a_la_pantalla_principal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Redirigir a la pantalla principal y cerrar la ventana de registro
                Pantalla_p pantalla = new Pantalla_p();
                frame.setContentPane(pantalla.panel1); // Cambiar el panel del frame
                frame.revalidate(); // Revalidar el frame para reflejar el cambio
                frame.repaint(); // Volver a dibujar el frame
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);
            }
        });
    }



    // Método para validar el nombre completo (debe tener exactamente 4 palabras)
    private boolean validarNombreCompleto(String nombreCompleto) {
        String[] partes = nombreCompleto.trim().split("\\s+");
        return partes.length == 4;
    }

    // Método para validar la contraseña
    private boolean validarContraseña(String contraseña) {
        // Expresión regular para comprobar contraseña
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return contraseña.matches(regex);
    }

    // Método para validar la cédula (solo números, exactamente 10 caracteres)
    private boolean validarCedula(String cedula) {
        return cedula.matches("\\d{10}");
    }

    // Método para encriptar la contraseña con SHA-256
    public static String encriptarContraseña(String contraseña) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(contraseña.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void insertarCliente(String nombreCompleto, String correoElectronico, String contraseña, String cedula, String direccion) throws Exception {
        // Conexión a la base de datos
        try (Connection connection = Conexion_DB.getConnection()) {  // Usamos try-with-resources para cerrar la conexión automáticamente
            // SQL para insertar datos
            String query = "INSERT INTO CLIENTE (NombreCompleto, CorreoElectronico, Contrasena, Cedula, Direccion, Rol) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            // Preparar el statement
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                // Establecer los valores
                stmt.setString(1, nombreCompleto);
                stmt.setString(2, correoElectronico);
                stmt.setString(3, encriptarContraseña(contraseña));  // Encriptamos la contraseña
                stmt.setString(4, cedula);
                stmt.setString(5, direccion);
                stmt.setString(6, "Usuario"); // El Rol siempre será "Usuario"

                // Ejecutar el update
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            // Si ocurre alguna excepción, la lanzamos nuevamente para manejarla fuera
            logger.log(Level.SEVERE, "Error en la base de datos", ex);
            throw new Exception("Error en la conexión o al ejecutar la consulta: " + ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) {
        // Crear el JFrame para mostrar el panel de registro
        JFrame frame = new JFrame("Registro");
        frame.setContentPane(new Registro(frame).panel1); // Pasamos el frame al constructor de Registro
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
