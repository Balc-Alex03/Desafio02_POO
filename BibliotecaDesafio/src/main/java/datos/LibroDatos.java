package datos;

import beans.LibroBeans;
import java.sql.*;
import java.util.*;
import util.Conexion;

// El CRUD de la tabla libro con prepared statement

public class LibroDatos {

    private static final String SQL_LISTAR =
            "SELECT l.id_libro, l.titulo, l.`año_publicacion`, "
            + "       l.id_autor, a.nombre AS autor, "
            + "       l.id_categoria, c.nombre_categoria AS categoria "
            + "FROM libro l "
            + "INNER JOIN autor a ON a.id_autor = l.id_autor "
            + "INNER JOIN categoria c ON c.id_categoria = l.id_categoria "
            + "WHERE (? = 0 OR l.id_autor = ?) "
            + "  AND (? = 0 OR l.id_categoria = ?) "
            + "ORDER BY l.id_libro";

    private static final String SQL_INSERTAR =
            "INSERT INTO libro (titulo, `año_publicacion`, id_autor, id_categoria) VALUES (?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE libro SET titulo = ?, `año_publicacion` = ?, id_autor = ?, id_categoria = ? "
            + "WHERE id_libro = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM libro WHERE id_libro = ?";

    private static final String SQL_DUPLICADO =
            "SELECT COUNT(*) FROM libro WHERE titulo = ? AND id_autor = ? AND id_libro <> ?";

    // todos los libros
    
    public List<LibroBeans> listar() throws SQLException {
        return listar(0, 0);
    }

    // Para filtrar los libros. 0 Significa sin filtro
    
    public List<LibroBeans> listar(int idAutor, int idCategoria) throws SQLException {
        List<LibroBeans> libros = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR)) {
            ps.setInt(1, idAutor);
            ps.setInt(2, idAutor);
            ps.setInt(3, idCategoria);
            ps.setInt(4, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LibroBeans libro = new LibroBeans(
                            rs.getInt("id_libro"),
                            rs.getString("titulo"),
                            rs.getInt("año_publicacion"),
                            rs.getInt("id_autor"),
                            rs.getInt("id_categoria"));
                    libro.setNombreAutor(rs.getString("autor"));
                    libro.setNombreCategoria(rs.getString("categoria"));
                    libros.add(libro);
                }
            }
        }
        return libros;
    }

    public boolean insertar(LibroBeans libro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
            ps.setString(1, libro.getTitulo());
            ps.setInt(2, libro.getAnioPublicacion());
            ps.setInt(3, libro.getIdAutor());
            ps.setInt(4, libro.getIdCategoria());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(LibroBeans libro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, libro.getTitulo());
            ps.setInt(2, libro.getAnioPublicacion());
            ps.setInt(3, libro.getIdAutor());
            ps.setInt(4, libro.getIdCategoria());
            ps.setInt(5, libro.getIdLibro());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idLibro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        }
    }
    
    /*
      Para indicar que el autor ya tiene un libro con ese titulo
      @param idLibroExcluido id del libro que se está editando (0 al insertar)
    
      - Jarvis
     */
    
    public boolean existeDuplicado(String titulo, int idAutor, int idLibroExcluido) throws SQLException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_DUPLICADO)) {
            ps.setString(1, titulo);
            ps.setInt(2, idAutor);
            ps.setInt(3, idLibroExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
