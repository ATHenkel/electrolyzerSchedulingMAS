package net.agent.SchedulingAgent;

import java.util.List;

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
    public SchedulingAgent(String endpointUrl, String mtpFileName, int numberofAgents, List<AID> phoneBook) {
        internalDataModel = new InternalDataModel();
        internalDataModel.setEndpointURL(endpointUrl);
        internalDataModel.setMtpFileName(mtpFileName);
        internalDataModel.setNumberofAgents(numberofAgents);
        internalDataModel.setPhoneBook(phoneBook);
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
        // Add cyclic behavior for receiving messages
        addBehaviour(new MessageReceiveBehaviour(this));
        
        // Initialize the knowledge base from a standard integration profile
        addBehaviour(new KnowledgeBaseInitializer(this));
        
        // Establish OPC-UA connection
        if (extractAgentNumber(this.getLocalName()) == 1) {
        	addBehaviour(new OPCUAConnection(this));
		}
       //addBehaviour(new OPCUAConnection(this));
        
        // Obtain production goals from the DSM system
//        addBehaviour(new GetGoalOfProduction(this));
        
        // Initialize phonebook with other agents
        initializePhoneBook();
    }
    
    /**
     * Initializes the phone book for communication between agents if not already initialized.
     */
    private void initializePhoneBook() {
        // Check if the phone book is already initialized
        if (internalDataModel.getPhoneBook() == null || internalDataModel.getPhoneBook().isEmpty()) {
            // Initialize the phone book only if it's not already initialized
            for (int i = 1; i <= internalDataModel.getNumberofAgents(); i++) {
                AID agentAID = new AID(String.valueOf(i), AID.ISLOCALNAME);
                // Check if the current agent is not the same as this agent
                if (!agentAID.equals(getAID())) {
                    internalDataModel.addAID2PhoneBook(agentAID);
                }
            }
        }
    }
    
	/**
	 * Extracts the agent number from the agent name.
	 * Assumes that the agent name follows the format "<instanceName>:PEA_Agent<number>".
	 * @param agentName the name of the agent
	 * @return the agent number
	 */
	public static int extractAgentNumber(String agentName) {
	    // Split the agent name by ":PEA_Agent" to get the part containing the agent number
	    String[] parts = agentName.split("--PEAAgent");

	    // Check if the agent name follows the expected format
	    if (parts.length == 2) {
	        // Extract the agent number from the second part and convert it to an integer
	        try {
	            return Integer.parseInt(parts[1]);
	        } catch (NumberFormatException e) {
	            // If the agent number is not a valid integer, return -1 to indicate an error
	            return -1;
	        }
	    } else {
	        // If the agent name does not follow the expected format, return -1 to indicate an error
	        return -1;
	    }
	}
}
