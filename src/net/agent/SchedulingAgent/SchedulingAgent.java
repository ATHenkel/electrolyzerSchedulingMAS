package net.agent.SchedulingAgent;

import jade.core.AID;
import jade.core.Agent;
import net.agent.SchedulingAgent.Behaviour.GetGoalOfProduction;
import net.agent.SchedulingAgent.Behaviour.MessageReceiveBehaviour;
import net.agent.SchedulingAgent.Behaviour.MonitorElectrolyzerState;
import net.agent.SchedulingAgent.Behaviour.OPCUAConnection;
import net.agent.SchedulingAgent.Behaviour.KnowledgeBaseInitializer;

/**
 * Represents a scheduling agent responsible for planning and coordinating
 * scheduling activities within a multi-agent system.
 */
public class SchedulingAgent extends Agent {

    private static final long serialVersionUID = 1L;
    private InternalDataModel internalDataModel;

    /**
     * Constructs a SchedulingAgent with specific configuration.
     * @param endpointUrl URL for the OPC-UA connection.
     * @param mtpFileName Name of the MTP file for initializing the knowledge base.
     * @param numberofAgents Total number of agents involved in the process.
     */
    public SchedulingAgent(String endpointUrl, String mtpFileName, int numberofAgents) {
        internalDataModel = new InternalDataModel();
        internalDataModel.setEndpointURL(endpointUrl);
        internalDataModel.setMtpFileName(mtpFileName);
        internalDataModel.setNumberofAgents(numberofAgents);
    }

    /**
     * Gets the internal data model associated with this agent.
     * @return the internal data model
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
     * Initializes the agent, setting up required behaviors and configurations.
     */
    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": Starting setup.");
        
        // Add cyclic behavior for receiving messages
        addBehaviour(new MessageReceiveBehaviour(this));
        
        // Initialize the knowledge base from a standard integration profile
        addBehaviour(new KnowledgeBaseInitializer(this));
        
        // Establish OPC-UA connection
//        addBehaviour(new OPCUAConnection(this));
        
        // Obtain production goals from the DSM system
        addBehaviour(new GetGoalOfProduction(this));
        
        // Initialize phonebook with other agents
        initializePhoneBook();
    }

    /**
     * Initializes the phone book for communication between agents.
     */
    private void initializePhoneBook() {
        for (int i = 1; i <= internalDataModel.getNumberofAgents(); i++) {
            AID agentAID = new AID(String.valueOf(i), AID.ISLOCALNAME);
            if (!agentAID.equals(getAID())) {
                internalDataModel.addAID2PhoneBook(agentAID);
            }
        }
    }
}
