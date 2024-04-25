package net.agent.SchedulingAgent.Behaviour;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.agent.DSMInformation.DSMInformation;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * Manages SQL database connections and transactions for the SchedulingAgent,
 * allowing for data retrieval and updates in a remote SQL database.
 */
public class SQLDatabaseConnector {

    private SchedulingAgent schedulingAgent;

    public SQLDatabaseConnector(SchedulingAgent schedulingAgent) {
        this.schedulingAgent = schedulingAgent;
    }

    /**
     * Reads data from a predefined SQL database and stores it in the DSMInformation model.
     */
    public void readDataFromDatabase() {
        String url = "jdbc:mysql://127.0.0.1:3306/emoduledatabase";
        String user = "externalUser";
        String password = "H2Giga";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            String query = "SELECT * FROM dsmdata";
            ResultSet resultSet = statement.executeQuery(query);

            DSMInformation dsmInformation = schedulingAgent.getInternalDataModel().getDSMInformation();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                double demand = resultSet.getDouble("demand");
                double electricityPrice = resultSet.getDouble("electricityprice");
                double powerIn = resultSet.getDouble("power_in");
                double productionQuantity = resultSet.getDouble("productionquantity");
                double tanklevel = resultSet.getDouble("tanklevel");
                int timestep = resultSet.getInt("timestep");

                dsmInformation.addExternalDSMInformation(id, demand, electricityPrice, powerIn, productionQuantity,
                        tanklevel, timestep);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts operational data for a PEA agent into the peasdata table in the database.
     * @param id The identifier for the PEA agent.
     * @param efficiency The operational efficiency of the agent.
     * @param lowerOperatingLimit The lower operational limit of the agent.
     * @param productionRate The current production rate of the agent.
     * @param upperOperatingLimit The upper operational limit of the agent.
     */
    public void insertDataIntoPeasData(int id, double efficiency, double lowerOperatingLimit, double productionRate,
                                       double upperOperatingLimit) {

        String url = "jdbc:mysql://10.246.55.91:3306/emoduledatabase";
        String user = "externalUser";
        String password = "H2Giga";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "INSERT INTO peasdata (id, efficiency, loweroperatinglimit, productionrate, upperoperatinglimit) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, id);
                preparedStatement.setDouble(2, efficiency);
                preparedStatement.setDouble(3, lowerOperatingLimit);
                preparedStatement.setDouble(4, productionRate);
                preparedStatement.setDouble(5, upperOperatingLimit);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
