package net.agent.SchedulingAgent.Behaviour;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * Handles incoming messages for a scheduling agent, updating operational states
 * and initiating rescheduling if necessary.
 */
public class MessageReceiveBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = -834250227095902254L;
	
	double sumProduction;

	public MessageReceiveBehaviour(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	SchedulingAgent schedulingAgent;

	@Override
	public void action() {

		// Get Values from Internal Data Model
		int numberOfAgents = this.schedulingAgent.getInternalDataModel().getNumberofAgents();
		int period = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		int iteration = this.schedulingAgent.getInternalDataModel().getIteration();
		int rowIndexShutdownOrder = this.schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();

		if (this.schedulingAgent.getInternalDataModel().isReceiveMessages()) {
			ACLMessage receivedMsg = this.schedulingAgent.receive();
			
			 /**
		     * Processes received ACLMessage, extracting content and performing actions based on the message data.
		     * @param receivedMsg The received ACLMessage.
		     */
			if (receivedMsg != null) {
				String content = receivedMsg.getContent();
				String[] parts = content.split(",");
				int msgIteration = Integer.parseInt(parts[0]);
				double msgProductionQuantity = Double.parseDouble(parts[1]);
				boolean msgLowerOperatingLimit = Boolean.parseBoolean(parts[2]);
				boolean msgUpperOperatingLimit = Boolean.parseBoolean(parts[3]);
				int msgRowIndexShutdownOrder = Integer.parseInt(parts[4]);
				boolean electrolyzerError = Boolean.parseBoolean(parts[5]);
				int periodError = Integer.parseInt(parts[6]);

				// Initiate rescheduling if PEA agent reports malfunction
				if (electrolyzerError) {
					System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " error message received, activate Re-Scheduling!");

					// Save the period in which a malfunction became known
					this.schedulingAgent.getInternalDataModel().setReschedulingPeriod(periodError);
					this.schedulingAgent.getInternalDataModel().setReschedulingActivated(true); // Set Re-Scheduling Activated to true
					this.schedulingAgent.getInternalDataModel().setSchedulingComplete(false); // Set SchedulingComplete to false
					this.schedulingAgent.getInternalDataModel().setCurrentPeriod(periodError); // Rescheduling for the period in which the failure was detected
					this.schedulingAgent.getInternalDataModel().clearIterationADMMTable(); // Clear ADMM-Results

					// Next Behaviour to be executed
					MinimizeX minimizeX = new MinimizeX(schedulingAgent);
					this.schedulingAgent.addBehaviour(minimizeX);
				}

				// Check whether the row index of the agent differs from the received row index
				if (msgRowIndexShutdownOrder != rowIndexShutdownOrder) {
					this.schedulingAgent.getInternalDataModel().setRowIndexShutdownOrder(msgRowIndexShutdownOrder);
				}

				// Check if no message has been received
				if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() == 0) {

					this.schedulingAgent.getInternalDataModel().setSumProduction(msgProductionQuantity); // Add Production Quantity to internal knowledge Base
					this.schedulingAgent.getInternalDataModel().addLowerOperatingLimit(iteration,
							msgLowerOperatingLimit); // Add Lower Operating Limit to List
					this.schedulingAgent.getInternalDataModel().addUpperOperatingLimit(iteration,
							msgUpperOperatingLimit); // Add Upper Operating Limit to List

				} else if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() > 0) {
					sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction()
							+ msgProductionQuantity; // Sum up production quantities
					this.schedulingAgent.getInternalDataModel().setSumProduction(sumProduction);
					this.schedulingAgent.getInternalDataModel().addLowerOperatingLimit(iteration,
							msgLowerOperatingLimit);// Add Lower Operating Limit to List
					this.schedulingAgent.getInternalDataModel().addUpperOperatingLimit(iteration,
							msgUpperOperatingLimit);// Add Upper Operating Limit to List
				}

				// Increase Message Counter
				this.schedulingAgent.getInternalDataModel().increaseCountReceivedMessages();

				// Check if all messages from this iteration have been received
				if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() == numberOfAgents - 1
						&& this.schedulingAgent.getInternalDataModel().getIteration() == msgIteration) {

					// Set and Reset Values
					this.schedulingAgent.getInternalDataModel().setReceiveMessages(false);
					this.schedulingAgent.getInternalDataModel().setCountReceivedMessages(0);
					//TODO: Wofür wird das setSumProduction hier benötigt? 
					//this.schedulingAgent.getInternalDataModel().setSumProduction(sumProduction);
					//System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Period:" + period+ " Iteration" + iteration + " sumProduction" + sumProduction);
					this.schedulingAgent.getInternalDataModel().addReceivedProductionQuantity(period, iteration,
							sumProduction);

					GatherProductionData gatherProductionData = new GatherProductionData(schedulingAgent);
					this.schedulingAgent.addBehaviour(gatherProductionData);
				}

			} else {
				block();
			}
		}
	}
}