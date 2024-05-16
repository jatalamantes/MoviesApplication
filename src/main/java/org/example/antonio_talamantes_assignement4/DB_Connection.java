package org.example.antonio_talamantes_assignement4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_Connection {
    public static final String databaseURL = "jdbc:ucanaccess://./MoviesDB.accdb";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseURL);
    }
}
