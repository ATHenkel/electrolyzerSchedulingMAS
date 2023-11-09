package net.agent.SchedulingAgent.Behaviour;

import java.util.List;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import net.agent.SchedulingAgent.IterationADMM;
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
		
	    // currentIteration und productionQuantity in ein Textformat umwandeln
	    String currentIterationStr = Integer.toString(currentIteration);
	    String productionQuantityStr = Double.toString(productionQuantity);
   
		List<AID> temporaryPhoneBook = this.schedulingAgent.getInternalDataModel().getPhoneBook();
		AID myAID = this.schedulingAgent.getAID();

	    // Die beiden Werte in die Nachricht einfügen, z.B. durch Trennzeichen
	    String content = currentIterationStr + "," + productionQuantityStr;
	  
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
