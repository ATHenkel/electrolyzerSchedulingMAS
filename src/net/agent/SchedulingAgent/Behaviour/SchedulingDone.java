package net.agent.SchedulingAgent.Behaviour;

import java.util.List;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.InternalDataModel;
import net.agent.SchedulingAgent.IterationADMM;
import net.agent.SchedulingAgent.SchedulingAgent;

public class SchedulingDone extends OneShotBehaviour {

	SchedulingAgent schedulingAgent;

	public SchedulingDone(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	@Override
	public void action() {
		System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " SchedulingDone Activated");

		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}

		List<IterationADMM> iterationADMMTable = this.schedulingAgent.getInternalDataModel().getIterationADMMTable();

		if (agentId == 1) {
			System.out.println("Iteration-ADMM-Table für Agenten 1");
			// print all values from Iteration ADMM table
			for (IterationADMM iteration : iterationADMMTable) {
				int period = iteration.getPeriod();
				int iterationNumber = iteration.getIteration();
				double productionQuantity = iteration.getProductionQuantity();
				double energyDemand = iteration.getEnergyDemand();
				double mLCOH = iteration.getmLCOH();
				double x = iteration.getX();

				// Format in table
				String formattedOutput = String.format(
						"Period: %d | Iteration: %d | Production Quantity: %.2f | Energy Demand: %.2f | mLCOH: %.2f | x: %.2f",
						period, iterationNumber, productionQuantity, energyDemand, mLCOH, x);

				System.out.println(formattedOutput);
			}
			
			myAgent.removeBehaviour(this); //Agent soll nicht sterben

		}

	}
}
