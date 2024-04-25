package net.agent.SchedulingAgent.Behaviour;

import java.util.List;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * This class handles broadcasting production data to other agents within the system.
 * It extends OneShotBehaviour, meaning it will execute its task once per invocation.
 */
public class ProductionDataBroadcaster extends OneShotBehaviour {

    private SchedulingAgent schedulingAgent;

    /**
     * Constructor to initialize the ProductionDataBroadcaster with its scheduling agent.
     * @param schedulingAgent The agent associated with this behaviour.
     */
    public ProductionDataBroadcaster(SchedulingAgent schedulingAgent) {
        this.schedulingAgent = schedulingAgent;
    }

    @Override
    public void action() {
        // Retrieve current period and iteration from the internal data model.
        int currentPeriod = schedulingAgent.getInternalDataModel().getCurrentPeriod();
        int currentIteration = schedulingAgent.getInternalDataModel().getIteration();
        double productionQuantity = schedulingAgent.getInternalDataModel().getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration);  
        double tolerancePower = 0.02; // Tolerance for checking operating limits.
        double minPower = schedulingAgent.getInternalDataModel().getMinPower();
        double maxPower = schedulingAgent.getInternalDataModel().getMaxPower();
        double x = schedulingAgent.getInternalDataModel().getX();
        boolean lowerOperatingLimitReached = false;
        boolean upperOperatingLimitReached = false;
        int rowIndexShutdownOrder = schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
        
        // Check if the lower operating limit is reached.
        if (Math.abs(x - minPower) < tolerancePower || !schedulingAgent.getInternalDataModel().isStateProduction()) {
            lowerOperatingLimitReached = true;
        }
         
        // Check if the upper operating limit is reached.
        if (Math.abs(x - maxPower) <= 0.01 && currentIteration > 10 || !schedulingAgent.getInternalDataModel().isStateProduction()) {
            upperOperatingLimitReached = true;
        }
     
        // Formatting data for transmission
        String content = formatMessageContent(currentIteration, productionQuantity, lowerOperatingLimitReached, upperOperatingLimitReached, rowIndexShutdownOrder);

        // Send the formatted message to other agents excluding self
        broadcastMessage(content);

        // Schedule the next behaviour
        schedulingAgent.addBehaviour(new MessageReceiveBehaviour(schedulingAgent));
    }

    /**
     * Formats the message content for broadcasting.
     */
    private String formatMessageContent(int iteration, double quantity, boolean lowerLimitReached, boolean upperLimitReached, int rowIndex) {
        return iteration + "," + quantity + "," + lowerLimitReached + "," + upperLimitReached + "," + rowIndex + ",false,-1";
    }

    /**
     * Sends a formatted message to all other agents in the phone book except itself.
     * @param content The content to be sent in the message.
     */
    private void broadcastMessage(String content) {
        List<AID> agents = schedulingAgent.getInternalDataModel().getPhoneBook();
        AID myAID = schedulingAgent.getAID();
        for (AID aid : agents) {
            if (!aid.equals(myAID)) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(aid);
                msg.setContent(content);
                msg.setSender(myAID);
                schedulingAgent.send(msg);
            }
        }
    }
}
