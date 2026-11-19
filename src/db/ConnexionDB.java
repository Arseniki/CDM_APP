package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe de connexion centralisee a la base MySQL.
 * Ne pas modifier apres le commit initial : tous les modules en dependent.
 */
public class ConnexionDB {

    // Parametres de connexion - a adapter localement si besoin
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "coupe_du_monde";
    private static final String USER     = "cdm_user";
    private static final String PASSWORD = "cdm2022";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC"
            + "&characterEncoding=UTF-8";

    private static Connection connection = null;

    private ConnexionDB() {
        // Classe utilitaire : pas d'instanciation
    }

    /**
     * Retourne une connexion unique (singleton) a la base MySQL.
     * Recree la connexion si elle est fermee.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "Pilote MySQL introuvable. Verifiez que mysql-connector-java.jar est dans lib/ et lie au projet.",
                        e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Ferme la connexion si elle est ouverte.
     */
    public static void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}