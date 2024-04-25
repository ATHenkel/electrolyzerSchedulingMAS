package net.agent.SchedulingAgent.Behaviour;

import java.text.SimpleDateFormat;
import java.util.Date;
import jade.core.behaviours.OneShotBehaviour;
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

	    // Format for the current date and time as prefix
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
	    String datePrefix = sdf.format(new Date());
		
		//Save Scheduling Results as .csv-Data
		String filepath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\out\\" + datePrefix + "_Agent" + localName + "_SchedulingResults.csv";
		this.schedulingAgent.getInternalDataModel().getSchedulingResults().saveSchedulingResultsToCSV(filepath);
		
		this.schedulingAgent.getInternalDataModel().setReceiveMessages(true);
	}
}
