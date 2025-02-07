import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



public class Ussuario {
    public JPanel panel1;
    private JLabel Ingreso_del_usuariologin;
    private JButton boton_de_busqueda;
    private JComboBox<String> buscar_segun;
    private JTextField ingreso_de_busqueda;
    private JTree categorias_de_productos;
    private JTable tabla_de_productos;
    private JTabbedPane tabbedPane1;
    private JTable tabla_de_carrito;
    private JButton boton_de_facturar;
    private JButton boton_de_borrar_carrito;
    private JTable tabla_productos_fecha;
    private JButton boton_crear_pPDF;
    private JLabel Ingreso_del_correlogin;
    private JButton actualizarButton;
    private JButton cerrarSesiónButton;
    private JList lista_facturas_segun_fecha;
    private JPanel Ingreso_del_correloginusa;
    private JButton actualizarButton1;

    // Constructor que recibe el objeto User
    public Ussuario(User user) {
        // Establecer el nombre completo y correo electrónico en los JLabels
        Ingreso_del_usuariologin.setText(user.getNombreCompleto());
        Ingreso_del_correlogin.setText(user.getCorreoElectronico());

        // Inicializar los componentes de búsqueda
        inicializarComboBox();
        cargarCategoriasEnTree();
        cargarDatosEnTabla("TODOS");
        cargarDatosCarrito();

        // Añadir listener al botón de búsqueda
        boton_de_busqueda.addActionListener(e -> realizarBusqueda());

        // Añadir un MouseListener a la tabla para detectar los clics en las celdas
        tabla_de_productos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabla_de_productos.rowAtPoint(evt.getPoint());
                int column = tabla_de_productos.columnAtPoint(evt.getPoint());

                if (row >= 0 && column >= 0) {
                    ProductoCelda productoCelda = (ProductoCelda) tabla_de_productos.getValueAt(row, column);
                    if (productoCelda != null) {
                        try {
                            mostrarDescripcion(productoCelda.getNombre());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        actualizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Llamada para cargar los datos al cargar la vista
                cargarDatosCarrito();
            }
        });
        boton_de_borrar_carrito.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mostrar un cuadro de confirmación antes de borrar
                int opcion = JOptionPane.showConfirmDialog(null,
                        "¿Estás seguro de que deseas eliminar todos los productos del carrito?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                // Si el usuario presiona "Sí"
                if (opcion == JOptionPane.YES_OPTION) {
                    eliminarCarrito();
                }

            }
        });
        boton_de_facturar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Obtener la fecha y hora actual
                    LocalDateTime fechaHora = LocalDateTime.now();

                    // Obtener los valores de los JLabels
                    String nombreCliente = Ingreso_del_usuariologin.getText();
                    String correoCliente = Ingreso_del_correlogin.getText();

                    // Obtener el Id_cliente de la tabla Clientes basado en el CorreoElectronico
                    int idCliente = obtenerIdCliente(correoCliente);

                    if (idCliente == -1) {
                        JOptionPane.showMessageDialog(null, "No se encontró un cliente con ese correo electrónico.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Copiar los registros de CARRITO_DROP a FACTURAS en una transacción
                    copiarCarritoAFacturas(fechaHora, correoCliente, idCliente, nombreCliente);

                    JOptionPane.showMessageDialog(null, "Factura generada con éxito.");
                    eliminarCarrito();
                    obtenerFechasFacturas();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al generar la factura: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }


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
        boton_crear_pPDF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener el nombre del usuario y el correo electrónico
                String nombreCliente = Ingreso_del_usuariologin.getText();
                String correoCliente = Ingreso_del_correlogin.getText();

                // Crear una instancia de la clase GenerarPDF
                GenerarPDF generarPDF = new GenerarPDF();

                // Llamar al método generarPDF con los parámetros correspondientes
                generarPDF.generarPDF(tabla_productos_fecha, lista_facturas_segun_fecha, Ingreso_del_usuariologin, Ingreso_del_correlogin);
            }
        });

        actualizarButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                obtenerFechasFacturas();

            }
        });
        lista_facturas_segun_fecha.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Solo respondemos si la selección no está siendo modificada
                    String fechaSeleccionada = (String) lista_facturas_segun_fecha.getSelectedValue(); // Obtener la fecha seleccionada

                    if (fechaSeleccionada != null) {
                        // Aquí llamamos al método cargarProductosEnTabla, pasando la fecha seleccionada y el panel
                        cargarProductosEnTabla(fechaSeleccionada);  // 'panel' es el contenedor donde se actualizará la JTable
                    }
                }
            }
        });
        boton_crear_pPDF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener el nombre del usuario y el correo electrónico
                String nombreCliente = Ingreso_del_usuariologin.getText();
                String correoCliente = Ingreso_del_correlogin.getText();

                // Crear una instancia de la clase GenerarPDF
                GenerarPDF generarPDF = new GenerarPDF();

                // Llamar al método generarPDF con los parámetros correspondientes
                generarPDF.generarPDF(tabla_productos_fecha, lista_facturas_segun_fecha, Ingreso_del_usuariologin, Ingreso_del_correlogin);
            }
        });
    }


    public void cargarProductosEnTabla(String fechaSeleccionada) {
        // Crear el modelo de la tabla
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Id Producto");
        model.addColumn("Nombre Producto");
        model.addColumn("Cantidad");
        model.addColumn("Precio Unitario");
        model.addColumn("Precio");

        // Consulta SQL para obtener los productos de la base de datos
        String query = "SELECT Id_Producto, Nombre_producto, Cantidad, Precio FROM FACTURAS WHERE Fecha = ?";

        double totalNeto = 0; // Variable para acumular el Total Neto

        try (Connection conn = Conexion_DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, fechaSeleccionada);  // Usamos la fecha seleccionada
            ResultSet rs = stmt.executeQuery();

            // Limpiar la tabla antes de agregar nuevos datos
            model.setRowCount(0);

            // Llenar los datos en la tabla
            while (rs.next()) {
                int idProducto = rs.getInt("Id_Producto");
                String nombreProducto = rs.getString("Nombre_producto");
                int cantidad = rs.getInt("Cantidad");
                double precioUnitario = rs.getDouble("Precio");

                // Calcular el precio (Cantidad * Precio Unitario)
                double precioTotal = cantidad * precioUnitario;

                // Acumular el Total Neto
                totalNeto += precioTotal;

                // Agregar la fila con los datos
                model.addRow(new Object[]{idProducto, nombreProducto, cantidad, precioUnitario, precioTotal});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Calcular el Total IVA (15% del Total Neto)
        double totalIVA = totalNeto * 0.15;

        // Calcular el Total Factura (Total Neto + Total IVA)
        double totalFactura = totalNeto + totalIVA;

        // Agregar las filas de totales al final de la tabla
        model.addRow(new Object[]{"Total Neto", "", "", "", String.format("%.2f", totalNeto)});
        model.addRow(new Object[]{"Total IVA (15%)", "", "", "", String.format("%.2f", totalIVA)});
        model.addRow(new Object[]{"Total Factura", "", "", "", String.format("%.2f", totalFactura)});

        // Actualizar la JTable con el nuevo modelo
        tabla_productos_fecha.setModel(model);
    }







    //  Pone las fecha de las facturas en la Jlist
    private void obtenerFechasFacturas() {
        // Obtener el texto del JLabel Ingreso_del_correlogin
        String correoElectronico = Ingreso_del_correlogin.getText();

        // Consulta SQL para obtener las fechas agrupadas sin la hora, filtrando por el correo electrónico
        String query = "SELECT DISTINCT DATE(Fecha) AS Fecha FROM FACTURAS WHERE CorreoElectronico = ? ORDER BY Fecha";

        try (Connection conn = Conexion_DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Establecer el valor del parámetro (correo electrónico) en la consulta
            pstmt.setString(1, correoElectronico);

            // Ejecutar la consulta
            ResultSet rs = pstmt.executeQuery();

            // Formateador de fecha para mostrar solo el día (sin la hora)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // Limpiar el modelo antes de agregar nuevos elementos
            DefaultListModel<String> model = new DefaultListModel<>();
            lista_facturas_segun_fecha.setModel(model);

            // Recorrer los resultados de la consulta
            while (rs.next()) {
                Date fecha = rs.getDate("Fecha");
                String fechaFormateada = sdf.format(fecha);  // Formatear la fecha para mostrar solo la fecha
                model.addElement(fechaFormateada);  // Agregar la fecha formateada al modelo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private int obtenerIdCliente(String correoCliente) throws SQLException {
        String query = "SELECT Id FROM CLIENTE WHERE CorreoElectronico = ?";

        try (Connection conn = Conexion_DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, correoCliente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("Id");
            } else {
                return -1;  // Cliente no encontrado
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void copiarCarritoAFacturas(LocalDateTime fechaHora, String correoCliente, int idCliente, String nombreCliente) throws SQLException {
        String queryCarrito = "SELECT Id_Producto, Cantidad, Nombre_producto, Precio FROM CARRITO_DROP"; // No filtramos, tomamos todo el carrito

        try (Connection conn = Conexion_DB.getConnection();
             PreparedStatement stmtCarrito = conn.prepareStatement(queryCarrito)) {

            ResultSet rsCarrito = stmtCarrito.executeQuery();

            // Iniciar una transacción
            conn.setAutoCommit(false);

            // Insertar los productos en FACTURAS
            String queryFacturaDetalle = "INSERT INTO FACTURAS (Fecha, CorreoElectronico, Id_cliente, Nombre, Id_Producto, Cantidad, Nombre_producto, Precio) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmtFactura = conn.prepareStatement(queryFacturaDetalle)) {
                // Insertar los productos del carrito en la tabla FACTURAS
                while (rsCarrito.next()) {
                    int idProducto = rsCarrito.getInt("Id_Producto");
                    int cantidad = rsCarrito.getInt("Cantidad");
                    String nombreProducto = rsCarrito.getString("Nombre_producto");
                    double precio = rsCarrito.getDouble("Precio");

                    // Insertar los productos de la factura, junto con la información del cliente
                    stmtFactura.setTimestamp(1, Timestamp.valueOf(fechaHora)); // Fecha de la factura
                    stmtFactura.setString(2, correoCliente); // Correo del cliente
                    stmtFactura.setInt(3, idCliente); // Id del cliente
                    stmtFactura.setString(4, nombreCliente); // Nombre del cliente
                    stmtFactura.setInt(5, idProducto); // Id del producto
                    stmtFactura.setInt(6, cantidad); // Cantidad del producto
                    stmtFactura.setString(7, nombreProducto); // Nombre del producto
                    stmtFactura.setDouble(8, precio); // Precio del producto

                    stmtFactura.addBatch(); // Añadir la inserción a un batch
                }

                // Ejecutar todas las inserciones a la vez
                stmtFactura.executeBatch();

                // Commit de la transacción
                conn.commit();

            } catch (SQLException e) {
                conn.rollback(); // Rollback si ocurre un error
                throw new SQLException("Error al insertar productos en la factura", e);
            } finally {
                conn.setAutoCommit(true); // Restablecer el autocommit
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private void eliminarCarrito() {
        String query = "DELETE FROM CARRITO_DROP"; // Consulta SQL para eliminar todos los registros

        try (Connection conn = Conexion_DB.getConnection();
             Statement stmt = conn.createStatement()) {

            // Ejecutar la consulta
            int filasEliminadas = stmt.executeUpdate(query);

            // Verificar si se eliminaron filas
            if (filasEliminadas > 0) {
                JOptionPane.showMessageDialog(null, "Todos los productos han sido eliminados del carrito.");
                // Actualizar la tabla después de la eliminación
                cargarDatosCarrito();
            } else {
                JOptionPane.showMessageDialog(null, "No se encontraron productos en el carrito.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al eliminar los productos del carrito: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private void inicializarComboBox() {
        buscar_segun.removeAllItems();
        buscar_segun.addItem("Nombre");
        buscar_segun.addItem("Marca");
        buscar_segun.addItem("Precio");
    }

    private void cargarCategoriasEnTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Categorías");

        // Añadir categorías
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

        // Agregar las categorías al nodo raíz
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
            JOptionPane.showMessageDialog(null, "Error al cargar los datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        tabla_de_productos.setModel(modelo);

        tabla_de_productos.getColumnModel().getColumn(0).setPreferredWidth(145);
        tabla_de_productos.getColumnModel().getColumn(1).setPreferredWidth(145);
        tabla_de_productos.getColumnModel().getColumn(2).setPreferredWidth(145);

        tabla_de_productos.setRowHeight(180); // Ajuste del tamaño de las filas

        // Cambiar el TableCellRenderer para mostrar la imagen y el nombre
        tabla_de_productos.setDefaultRenderer(Object.class, new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                ProductoCelda producto = (ProductoCelda) value;
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());

                if (producto != null) {
                    // Panel para contener la imagen y el nombre
                    JLabel labelImagen = new JLabel();
                    if (producto.getImagen() != null) {
                        // Redimensionar la imagen para que encaje en la celda
                        Image imagen = producto.getImagen().getImage();  // Obtener la imagen
                        Image imagenRedimensionada = imagen.getScaledInstance(145, 180, Image.SCALE_SMOOTH);  // Redimensionar la imagen
                        labelImagen.setIcon(new ImageIcon(imagenRedimensionada));  // Establecer la imagen redimensionada
                    }

                    // Etiqueta para mostrar el nombre del producto debajo de la imagen
                    JLabel labelNombre = new JLabel(producto.getNombre(), SwingConstants.CENTER);
                    labelNombre.setVerticalAlignment(SwingConstants.TOP);
                    labelNombre.setHorizontalAlignment(SwingConstants.CENTER);

                    panel.add(labelImagen, BorderLayout.CENTER);
                    panel.add(labelNombre, BorderLayout.SOUTH); // Colocar el nombre debajo de la imagen

                    // Establecer los tamaños preferidos para las celdas (ajustar según sea necesario)
                    panel.setPreferredSize(new Dimension(145, 180));  // Ajusta esto según tus necesidades
                }
                return panel;
            }
        });
    }

    private void cargarDatosCarrito() {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("Nombre Producto");
        modelo.addColumn("Cantidad");
        modelo.addColumn("Precio");

        String query = "SELECT Nombre_producto, Cantidad, Precio FROM CARRITO_DROP";

        try (Connection conn = Conexion_DB.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Recorrer los resultados de la consulta
            while (rs.next()) {
                String nombreProducto = rs.getString("Nombre_producto");
                int cantidad = rs.getInt("Cantidad");
                double precio = rs.getDouble("Precio");

                // Crear una fila con los datos del producto
                Object[] fila = {nombreProducto, cantidad, precio};
                modelo.addRow(fila);  // Añadir la fila al modelo
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los datos del carrito: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Establecer el modelo en la JTable
        tabla_de_carrito.setModel(modelo);
    }



    private void mostrarDescripcion(String nombreProducto) throws SQLException {
        String query = "SELECT * FROM PRODUCTOS WHERE Nombre = ?";
        try (Connection conn = Conexion_DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombreProducto);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String descripcion = rs.getString("Descripcion");
                int stockDisponible = rs.getInt("Stock");
                double precio = rs.getDouble("Precio");
                String marca = rs.getString("Marca");

                // Mostrar la descripción en un JOptionPane
                Object[] options = {"Comprar", "Cerrar"};
                int option = JOptionPane.showOptionDialog(null,
                        "Nombre: " + nombreProducto + "\nMarca: " + marca +
                                "\nPrecio: $" + precio + "\nStock: " + stockDisponible + "\nDescripción: " + descripcion,
                        "Descripción del Producto", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                // Si el usuario hace clic en "Comprar"
                if (option == JOptionPane.YES_OPTION) {
                    String cantidadStr = JOptionPane.showInputDialog("Ingrese la cantidad a comprar:");
                    int cantidad = Integer.parseInt(cantidadStr);

                    if (cantidad > 0 && cantidad <= stockDisponible) {
                        agregarAlCarrito(nombreProducto, cantidad);
                        JOptionPane.showMessageDialog(null, "Producto agregado al carrito.");
                        cargarDatosCarrito();
                    } else {
                        JOptionPane.showMessageDialog(null, "Cantidad no disponible.");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void agregarAlCarrito(String nombreProducto, int cantidad) throws Exception {
        try (Connection conn = Conexion_DB.getConnection()) {
            String query = "INSERT INTO CARRITO_DROP (Id_producto, Nombre_producto, Cantidad, Precio) " +
                    "SELECT Id, Nombre, ?, Precio FROM PRODUCTOS WHERE Nombre = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, cantidad);
                stmt.setString(2, nombreProducto);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class ProductoCelda {
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
