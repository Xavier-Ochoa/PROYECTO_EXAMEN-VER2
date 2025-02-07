import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Crear un JFrame con un título
        JFrame frame = new JFrame("Pantalla de Productos");

        // Establecer el contenido de la ventana con el panel de la clase Pantalla_p
        // (Deberías asegurarte de que 'panel1' sea público o proporcionar un getter)
        Pantalla_p pantalla = new Pantalla_p();  // Crear una instancia de Pantalla_p
        frame.setContentPane(pantalla.panel1);   // Establecer el panel

        // Configurar el comportamiento de cierre
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Establecer el tamaño de la ventana (por ejemplo 800x600 píxeles)
        frame.setSize(800, 600);  // Puedes ajustar el tamaño que prefieras

        // Establecer el tamaño preferido para el JFrame (opcional, puedes usarlo junto con pack)
        frame.setPreferredSize(new java.awt.Dimension(800, 600)); // Esto es opcional

        // Empaquetar la ventana, ajustando el tamaño a los componentes dentro de la ventana
        frame.pack();

        // Hacer visible la ventana
        frame.setVisible(true);

        // Opcional: Centrar la ventana en la pantalla
        frame.setLocationRelativeTo(null);
    }
}
