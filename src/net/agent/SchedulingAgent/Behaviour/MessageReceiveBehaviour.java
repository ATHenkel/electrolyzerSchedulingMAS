package net.agent.SchedulingAgent.Behaviour;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import net.agent.SchedulingAgent.SchedulingAgent;

public class MessageReceiveBehaviour extends CyclicBehaviour {
	double sumProduction;

	public MessageReceiveBehaviour(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	SchedulingAgent schedulingAgent;

	@Override
	public void action() {
    	//Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
		
		//Get Values from Internal Data Model 
		int numberOfAgents = this.schedulingAgent.getInternalDataModel().getNumberofAgents();
		int period = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		int iteration = this.schedulingAgent.getInternalDataModel().getIteration();
		int rowIndexShutdownOrder = this.schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
		
		if (this.schedulingAgent.getInternalDataModel().isReceiveMessages()) {
			
		ACLMessage receivedMsg = this.schedulingAgent.receive();

			if (receivedMsg != null) {
				String content = receivedMsg.getContent();
				String[] parts = content.split(",");
				int msgIteration = Integer.parseInt(parts[0]);
				double msgProductionQuantity = Double.parseDouble(parts[1]);
				boolean msgLowerOperatingLimit = Boolean.parseBoolean(parts[2]);
				int msgRowIndexShutdownOrder = Integer.parseInt(parts[3]);
				
				//TODO TEST:
//				if (agentId == 1) {
//					System.out.println("Iteration: " + iteration + " Sender: " + receivedMsg.getSender() + " lowerLimit: " + msgLowerOperatingLimit);
//				}

				//Check whether the row index of the agent differs from the received row index 
				if (msgRowIndexShutdownOrder != rowIndexShutdownOrder) {
					this.schedulingAgent.getInternalDataModel().setRowIndexShutdownOrder(msgRowIndexShutdownOrder);
				}
				
				// Check if no message has been received
				if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() == 0) {
					
					// Add Production Quantity to internal knowledge Base
					this.schedulingAgent.getInternalDataModel().setSumProduction(msgProductionQuantity);
					
					//Add Lower Operating Limit to List
					this.schedulingAgent.getInternalDataModel().addLowerOperatingLimit(iteration, msgLowerOperatingLimit);
					
				} else if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() > 0) {
					//Sum up production quantities
					sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction() + msgProductionQuantity;
					this.schedulingAgent.getInternalDataModel().setSumProduction(sumProduction);
					
					//Add Lower Operating Limit to List
					this.schedulingAgent.getInternalDataModel().addLowerOperatingLimit(iteration, msgLowerOperatingLimit);
				}

				// Increase Message Counter
				this.schedulingAgent.getInternalDataModel().increaseCountReceivedMessages();

				if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() == numberOfAgents-1
						&& this.schedulingAgent.getInternalDataModel().getIteration() == msgIteration) {
					
					// Set and Reset Values
					this.schedulingAgent.getInternalDataModel().setReceiveMessages(false);
					this.schedulingAgent.getInternalDataModel().setCountReceivedMessages(0);
					this.schedulingAgent.getInternalDataModel().setSumProduction(sumProduction);
					this.schedulingAgent.getInternalDataModel().addReceivedProductionQuantity(period, iteration, sumProduction);

					GatherProductionData gatherProductionData = new GatherProductionData(schedulingAgent);
					this.schedulingAgent.addBehaviour(gatherProductionData);
				}

			} else {
				block();
			}
		}
	}
}