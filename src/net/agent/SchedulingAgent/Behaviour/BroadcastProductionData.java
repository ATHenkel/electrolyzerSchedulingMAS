package net.agent.SchedulingAgent.Behaviour;

import java.util.List;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import net.agent.SchedulingAgent.SchedulingAgent;

public class BroadcastProductionData extends OneShotBehaviour {

	public BroadcastProductionData(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	SchedulingAgent schedulingAgent;

	@Override
	public void action() {
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		int currentIteration = this.schedulingAgent.getInternalDataModel().getIteration();
		double productionQuantity = this.schedulingAgent.getInternalDataModel()
				.getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration);	
		double tolerancePower = 0.02; //TODO Parameter noch anpassen
		double minPower = this.schedulingAgent.getInternalDataModel().getMinPower();
		double maxPower = this.schedulingAgent.getInternalDataModel().getMaxPower();
		double x = this.schedulingAgent.getInternalDataModel().getX();
		boolean lowerOperatingLimitReached = false;
		boolean upperOperatingLimitReached = false;
		int rowIndexShutdownOrder = this.schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
		
		//Check if lower Operating Limit is reached, set boolean to true
		 if (Math.abs(x - minPower) < tolerancePower)
		 {
			 lowerOperatingLimitReached = true;
		 }
		 
		 //TODO: Hier weiter dran arbeiten 
			//Check if upper Operating Limit is reached, set boolean to true
		 if (Math.abs(x - maxPower) <= 0.01 && currentIteration > 10)
		 {
			 upperOperatingLimitReached = true;
		 }
		
	    // Convert currentIteration and productionQuantity into a text format
	    String currentIterationStr = Integer.toString(currentIteration);
	    String productionQuantityStr = Double.toString(productionQuantity);
	    String lowerOperatingLimitReachedStr = Boolean.toString(lowerOperatingLimitReached);
	    String upperOperatingLimitReachedStr = Boolean.toString(upperOperatingLimitReached);
	    String rowIndexShutdownOrderStr = Integer.toString(rowIndexShutdownOrder);
   
		List<AID> temporaryPhoneBook = this.schedulingAgent.getInternalDataModel().getPhoneBook();
		AID myAID = this.schedulingAgent.getAID();

	    // Separate the two values in the message using separators
	    String content = currentIterationStr + "," + productionQuantityStr + "," + lowerOperatingLimitReachedStr + "," + upperOperatingLimitReachedStr + "," + rowIndexShutdownOrderStr;
	  
		for (int i = 0; i < temporaryPhoneBook.size(); i++) {
			AID receiverAID = temporaryPhoneBook.get(i);
			if (!receiverAID.equals(myAID)) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(receiverAID);
				msg.setContent(content);
				msg.setSender(myAID);
				this.schedulingAgent.send(msg);
			}
		}
		
		// Next Behaviour to be executed
		MessageReceiveBehaviour messageReceiveBehaviour = new MessageReceiveBehaviour(schedulingAgent);
		this.schedulingAgent.addBehaviour(messageReceiveBehaviour);
	}

}
