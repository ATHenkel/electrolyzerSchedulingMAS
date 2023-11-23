package net.agent.SchedulingAgent.Behaviour;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcDatabaseConnector {

    public void readDataFromDatabase() {
        String url = "jdbc:mysql://10.246.55.91:3306/emoduledatabase";
        String user = "externalUser";
        String password = "H2Giga";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            String query = "SELECT * FROM dsmdata";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("id"));
                System.out.println("Demand: " + resultSet.getDouble("demand"));
                System.out.println("ElectricityPrice: " + resultSet.getDouble("electricityprice"));
                System.out.println("PowerIn: " + resultSet.getDouble("power_in"));
                System.out.println("ProductionQuantity: " + resultSet.getDouble("productionquantity"));
                System.out.println("Tanklevel: " + resultSet.getDouble("tanklevel"));
                System.out.println("Timestep: " + resultSet.getDouble("timestep"));

                System.out.println("-----------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
