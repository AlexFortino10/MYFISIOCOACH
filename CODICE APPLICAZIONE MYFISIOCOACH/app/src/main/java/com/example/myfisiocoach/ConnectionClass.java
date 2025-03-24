package com.example.myfisiocoach;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    private static final String URL = "jdbc:mysql://192.168.1.49:3306/myfisiocoach";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    public static Connection connection;

   //Metodo per connettersi al database
    public static Connection connect() {

        try {

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("Connected to the database" + connection);
            if (connection != null) {
                System.out.println("Connection successful");
            } else {
                System.out.println("Connection failed");
            }
        }catch(ClassNotFoundException | SQLException e) {

            e.printStackTrace();
            System.out.println("Connection failed");

        }

        return connection;
    }
}
