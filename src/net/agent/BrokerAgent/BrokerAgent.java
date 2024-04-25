package net.agent.BrokerAgent;

import jade.core.Agent;
import net.agent.BrokerAgent.Behaviour.instantiateAgents;

/**
 * BrokerAgent acts as a central node in the agent network, instantiates PEA-Agent, managing shared resources and coordinating agent activities.
 */
public class BrokerAgent extends Agent {

    private static final long serialVersionUID = 1L;
    private InternalDataModel internalDataModel;

    public BrokerAgent() {
        internalDataModel = new InternalDataModel();
    }

    /**
     * Gets the internal data model associated with this agent.
     * @return the internal data model instance
     */
    public InternalDataModel getInternalDataModel() {
        return internalDataModel;
    }

    /**
     * Sets the internal data model for this agent.
     * @param internalDataModel the internal data model to set
     */
    public void setInternalDataModel(InternalDataModel internalDataModel) {
        this.internalDataModel = internalDataModel;
    }

    /**
     * Initializes the agent setup and adds necessary behaviors.
     */
    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": initializing broker agent behaviors.");
        addBehaviour(new instantiateAgents(this));
    }

}
