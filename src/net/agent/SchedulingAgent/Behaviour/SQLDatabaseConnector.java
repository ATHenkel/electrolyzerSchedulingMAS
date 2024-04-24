package net.agent.SchedulingAgent.Behaviour;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.agent.DSMInformation.DSMInformation;
import net.agent.SchedulingAgent.SchedulingAgent;

public class SQLDatabaseConnector {

	SchedulingAgent schedulingAgent;

	public SQLDatabaseConnector(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	public void readDataFromDatabase() {
		
	   	//Get Agent-ID as Integer
			String localName = this.schedulingAgent.getLocalName();
			int agentId;
			try {
				agentId = Integer.parseInt(localName);
			} catch (NumberFormatException e) {
				agentId = -1; // Default value if the conversion fails.
			}

		// Get Internal DataBase to store information from SQL-DB
		DSMInformation dsmInformation = schedulingAgent.getInternalDataModel().getDSMInformation();

		// SQL-DB Connection information
		String url = "jdbc:mysql://127.0.0.1:3306/emoduledatabase";
		String user = "externalUser";
		String password = "H2Giga";

		// Connect to SQL-DB
		try (Connection connection = DriverManager.getConnection(url, user, password);
				Statement statement = connection.createStatement()) {

			// Get all Data from Table dsmdata
			String query = "SELECT * FROM dsmdata";
			ResultSet resultSet = statement.executeQuery(query);

			// Write all Values to internal Knowledgebase
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				double demand = resultSet.getDouble("demand");
				double electricityPrice = resultSet.getDouble("electricityprice");
				double powerIn = resultSet.getDouble("power_in");
				double productionQuantity = resultSet.getDouble("productionquantity");
				double tanklevel = resultSet.getDouble("tanklevel");
				int timestep = resultSet.getInt("timestep");
				
				// Add Values to dsminformation table
				dsmInformation.addExternalDSMInformation(id, demand, electricityPrice, powerIn, productionQuantity,
						tanklevel, timestep);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public void insertDataIntoPeasData(int id, double efficiency, double lowerOperatingLimit, double productionRate,
			double upperOperatingLimit) {

		// SQL-DB Connection information
		String url = "jdbc:mysql://10.246.55.91:3306/emoduledatabase";
		String user = "externalUser";
		String password = "H2Giga";

		// Connect to SQL-DB
		try (Connection connection = DriverManager.getConnection(url, user, password)) {

			// SQL statement for inserting data into the peasdata table
			String query = "INSERT INTO peasdata (id, efficiency, loweroperatinglimit, productionrate, upperoperatinglimit) "
					+ "VALUES (?, ?, ?, ?, ?)";

			// Use prepared statement to avoid SQL injection
			try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
				preparedStatement.setInt(1, id);
				preparedStatement.setDouble(2, efficiency);
				preparedStatement.setDouble(3, lowerOperatingLimit);
				preparedStatement.setDouble(4, productionRate);
				preparedStatement.setDouble(5, upperOperatingLimit);

				// Executing the insert statement
				preparedStatement.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
