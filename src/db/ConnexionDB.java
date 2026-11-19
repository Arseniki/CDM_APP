package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionDB {
    private static final String URL = "jdbc:postgresql://localhost:5432/coupe_du_monde";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin"; // <-- À adapter sur chaque PC si différent

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
