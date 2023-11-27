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
		if (this.schedulingAgent.getInternalDataModel().isReceiveMessages()) {
			
		ACLMessage receivedMsg = this.schedulingAgent.receive();

			if (receivedMsg != null) {
				String content = receivedMsg.getContent();
				String[] parts = content.split(",");
				int msgIteration = Integer.parseInt(parts[0]);
				double msgProductionQuantity = Double.parseDouble(parts[1]);

				// Check if the message belongs to the current iteration
				if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() == 0) {
					
					// Add Production Quantity to temporary Variable
					this.schedulingAgent.getInternalDataModel().setSumProduction_temp(msgProductionQuantity);
					this.schedulingAgent.getInternalDataModel().setSumProduction(msgProductionQuantity);
					
				} else if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() > 0) {
					sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction() + msgProductionQuantity;
					this.schedulingAgent.getInternalDataModel().setSumProduction(sumProduction);
					
				}

				// Increase Message Counter
				this.schedulingAgent.getInternalDataModel().increaseCountReceivedMessages();

				//TODO 10  Agenten 
				if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() == 9
				//if (this.schedulingAgent.getInternalDataModel().getCountReceivedMessages() == 2
						&& this.schedulingAgent.getInternalDataModel().getIteration() == msgIteration) {
					
					// Set and Reset Values
					this.schedulingAgent.getInternalDataModel().setReceiveMessages(false);
					this.schedulingAgent.getInternalDataModel().setCountReceivedMessages(0);
					this.schedulingAgent.getInternalDataModel().setSumProduction_temp(0);
					this.schedulingAgent.getInternalDataModel().setSumProduction(sumProduction);

					GatherProductionData gatherProductionData = new GatherProductionData(schedulingAgent);
					this.schedulingAgent.addBehaviour(gatherProductionData);
				}

			} else {
				block();
			}
		}
	}
}