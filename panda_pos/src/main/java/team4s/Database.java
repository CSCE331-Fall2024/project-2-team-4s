package team4s;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_4s_db";
    private static String USER = "team_4s";
    private static String PASSWORD = "quietheat74";
    private static Connection conn;

    // connect to database
    public static Connection connect() throws SQLException {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }
}
