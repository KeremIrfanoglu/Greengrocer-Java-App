package com.greengrocer.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ShowDatabases {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "myuser";
        String pass = "1234";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {

            System.out.println("Available Databases:");
            while (rs.next()) {
                System.out.println("- " + rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
