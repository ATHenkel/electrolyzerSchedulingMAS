package net.agent.BrokerAgent;

import jade.core.Agent;
import net.agent.BrokerAgent.Behaviour.instantiateAgents;

public class BrokerAgent extends Agent {

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
		instantiateAgents instantiateAgents = new instantiateAgents(this);
		this.addBehaviour(instantiateAgents);
	}

}

