package net.agent.SchedulingAgent.Behaviour;

import java.text.SimpleDateFormat;
import java.util.Date;
import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * This behavior is triggered when the scheduling process is completed.
 * It logs the completion, saves the scheduling results, and resets the agent's state to be ready for new messages.
 */
public class SchedulingDone extends OneShotBehaviour {
	private static final long serialVersionUID = -7479890786191155653L;
	
	private SchedulingAgent schedulingAgent;

    public SchedulingDone(SchedulingAgent schedulingAgent) {
        this.schedulingAgent = schedulingAgent;
    }

    @Override
    public void action() {
        logCompletion();
        saveSchedulingResults();
        resetAgentState();
        
        System.err.println("-------------");
        
        if (Integer.valueOf(this.schedulingAgent.getLocalName()) == 1) {
			
        	this.schedulingAgent.getInternalDataModel().getSchedulingResults().printSchedulingResults();
		}
    }

    /**
     * Logs the completion of the scheduling process.
     */
    private void logCompletion() {
        System.out.println("Agent: " + schedulingAgent.getLocalName() + " SchedulingCompleted Activated");
    }

    /**
     * Saves the scheduling results to a CSV file in a designated output directory.
     */
    private void saveSchedulingResults() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String datePrefix = sdf.format(new Date());
        String fileName = "_Agent" + schedulingAgent.getLocalName() + "_SchedulingResults.csv";
        String filePath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\out\\" + datePrefix + fileName;
        
        schedulingAgent.getInternalDataModel().getSchedulingResults().saveSchedulingResultsToCSV(filePath);
    }

    /**
     * Resets the agent's state to be ready to receive new messages.
     */
    private void resetAgentState() {
        schedulingAgent.getInternalDataModel().setReceiveMessages(true);
    }
}
