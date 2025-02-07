import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Pantalla_p {
    public JPanel panel1;
    private JTextField ingreso_de_busqueda;
    private JButton boton_de_registrarse;
    private JButton boton_de_logearse;
    private JTable tabla_de_productos;
    private JTree categorias_de_productos;
    private JButton boton_de_busqueda;
    private JComboBox<String> buscar_segun;

    public Pantalla_p() {
        cargarDatosEnTabla("TODOS");  // Inicializar con todos los productos
        cargarCategoriasEnTree();
        inicializarComboBox();

        // Añadir listener al botón de búsqueda
        boton_de_busqueda.addActionListener(e -> realizarBusqueda());
        // Acciones de los botones
        boton_de_registrarse.addActionListener(e -> {
            abrirFormularioRegistro();
            cerrarPantalla();  // Cerrar la ventana actual
        });
        boton_de_logearse.addActionListener(e -> {
            abrirFormularioLogin();
            cerrarPantalla();  // Cerrar la ventana actual
        });
    }

    private void inicializarComboBox() {
        buscar_segun.removeAllItems();
        buscar_segun.addItem("Nombre");
        buscar_segun.addItem("Marca");
        buscar_segun.addItem("Precio");
    }

    private void cargarDatosEnTabla(String categoria) {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("Producto 1");
        modelo.addColumn("Producto 2");
        modelo.addColumn("Producto 3");

        // Crear la consulta según la categoría seleccionada
        String query = "SELECT Id, Nombre, Precio, Marca, Descripcion, Categoria, Imagen, Stock FROM PRODUCTOS";
        if (!categoria.equals("TODOS")) {
            query += " WHERE Categoria = '" + categoria + "'";  // Filtrar por categoría
        }

        try (Connection conn = Conexion_DB.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<ProductoCelda> listaProductos = new ArrayList<>();

            // Cargar los productos en una lista
            while (rs.next()) {
                byte[] imagenBytes = rs.getBytes("Imagen");
                String nombreProducto = rs.getString("Nombre");

                ImageIcon imagenIcon = (imagenBytes != null) ? new ImageIcon(imagenBytes) : null;
                ProductoCelda producto = new ProductoCelda(imagenIcon, nombreProducto);

                listaProductos.add(producto);
            }

            // Llenar la tabla con filas de 3 productos por fila
            for (int i = 0; i < listaProductos.size(); i += 3) {
                Object[] fila = new Object[3];

                // Agregar productos a la fila asegurando que los espacios vacíos se muestren como "Vacío"
                for (int j = 0; j < 3; j++) {
                    if (i + j < listaProductos.size()) {
                        fila[j] = listaProductos.get(i + j);
                    } else {
                        fila[j] = new ProductoCelda(null, "Vacío"); // Celda vacía
                    }
                }

                modelo.addRow(fila);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        tabla_de_productos.setModel(modelo);

        tabla_de_productos.getColumnModel().getColumn(0).setPreferredWidth(145);
        tabla_de_productos.getColumnModel().getColumn(1).setPreferredWidth(145);
        tabla_de_productos.getColumnModel().getColumn(2).setPreferredWidth(145);

        tabla_de_productos.getColumnModel().getColumn(0).setCellRenderer(new ProductoCellRenderer());
        tabla_de_productos.getColumnModel().getColumn(1).setCellRenderer(new ProductoCellRenderer());
        tabla_de_productos.getColumnModel().getColumn(2).setCellRenderer(new ProductoCellRenderer());

        tabla_de_productos.setRowHeight(150);
        tabla_de_productos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void realizarBusqueda() {
        String criterioBusqueda = (String) buscar_segun.getSelectedItem();
        String valorBusqueda = ingreso_de_busqueda.getText().trim();

        // Si el campo de búsqueda está vacío, no hacer nada
        if (valorBusqueda.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor ingrese un valor para buscar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Realizar la búsqueda
        DefaultTableModel modelo = (DefaultTableModel) tabla_de_productos.getModel();
        String query = "SELECT Id, Nombre, Precio, Marca, Descripcion, Categoria, Imagen, Stock FROM PRODUCTOS WHERE " + criterioBusqueda + " LIKE '%" + valorBusqueda + "%'";

        try (Connection conn = Conexion_DB.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<ProductoCelda> listaProductos = new ArrayList<>();

            // Cargar los productos en una lista
            while (rs.next()) {
                byte[] imagenBytes = rs.getBytes("Imagen");
                String nombreProducto = rs.getString("Nombre");

                ImageIcon imagenIcon = (imagenBytes != null) ? new ImageIcon(imagenBytes) : null;
                ProductoCelda producto = new ProductoCelda(imagenIcon, nombreProducto);

                listaProductos.add(producto);
            }

            // Limpiar el modelo de la tabla antes de agregar nuevos resultados
            modelo.setRowCount(0);

            // Llenar la tabla con filas de 3 productos por fila
            for (int i = 0; i < listaProductos.size(); i += 3) {
                Object[] fila = new Object[3];

                // Agregar productos a la fila asegurando que los espacios vacíos se muestren como "Vacío"
                for (int j = 0; j < 3; j++) {
                    if (i + j < listaProductos.size()) {
                        fila[j] = listaProductos.get(i + j);
                    } else {
                        fila[j] = new ProductoCelda(null, "Vacío"); // Celda vacía
                    }
                }

                modelo.addRow(fila);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al realizar la búsqueda: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCategoriasEnTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Categorías");

        DefaultMutableTreeNode todos = new DefaultMutableTreeNode("TODOS");
        DefaultMutableTreeNode computadorasYLaptops = new DefaultMutableTreeNode("Computadoras y Laptops");
        DefaultMutableTreeNode telefonosYSmartphones = new DefaultMutableTreeNode("Teléfonos y Smartphones");
        DefaultMutableTreeNode tabletasYEReaders = new DefaultMutableTreeNode("Tabletas y E-Readers");
        DefaultMutableTreeNode televisoresYAudio = new DefaultMutableTreeNode("Televisores y Audio");
        DefaultMutableTreeNode electronicaPortatilYGadgets = new DefaultMutableTreeNode("Electrónica Portátil y Gadgets");
        DefaultMutableTreeNode redesYConectividad = new DefaultMutableTreeNode("Redes y Conectividad");
        DefaultMutableTreeNode gaming = new DefaultMutableTreeNode("Gaming");
        DefaultMutableTreeNode hogarInteligente = new DefaultMutableTreeNode("Hogar Inteligente");
        DefaultMutableTreeNode almacenamientoYBackup = new DefaultMutableTreeNode("Almacenamiento y Backup");
        DefaultMutableTreeNode perifericosYAccesorios = new DefaultMutableTreeNode("Periféricos y Accesorios");
        DefaultMutableTreeNode cuidadoYReparacion = new DefaultMutableTreeNode("Cuidado y Reparación de Dispositivos");

        root.add(todos);
        root.add(computadorasYLaptops);
        root.add(telefonosYSmartphones);
        root.add(tabletasYEReaders);
        root.add(televisoresYAudio);
        root.add(electronicaPortatilYGadgets);
        root.add(redesYConectividad);
        root.add(gaming);
        root.add(hogarInteligente);
        root.add(almacenamientoYBackup);
        root.add(perifericosYAccesorios);
        root.add(cuidadoYReparacion);

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        categorias_de_productos.setModel(treeModel);

        // Agregar TreeSelectionListener para escuchar cuando se seleccione una categoría
        categorias_de_productos.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) categorias_de_productos.getLastSelectedPathComponent();
            if (nodoSeleccionado != null) {
                String categoriaSeleccionada = nodoSeleccionado.toString();
                cargarDatosEnTabla(categoriaSeleccionada);  // Filtrar por la categoría seleccionada
            }
        });
    }

    private void abrirFormularioRegistro() {
        JFrame frame = new JFrame("Formulario de Registro");
        frame.setContentPane(new Registro(frame).panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);  // Centrar ventana
        frame.setVisible(true);
    }

    private void abrirFormularioLogin() {
        JFrame frame = new JFrame("Formulario de Login");
        frame.setContentPane(new Login1(frame).panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);  // Centrar ventana
        frame.setVisible(true);
    }

    // Método para cerrar la ventana actual
    private void cerrarPantalla() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(panel1); // Obtener la ventana que contiene el JPanel
        if (frame != null) {
            frame.dispose(); // Cerrar la ventana
        }
    }

    static class ProductoCelda {
        private ImageIcon imagen;
        private String nombre;

        public ProductoCelda(ImageIcon imagen, String nombre) {
            this.imagen = imagen;
            this.nombre = nombre;
        }

        public ImageIcon getImagen() {
            return imagen;
        }

        public String getNombre() {
            return nombre;
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
}
