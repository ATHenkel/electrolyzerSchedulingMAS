package net.agent.SchedulingAgent.Behaviour;

import java.util.ArrayList;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class GetGoalOfProduction extends OneShotBehaviour {
	
	SchedulingAgent schedulingAgent;
	
	public GetGoalOfProduction(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	
	@Override
	public void action() {
		
		//TODO: Test for Shutdown Order
		ArrayList<Integer> shutdownOrderList = this.schedulingAgent.getInternalDataModel().getShutdownOrderList();
		shutdownOrderList.add(2);
		shutdownOrderList.add(1);
		shutdownOrderList.add(3);
		
		//Read Values from SQL-Database
		System.out.println("Agent " + this.schedulingAgent.getLocalName() + " connect to SQL-Database");
		SQLDatabaseConnector connector = new SQLDatabaseConnector(schedulingAgent);
		connector.readDataFromDatabase();
		
		//Next behaviour to be executed
		MinimizeX minimizeX = new MinimizeX(schedulingAgent);
		this.schedulingAgent.addBehaviour(minimizeX);
	}

}