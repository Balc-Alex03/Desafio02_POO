package datos;

import beans.CategoriaBeans;
import java.sql.*;
import java.util.*;
import util.Conexion;

public class CategoriaDatos {

    public List<CategoriaBeans> listar() throws SQLException {
        String sql = "SELECT id_categoria, nombre_categoria FROM categoria ORDER BY nombre_categoria";
        List<CategoriaBeans> categorias = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new CategoriaBeans(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre_categoria")));
            }
        }
        return categorias;
    }
}
