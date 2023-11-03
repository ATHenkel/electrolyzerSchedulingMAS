package net.agent.SchedulingAgent;

import jade.core.AID;
import jade.core.Agent;
import net.agent.SchedulingAgent.Behaviour.GetGoalOfProduction;
import net.agent.SchedulingAgent.Behaviour.MessageReceiveBehaviour;
import net.agent.SchedulingAgent.Behaviour.OPCUAConnection;

public class SchedulingAgent extends Agent {

	private static final long serialVersionUID = 1L;
	InternalDataModel internalDataModel = new InternalDataModel();		
	
	public InternalDataModel getInternalDataModel() {
		return internalDataModel;
	}

	public void setInternalDataModel(InternalDataModel internalDataModel) {
		this.internalDataModel = internalDataModel;
	}
	
	protected void setup() {
		
		//Will be called during instantiation
		MessageReceiveBehaviour messageReceiveBehaviour = new MessageReceiveBehaviour(this);
		this.addBehaviour(messageReceiveBehaviour);
		
	//	OPCUAConnection opcuaConnection = new OPCUAConnection(this);
	//	this.addBehaviour(opcuaConnection);
		
		GetGoalOfProduction getGoalOfProduction = new GetGoalOfProduction(this);
		this.addBehaviour(getGoalOfProduction);
		
		//Add Agents to PhoneBook
		for (int i = 0; i < 3; i++) {
			AID agentAID = new AID(String.valueOf(i+1),AID.ISLOCALNAME);
			if (agentAID!=this.getAID()) {
				 this.getInternalDataModel().addAID2PhoneBook(agentAID);
			}
		}
	}
	

	
	
}
