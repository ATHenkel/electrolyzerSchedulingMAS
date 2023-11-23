package net.agent.SchedulingAgent.Behaviour;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.DSMInformation.DSMInformation;
import net.agent.SchedulingAgent.SchedulingAgent;

public class GetGoalOfProduction extends OneShotBehaviour {
	
	SchedulingAgent schedulingAgent;
	
	public GetGoalOfProduction(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	
	@Override
	public void action() {
    	//Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
		
		DSMInformation dsmInformation = schedulingAgent.getInternalDataModel().getDSMInformation();
		
		 //10 Electrolyzer, 6 Periods
//		 dsmInformation.addExternalDSMInformation(1, 0.175, 19.48/1000); 
//		 dsmInformation.addExternalDSMInformation(2, 0.211, 55.54/1000); 
//		 dsmInformation.addExternalDSMInformation(3, 0.156 , 54.09/1000); 
//		 dsmInformation.addExternalDSMInformation(4, 0.378, 33.42/1000);
//		 dsmInformation.addExternalDSMInformation(5, 0.265, 49.6/1000);
//		 dsmInformation.addExternalDSMInformation(6, 0.187, 63.95/1000);

		
		// 3 Electrolyzer, 12 Periods
		 dsmInformation.addExternalDSMInformation(1, 0.1334, 19.48/1000); 
		 dsmInformation.addExternalDSMInformation(2, 0.0781, 55.54/1000); 
		 dsmInformation.addExternalDSMInformation(3, 0.0669 , 54.09/1000); 
		 dsmInformation.addExternalDSMInformation(4, 0.1262, 33.42/1000);
		 dsmInformation.addExternalDSMInformation(5, 0.0444, 49.6/1000);
		 dsmInformation.addExternalDSMInformation(6, 0.0469, 63.95/1000);
		 dsmInformation.addExternalDSMInformation(7, 0.0881, 10.01/1000);
		 dsmInformation.addExternalDSMInformation(8, 0.0989, 103.94/1000);
		 dsmInformation.addExternalDSMInformation(9, 0.0271, 105.42/1000);
		 dsmInformation.addExternalDSMInformation(10, 0.0758, 116.16/1000);
		 dsmInformation.addExternalDSMInformation(11, 0.0344, 109.01/1000);
		 dsmInformation.addExternalDSMInformation(12, 0.0812, 105.55/1000);
		 
		 
		 //Connection SQL-Database 
			if (agentId == 1) {
				 System.out.println("Connect to SQL-Database:");
				 JdbcDatabaseConnector connector = new JdbcDatabaseConnector();
		         connector.readDataFromDatabase();
			}
	
		 	
		//Next behaviour to be executed
		MinimizeX minimizeX = new MinimizeX(schedulingAgent);
		this.schedulingAgent.addBehaviour(minimizeX);
	}

}