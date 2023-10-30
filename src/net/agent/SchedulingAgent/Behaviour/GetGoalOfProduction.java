package net.agent.SchedulingAgent.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import net.agent.DSMInformation.DSMInformation;
import net.agent.SchedulingAgent.SchedulingAgent;

public class GetGoalOfProduction extends OneShotBehaviour {
	
	SchedulingAgent schedulingAgent;
	
	public GetGoalOfProduction(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	
	@Override
	public void action() {
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();

		DSMInformation dsmInformation = schedulingAgent.getInternalDataModel().getDSMInformation();
		 dsmInformation.addExternalDSMInformation(1, 0.047585 * 1.88, 0.05); 
		 dsmInformation.addExternalDSMInformation(2, 0.047585 * 2.8, 0.05); 
		 dsmInformation.addExternalDSMInformation(3, 0.047585 * 1.7, 0.05); 
		 dsmInformation.addExternalDSMInformation(4, 0.047585 * 2.43, 0.05);
		 double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);  

		//Als nächstes auszuführendes Behaviour 
		MinimizeX minimizeX = new MinimizeX(schedulingAgent);
		this.schedulingAgent.addBehaviour(minimizeX);
		
	}

}