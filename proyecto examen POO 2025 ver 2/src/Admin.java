import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Admin {
    public JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JTable tabla_de_productos;
    private JTree categorias_de_productos;
    private JLabel label_de_imagen;
    private JButton cargarImagenButton;
    private JButton agregarProductoButton;
    private JTextField nombreTextField;
    private JTextField precioTextField;
    private JTextField marcaTextField;
    private JTextField descripcionTextField;
    private JTextField categoriaTextField;
    private JTextField stockTextField;
    private JButton cerrarSesiónButton;
    private byte[] imagenBytes;  // Variable para almacenar la imagen cargada

    public Admin() {
        cargarDatosEnTabla("TODOS");  // Inicializar con todos los productos
        cargarCategoriasEnTree();
        agregarEventoClickTabla();
        agregarProductoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProducto();
            }
        });
        cargarImagenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarYGuardarImagen();  // Llama al método que carga y guarda la imagen
            }
        });
        cerrarSesiónButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cierra la ventana actual (donde está el botón)
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(cerrarSesiónButton);
                currentFrame.dispose();  // Cierra la ventana actual completamente

                // Crear un nuevo JFrame con la pantalla Pantalla_p
                JFrame frame = new JFrame("Pantalla de Productos");

                // Crear la instancia de Pantalla_p (asumiendo que 'panel1' es un JPanel)
                Pantalla_p pantalla = new Pantalla_p();

                // Establecer el panel en el JFrame
                frame.setContentPane(pantalla.panel1);  // panel1 es el panel de la clase Pantalla_p

                // Configurar el comportamiento de cierre
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Establecer el tamaño de la ventana
                frame.setSize(800, 600);

                // Hacer visible la ventana
                frame.setVisible(true);

                // Opcional: Centrar la ventana
                frame.setLocationRelativeTo(null);
            }
        });
    }

    private void cargarDatosEnTabla(String categoria) {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("Producto 1");
        modelo.addColumn("Producto 2");
        modelo.addColumn("Producto 3");

        String query = "SELECT Id, Nombre, Precio, Marca, Descripcion, Categoria, Imagen, Stock FROM PRODUCTOS";
        if (!categoria.equals("TODOS")) {
            query += " WHERE Categoria = ?";
        }

        try (Connection conn = Conexion_DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Si no es "TODOS", añade el parámetro para la categoría
            if (!categoria.equals("TODOS")) {
                stmt.setString(1, categoria);
            }

            ResultSet rs = stmt.executeQuery();
            List<ProductoCelda> listaProductos = new ArrayList<>();

            while (rs.next()) {
                byte[] imagenBytes = rs.getBytes("Imagen");
                String nombreProducto = rs.getString("Nombre");
                int idProducto = rs.getInt("Id");

                ImageIcon imagenIcon = (imagenBytes != null) ? new ImageIcon(imagenBytes) : null;
                ProductoCelda producto = new ProductoCelda(imagenIcon, nombreProducto, idProducto);
                listaProductos.add(producto);
            }

            // Agregar los productos a la tabla
            for (int i = 0; i < listaProductos.size(); i += 3) {
                Object[] fila = new Object[3];
                for (int j = 0; j < 3; j++) {
                    if (i + j < listaProductos.size()) {
                        fila[j] = listaProductos.get(i + j);
                    } else {
                        fila[j] = new ProductoCelda(null, "Vacío", -1);  // -1 para "vacío"
                    }
                }
                modelo.addRow(fila);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        tabla_de_productos.setModel(modelo);
        for (int i = 0; i < 3; i++) {
            tabla_de_productos.getColumnModel().getColumn(i).setCellRenderer(new ProductoCellRenderer());
            tabla_de_productos.getColumnModel().getColumn(i).setPreferredWidth(200); // ancho de la celda


        }
        tabla_de_productos.setRowHeight(150);
        tabla_de_productos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    // ProductoCelda con idProducto incluido
    static class ProductoCelda {
        private ImageIcon imagen;
        private String nombre;
        private int idProducto;

        public ProductoCelda(ImageIcon imagen, String nombre, int idProducto) {
            this.imagen = imagen;
            this.nombre = nombre;
            this.idProducto = idProducto;
        }

        public ImageIcon getImagen() {
            return imagen;
        }

        public String getNombre() {
            return nombre;
        }

        public int getIdProducto() {
            return idProducto;
        }
    }

    static class ProductoCellRenderer extends JPanel implements TableCellRenderer {
        private JLabel lblImagen;
        private JLabel lblNombre;

        public ProductoCellRenderer() {
            setLayout(new BorderLayout());
            lblImagen = new JLabel();
            lblNombre = new JLabel("", SwingConstants.CENTER);
            lblNombre.setFont(new Font("Arial", Font.PLAIN, 12));

            add(lblImagen, BorderLayout.CENTER);
            add(lblNombre, BorderLayout.SOUTH);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ProductoCelda) {
                ProductoCelda producto = (ProductoCelda) value;

                // Si el producto es "Vacío", no mostramos imagen y solo ponemos el texto
                if (producto.getImagen() != null) {
                    Image img = producto.getImagen().getImage();
                    Image newImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(newImg));
                } else {
                    lblImagen.setIcon(null);
                }

                lblNombre.setText(producto.getNombre());

                if (isSelected) {
                    setBackground(new Color(200, 200, 255));
                } else {
                    setBackground(Color.WHITE);
                }
            }
            return this;
        }
    }

    private void agregarEventoClickTabla() {
        tabla_de_productos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla_de_productos.rowAtPoint(e.getPoint());
                int columna = tabla_de_productos.columnAtPoint(e.getPoint());
                if (fila >= 0 && columna >= 0) {
                    ProductoCelda producto = (ProductoCelda) tabla_de_productos.getValueAt(fila, columna);
                    if (producto != null && producto.getIdProducto() != -1) {
                        // Mostrar el id del producto al hacer clic
                        String mensaje = "Producto seleccionado: " + producto.getNombre() + " (ID: " + producto.getIdProducto() + ")";
                        int confirmacion = JOptionPane.showConfirmDialog(
                                null,
                                mensaje + "\n¿Está seguro de eliminar este producto?",
                                "Confirmar eliminación",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (confirmacion == JOptionPane.YES_OPTION) {
                            // Eliminar el producto de la base de datos usando el idProducto
                            eliminarProductoDeBD(producto.getIdProducto());
                            cargarDatosEnTabla("TODOS");  // Recargar todos los productos después de eliminar
                        }
                    }
                }
            }
        });
    }

    private void eliminarProductoDeBD(int idProducto) {
        String query = "DELETE FROM PRODUCTOS WHERE Id = ?";
        try (Connection conn = Conexion_DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idProducto);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Producto eliminado exitosamente.");
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el producto para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al eliminar el producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void cargarCategoriasEnTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Categorías");

        String[] categorias = {
                "TODOS", "Computadoras y Laptops", "Teléfonos y Smartphones", "Tabletas y E-Readers",
                "Televisores y Audio", "Electrónica Portátil y Gadgets", "Redes y Conectividad", "Gaming",
                "Hogar Inteligente", "Almacenamiento y Backup", "Periféricos y Accesorios", "Cuidado y Reparación de Dispositivos"
        };

        for (String categoria : categorias) {
            root.add(new DefaultMutableTreeNode(categoria));
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        categorias_de_productos.setModel(treeModel);

        categorias_de_productos.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) categorias_de_productos.getLastSelectedPathComponent();
                if (nodoSeleccionado != null) {
                    String categoriaSeleccionada = nodoSeleccionado.toString();
                    cargarDatosEnTabla(categoriaSeleccionada);  // Filtrar por la categoría seleccionada
                }
            }
        });
    }

    private void cargarYGuardarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona una imagen");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "gif"));

        int seleccion = fileChooser.showOpenDialog(null);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivoImagen = fileChooser.getSelectedFile();

            // Mostrar la imagen seleccionada en la interfaz gráfica
            ImageIcon icono = new ImageIcon(archivoImagen.getAbsolutePath());
            Image imagenEscalada = icono.getImage().getScaledInstance(label_de_imagen.getWidth(), label_de_imagen.getHeight(), Image.SCALE_SMOOTH);
            label_de_imagen.setIcon(new ImageIcon(imagenEscalada));

            // Guardar la imagen en formato byte[] para insertarla en la base de datos
            try (FileInputStream fis = new FileInputStream(archivoImagen)) {
                imagenBytes = fis.readAllBytes();  // Almacenar la imagen en la variable byte[]
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error al leer la imagen: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void agregarProducto() {
        String nombre = nombreTextField.getText();
        String precioStr = precioTextField.getText();
        String marca = marcaTextField.getText();
        String descripcion = descripcionTextField.getText();
        String categoria = categoriaTextField.getText();
        String stockStr = stockTextField.getText();

        // Validar los campos (por ejemplo, asegurarse de que el precio y stock son números válidos)
        try {
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);

            // Validar si hay imagen cargada
            if (imagenBytes == null) {
                JOptionPane.showMessageDialog(null, "Por favor, carga una imagen para el producto.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Preparar la consulta SQL para insertar el nuevo producto
            String query = "INSERT INTO PRODUCTOS (Nombre, Precio, Marca, Descripcion, Categoria, Imagen, Stock) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = Conexion_DB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, nombre);
                stmt.setDouble(2, precio);
                stmt.setString(3, marca);
                stmt.setString(4, descripcion);
                stmt.setString(5, categoria);
                stmt.setBytes(6, imagenBytes);  // Insertar la imagen como BLOB
                stmt.setInt(7, stock);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Producto agregado exitosamente.");
                    // Limpiar los campos después de agregar el producto
                    nombreTextField.setText("");
                    precioTextField.setText("");
                    marcaTextField.setText("");
                    descripcionTextField.setText("");
                    categoriaTextField.setText("");
                    stockTextField.setText("");
                    label_de_imagen.setIcon(null);  // Limpiar la imagen
                } else {
                    JOptionPane.showMessageDialog(null, "Error al agregar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al agregar el producto a la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Por favor, ingresa valores válidos para precio y stock.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    public static void main(String[] args) {
        JFrame frame = new JFrame("Pantalla de Admin");
        frame.setContentPane(new Admin().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
