package net.agent.SchedulingAgent;

import jade.core.AID;
import jade.core.Agent;
import net.agent.SchedulingAgent.Behaviour.GetGoalOfProduction;
import net.agent.SchedulingAgent.Behaviour.MessageReceiveBehaviour;
import net.agent.SchedulingAgent.Behaviour.OPCUAConnection;
import net.agent.SchedulingAgent.Behaviour.initializeKnowledgebase;

public class SchedulingAgent extends Agent {
	
    // Constructor with additional parameter for the endpoint URL and name of the MTP
    public SchedulingAgent(String endpointUrl, String MTPname, int numberofAgents) {
        this.getInternalDataModel().setEndpointURL(endpointUrl);
        this.getInternalDataModel().setNumberofAgents(numberofAgents);
    }

	private static final long serialVersionUID = 1L;
	InternalDataModel internalDataModel = new InternalDataModel();

	public InternalDataModel getInternalDataModel() {
		return internalDataModel;
	}

	public void setInternalDataModel(InternalDataModel internalDataModel) {
		this.internalDataModel = internalDataModel;
	}

	protected void setup() {
		
		// Will be called during instantiation
		
		//Message Receive behavior, runs cyclically
		MessageReceiveBehaviour messageReceiveBehaviour = new MessageReceiveBehaviour(this);
		this.addBehaviour(messageReceiveBehaviour);
		
		//Initialize knowledge base from standard integration profile
		initializeKnowledgebase initializeKnowledgebase = new initializeKnowledgebase(this);
		this.addBehaviour(initializeKnowledgebase);
		

		//Connect to Low-Level Controller via OPC-UA
//		OPCUAConnection opcuaConnection = new OPCUAConnection(this);
//		this.addBehaviour(opcuaConnection);

		//Get Production Goals from DSM 
		GetGoalOfProduction getGoalOfProduction = new GetGoalOfProduction(this);
		this.addBehaviour(getGoalOfProduction);

		// Add Agents to PhoneBook
		for (int i = 0; i < this.internalDataModel.getNumberofAgents(); i++) {
			AID agentAID = new AID(String.valueOf(i + 1), AID.ISLOCALNAME);
			if (agentAID != this.getAID()) {
				this.getInternalDataModel().addAID2PhoneBook(agentAID);
			}
		}
	}

}
