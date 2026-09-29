package vista;

import beans.*;
import datos.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

// Pantalla principal - formulario de libros, filtros y tabla

public class frmBiblioteca extends JFrame {

    // Colores
    
    private static final Color ACENTO = new Color(0x1e9e80);
    private static final Color LINEA = new Color(0x4A4A4A);

    // Capa de datos
    
    private final LibroDatos libroDatos = new LibroDatos();
    private final AutorDatos autorDatos = new AutorDatos();
    private final CategoriaDatos categoriaDatos = new CategoriaDatos();

    // Modelos para el ComboBox y la tabla - muestran lo que haya en estos modelos
    
    private final DefaultComboBoxModel<AutorBeans> modeloAutor = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<CategoriaBeans> modeloCategoria = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<AutorBeans> modeloFiltroAutor = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<CategoriaBeans> modeloFiltroCategoria = new DefaultComboBoxModel<>();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Título", "Año", "Autor", "Categoría"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false; 
        }
    };

    // Controles de la ventana
    
    private final JTextField txtTitulo = new JTextField(30);
    private final JTextField txtAnio = new JTextField(6);
    private final JComboBox<AutorBeans> cboAutor = new JComboBox<>(modeloAutor);
    private final JComboBox<CategoriaBeans> cboCategoria = new JComboBox<>(modeloCategoria);
    private final JComboBox<AutorBeans> cboFiltroAutor = new JComboBox<>(modeloFiltroAutor);
    private final JComboBox<CategoriaBeans> cboFiltroCategoria = new JComboBox<>(modeloFiltroCategoria);
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JTable tblLibros = new JTable(modeloTabla);

    private List<LibroBeans> libros = new ArrayList<>(); // libros que muestra la tabla 
    private LibroBeans seleccionado;                     // libro elegido en la tabla 
    private boolean cargando = false;                    // evita eventos mientras se llena la tabla

    public frmBiblioteca() {
        super("Desafio02 - Biblioteca Digital");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirVentana();

        tblLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLibros.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        tblLibros.getColumnModel().getColumn(1).setPreferredWidth(300);  // Título
        tblLibros.getColumnModel().getColumn(2).setPreferredWidth(60);   // Año
        tblLibros.getColumnModel().getColumn(3).setPreferredWidth(200);  // Autor
        tblLibros.getColumnModel().getColumn(4).setPreferredWidth(130);  // Categoría
        tblLibros.setRowHeight(26);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        tblLibros.getColumnModel().getColumn(0).setCellRenderer(centrado); // ID
        tblLibros.getColumnModel().getColumn(2).setCellRenderer(centrado); // Año
        if (cargarCombos()) {
            cargarTabla();
        }
        limpiar();

        // Eventos
        
        btnGuardar.addActionListener(e -> guardar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        cboFiltroAutor.addActionListener(e -> refrescar());
        cboFiltroCategoria.addActionListener(e -> refrescar());
        tblLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !cargando) {
                mostrarSeleccionado();
            }
        });

        pack();
        
        // para que la ventana no se pueda encojer mas de su tamaño base
        
        setMinimumSize(getSize()); 
        setLocationRelativeTo(null);
    }

    // Diseño de la ventana

    private void construirVentana() {
        
        // Título principal
        
        JLabel titulo = new JLabel("Biblioteca Digital");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 28f));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        // Seccion 1 - datos del libro
        
        JPanel seccionDatos = new JPanel(new BorderLayout(0, 10));
        seccionDatos.add(crearSubtitulo("Datos del libro"), BorderLayout.NORTH);
        seccionDatos.add(crearFormulario(), BorderLayout.CENTER);

        // Seccion 2 - libros registrados
        
        JPanel cabeceraLibros = new JPanel(new BorderLayout(0, 10));
        cabeceraLibros.add(crearSubtitulo("Libros registrados"), BorderLayout.NORTH);
        cabeceraLibros.add(crearFiltros(), BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(tblLibros);
        scroll.setPreferredSize(new Dimension(760, 260));

        JPanel seccionLibros = new JPanel(new BorderLayout(0, 10));
        seccionLibros.add(cabeceraLibros, BorderLayout.NORTH);
        seccionLibros.add(scroll, BorderLayout.CENTER);

        // Las dos secciones con espacio entre ellas
        
        JPanel cuerpo = new JPanel(new BorderLayout(0, 30));
        cuerpo.add(seccionDatos, BorderLayout.NORTH);
        cuerpo.add(seccionLibros, BorderLayout.CENTER);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 24, 22, 24));
        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(cuerpo, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    // Titulo y linea debajo
    
    private JLabel crearSubtitulo(String texto) {
        JLabel subtitulo = new JLabel(texto);
        subtitulo.setFont(subtitulo.getFont().deriveFont(Font.BOLD, 16f));
        subtitulo.setForeground(ACENTO);
        subtitulo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINEA),
                BorderFactory.createEmptyBorder(0, 0, 6, 0)));
        return subtitulo;
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        agregar(panel, new JLabel("Título:"), 0, 0, 0);
        agregar(panel, txtTitulo, 1, 0, 1);
        agregar(panel, new JLabel("Año de publicación:"), 2, 0, 0);
        agregar(panel, txtAnio, 3, 0, 0);
        agregar(panel, new JLabel("Autor:"), 0, 1, 0);
        agregar(panel, cboAutor, 1, 1, 1);
        agregar(panel, new JLabel("Categoría:"), 2, 1, 0);
        agregar(panel, cboCategoria, 3, 1, 0);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 4;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(14, 0, 0, 0);
        panel.add(botones, g);
        return panel;
    }

    private JPanel crearFiltros() {
        JPanel panel = new JPanel(new GridBagLayout());
        agregar(panel, new JLabel("Filtrar por autor:"), 0, 0, 0);
        agregar(panel, cboFiltroAutor, 1, 0, 0);
        agregar(panel, new JLabel("y categoría:"), 2, 0, 0);
        agregar(panel, cboFiltroCategoria, 3, 0, 0);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 4;
        g.weightx = 1;
        panel.add(Box.createHorizontalGlue(), g);
        return panel;
    }

    // Coloca un control en la cuadrícula cuánto se estira
    
    private void agregar(JPanel panel, Component control, int x, int y, double pesoX) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.weightx = pesoX;
        g.fill = GridBagConstraints.HORIZONTAL;
        boolean esCampo = (x == 1 || x == 3);
        g.insets = new Insets(4, esCampo ? 8 : 0, 4, x == 1 ? 24 : 0);
        panel.add(control, g);
    }

    // Carga de datos utilizando los beans

    private boolean cargarCombos() {
        try {
            modeloAutor.addElement(new AutorBeans(0, "Seleccione un autor", ""));
            modeloFiltroAutor.addElement(new AutorBeans(0, "Todos los autores", ""));
            modeloCategoria.addElement(new CategoriaBeans(0, "Seleccione una categoría"));
            modeloFiltroCategoria.addElement(new CategoriaBeans(0, "Todas las categorías"));

            for (AutorBeans autor : autorDatos.listar()) {
                modeloAutor.addElement(autor);
                modeloFiltroAutor.addElement(autor);
            }
            for (CategoriaBeans categoria : categoriaDatos.listar()) {
                modeloCategoria.addElement(categoria);
                modeloFiltroCategoria.addElement(categoria);
            }
            return true;
        } catch (SQLException ex) {
            mostrarError(ex, "cargar los autores y categorías");
            return false;
        }
    }

    // Consulta los libros aplicando los filtros
    
    private void cargarTabla() {
        AutorBeans autorFiltro = (AutorBeans) cboFiltroAutor.getSelectedItem();
        CategoriaBeans categoriaFiltro = (CategoriaBeans) cboFiltroCategoria.getSelectedItem();
        int idAutor = autorFiltro == null ? 0 : autorFiltro.getIdAutor();
        int idCategoria = categoriaFiltro == null ? 0 : categoriaFiltro.getIdCategoria();
        try {
            libros = libroDatos.listar(idAutor, idCategoria);
            cargando = true;
            modeloTabla.setRowCount(0);
            for (LibroBeans libro : libros) {
                modeloTabla.addRow(new Object[]{
                    libro.getIdLibro(),
                    libro.getTitulo(),
                    libro.getAnioPublicacion(),
                    libro.getNombreAutor(),
                    libro.getNombreCategoria()
                });
            }
            cargando = false;
        } catch (SQLException ex) {
            mostrarError(ex, "consultar los libros");
        }
    }

    // Botones

    private void guardar() {
        LibroBeans libro = leerFormulario();
        if (libro == null) {
            return;
        }
        try {
            if (libroDatos.existeDuplicado(libro.getTitulo(), libro.getIdAutor(), 0)) {
                aviso("Este autor ya tiene registrado un libro con ese título.");
                return;
            }
            libroDatos.insertar(libro);
            informar("El libro se guardó correctamente.");
            refrescar();
        } catch (SQLException ex) {
            mostrarError(ex, "guardar el libro");
        }
    }

    private void editar() {
        if (seleccionado == null) {
            aviso("Primero seleccione en la tabla el libro que desea editar.");
            return;
        }
        LibroBeans libro = leerFormulario();
        if (libro == null) {
            return;
        }
        libro.setIdLibro(seleccionado.getIdLibro());
        try {
            if (libroDatos.existeDuplicado(libro.getTitulo(), libro.getIdAutor(), libro.getIdLibro())) {
                aviso("Este autor ya tiene registrado otro libro con ese título.");
                return;
            }
            libroDatos.actualizar(libro);
            informar("Los cambios se guardaron correctamente.");
            refrescar();
        } catch (SQLException ex) {
            mostrarError(ex, "actualizar el libro");
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            aviso("Primero seleccione en la tabla el libro que desea eliminar.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el libro \"" + seleccionado.getTitulo() + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            libroDatos.eliminar(seleccionado.getIdLibro());
            informar("El libro se eliminó correctamente.");
            refrescar();
        } catch (SQLException ex) {
            mostrarError(ex, "eliminar el libro");
        }
    }

    // Vacía el formulario y quita la selección de la tabla
    
    private void limpiar() {
        cargando = true;
        tblLibros.clearSelection();
        cargando = false;
        txtTitulo.setText("");
        txtAnio.setText("");
        if (modeloAutor.getSize() > 0) {
            cboAutor.setSelectedIndex(0);
        }
        if (modeloCategoria.getSize() > 0) {
            cboCategoria.setSelectedIndex(0);
        }
        seleccionado = null;
        actualizarBotones();
        txtTitulo.requestFocusInWindow();
    }

    // Deja el formulario limpio y vuelve a consultar la tabla
    
    private void refrescar() {
        limpiar();
        cargarTabla();
    }

    // Pasa al formulario el libro elegido en la tabla
    
    private void mostrarSeleccionado() {
        int fila = tblLibros.getSelectedRow();
        if (fila < 0 || fila >= libros.size()) {
            return;
        }
        seleccionado = libros.get(fila);
        txtTitulo.setText(seleccionado.getTitulo());
        txtAnio.setText(String.valueOf(seleccionado.getAnioPublicacion()));
        for (int i = 0; i < modeloAutor.getSize(); i++) {
            if (modeloAutor.getElementAt(i).getIdAutor() == seleccionado.getIdAutor()) {
                cboAutor.setSelectedIndex(i);
            }
        }
        for (int i = 0; i < modeloCategoria.getSize(); i++) {
            if (modeloCategoria.getElementAt(i).getIdCategoria() == seleccionado.getIdCategoria()) {
                cboCategoria.setSelectedIndex(i);
            }
        }
        actualizarBotones();
    }

    // Hace que guarde solo con un formulario nuevo. Editar y Eliminar solo con un libro elegido
    
    private void actualizarBotones() {
        boolean hayLibro = seleccionado != null;
        btnGuardar.setEnabled(!hayLibro);
        btnEditar.setEnabled(hayLibro);
        btnEliminar.setEnabled(hayLibro);
    }

    /* 
     Validacion
     Devuelve el libro con lo escrito en el formulario o null si algún dato es incorrecto
    */
    
    private LibroBeans leerFormulario() {
        String titulo = txtTitulo.getText().trim().replaceAll("\\s+", " ");
        if (titulo.isEmpty()) {
            aviso("Escriba el título del libro.");
            return null;
        }
        if (titulo.length() > 150) {
            aviso("El título no puede tener más de 150 caracteres.");
            return null;
        }

        String textoAnio = txtAnio.getText().trim();
        if (textoAnio.isEmpty()) {
            aviso("Escriba el año de publicación.");
            return null;
        }
        int anio;
        try {
            anio = Integer.parseInt(textoAnio);
        } catch (NumberFormatException ex) {
            aviso("El año de publicación debe ser un número de 4 dígitos, por ejemplo 1967.");
            return null;
        }
        int anioActual = Year.now().getValue();
        if (anio < 1000 || anio > anioActual) {
            aviso("El año de publicación debe estar entre 1000 y " + anioActual + ".");
            return null;
        }

        AutorBeans autor = (AutorBeans) cboAutor.getSelectedItem();
        if (autor == null || autor.getIdAutor() == 0) {
            aviso("Seleccione el autor del libro.");
            return null;
        }
        CategoriaBeans categoria = (CategoriaBeans) cboCategoria.getSelectedItem();
        if (categoria == null || categoria.getIdCategoria() == 0) {
            aviso("Seleccione la categoría del libro.");
            return null;
        }

        LibroBeans libro = new LibroBeans();
        libro.setTitulo(titulo);
        libro.setAnioPublicacion(anio);
        libro.setIdAutor(autor.getIdAutor());
        libro.setIdCategoria(categoria.getIdCategoria());
        return libro;
    }

    // Mensajes con el Jpane

    private void informar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revise los datos", JOptionPane.WARNING_MESSAGE);
    }

    // Muestra un mensaje claro para el usuario mientras que el detalle técnico solo va a la consola
    
    private void mostrarError(SQLException ex, String accion) {
        System.err.println("[Biblioteca] Error al " + accion + ": " + ex);
        String estado = ex.getSQLState() == null ? "" : ex.getSQLState();
        String causa;
        if (ex.getErrorCode() == 1049) {
            causa = "La base de datos biblioteca_db no existe. Impórtela desde phpMyAdmin con biblioteca_db.sql.";
        } else if (ex.getErrorCode() == 1045 || ex.getErrorCode() == 1698) {
            causa = "El usuario o la contraseña de la base de datos no son correctos.";
        } else if (estado.startsWith("08")) {
            causa = "No hay conexión con la base de datos. Verifique que MariaDB esté iniciado.";
        } else {
            causa = "Ocurrió un problema inesperado con la base de datos. Intente de nuevo.";
        }
        JOptionPane.showMessageDialog(this, "No se pudo " + accion + ".\n" + causa,
                "No fue posible completar la acción", JOptionPane.ERROR_MESSAGE);
    }

    // Colores propios con Nimbus
    
    private static void aplicarTema() {
        Color fondo = Color.decode("#2b2b2b");
        Color fondoCajas = Color.decode("#3c3c3c"); // tablas, campos de texto y combos
        Color texto = Color.WHITE;

        UIManager.put("control", fondo);                    // fondo de la ventana y los paneles
        UIManager.put("text", texto);                       // color del texto
        UIManager.put("nimbusBase", Color.decode("#1a1a1a")); // botones, bordes y barras
        UIManager.put("nimbusLightBackground", fondoCajas);
        UIManager.put("nimbusSelectionBackground", Color.decode("#235247")); // fila seleccionada
        UIManager.put("nimbusSelectedText", texto);
        UIManager.put("Label.foreground", texto);
        UIManager.put("Table.selectionForeground", texto);
        UIManager.put("Table[Enabled+Selected].textForeground", texto);
    }

    public static void main(String[] args) {
        try {
            aplicarTema();
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Si Nimbus no está disponible se usa el aspecto por defecto
        }
        EventQueue.invokeLater(() -> new frmBiblioteca().setVisible(true));
    }
}