package net.agent.BrokerAgent;

import jade.core.Agent;
import net.agent.BrokerAgent.Behaviour.instantiateSchedulingAgents;

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
		instantiateSchedulingAgents instantiateSchedulingAgents = new instantiateSchedulingAgents(this);
		this.addBehaviour(instantiateSchedulingAgents);
	}

}

