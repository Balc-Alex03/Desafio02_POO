package util;

import java.sql.*;

//  Cada clase del paquete datos pide una conexión nueva y la cierra con el try with resources

public class Conexion {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String SERVIDOR = "localhost";
    private static final String PUERTO = "3306";
    private static final String BASE_DATOS = "biblioteca_db";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    private static final String URL = "jdbc:mysql://" + SERVIDOR + ":" + PUERTO + "/" + BASE_DATOS
            + "?useUnicode=true&characterEncoding=UTF-8"
            + "&useSSL=false&allowPublicKeyRetrieval=true"
            + "&useAffectedRows=false&serverTimezone=UTC";

    private Conexion() {
        // Es una clase unitaria, no se instancia
    }

    public static Connection getConexion() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontró el driver JDBC " + DRIVER, "JDBC_DRIVER", ex);
        }
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
