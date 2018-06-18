package tikape.reseptiarkisto.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private String databaseAddress;

    public Database(String databaseAddress) throws ClassNotFoundException {
        this.databaseAddress = databaseAddress;
    }
    
    public Connection getConnection() throws SQLException {
        if (databaseAddress != null && databaseAddress.length() > 0) {
            return DriverManager.getConnection(databaseAddress);
        }
        
        return DriverManager.getConnection("jdbc:sqlite:reseptiarkisto.db");
    }
}
