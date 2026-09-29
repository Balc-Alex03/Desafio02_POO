package datos;

import beans.AutorBeans;
import java.sql.*;
import java.util.*;
import util.Conexion;

public class AutorDatos {

    public List<AutorBeans> listar() throws SQLException {
        String sql = "SELECT id_autor, nombre, nacionalidad FROM autor ORDER BY nombre";
        List<AutorBeans> autores = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                autores.add(new AutorBeans(
                        rs.getInt("id_autor"),
                        rs.getString("nombre"),
                        rs.getString("nacionalidad")));
            }
        }
        return autores;
    }
}
