package net.agent.SchedulingAgent.Behaviour;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import jade.core.behaviours.TickerBehaviour;
import net.agent.DSMInformation.SchedulingResults;
import net.agent.SchedulingAgent.SchedulingAgent;

public class SetpointUpdater extends TickerBehaviour {
	private static final long serialVersionUID = -3300876452593347028L;

	public SetpointUpdater(SchedulingAgent schedulingAgent) {
		super(schedulingAgent, 2000); // period in milliseconds
		this.schedulingAgent = schedulingAgent;
	}

	SchedulingAgent schedulingAgent;

	@Override
	protected void onTick() {
		// Increment Counter
		this.schedulingAgent.getInternalDataModel().incrementCounter();
		int counter = this.schedulingAgent.getInternalDataModel().getCounter();

		// Initialize the values for operating point and demand
		double setpoint = 0;
		double demand = 0;

		// Get OPC-UA Information from Internal Data Model
		AddressSpace addressSpace = this.schedulingAgent.getInternalDataModel().getAddressSpace();
		Boolean schedulingComplete = this.schedulingAgent.getInternalDataModel().isSchedulingComplete();
		LocalDateTime lastScheduleWriteTime = this.schedulingAgent.getInternalDataModel().getLastScheduleWriteTime();

		// Get current Time
		LocalDateTime currentTime = LocalDateTime.now();

		// Time difference between writing values to the PLC in seconds
		double writeTimeDifference = 10; // time in seconds
		int nextPeriod = this.schedulingAgent.getInternalDataModel().getSchedulingResultNextPeriod();

		// Get Scheduling Results
		SchedulingResults schedulingResults = this.schedulingAgent.getInternalDataModel().getSchedulingResults();
		int numberScheduledPeriods = schedulingResults.getNumberScheduledPeriods();
		Map<String, Object> resultNextPeriod = schedulingResults.getResult(nextPeriod);

		// Calculate the difference between the times
		Duration timeDifference = Duration.between(lastScheduleWriteTime, currentTime);

		try {
			// Define OPC UA Node-IDs
			NodeId SetpointNodeId = new NodeId(2, "AnaView_AEL_StackUnit1--PEAAgent1_Setpoint.V");

			// Define corresponding OPC UA Nodes
			UaVariableNode SetpointNode = (UaVariableNode) addressSpace.getNode(SetpointNodeId);
			
			// TODO: Test for Agent 1
			if (schedulingComplete && extractAgentNumber(this.schedulingAgent.getLocalName()) == 1) {
				if (nextPeriod <= numberScheduledPeriods) {

					// Check if new value must be written to the PLC in case of TimeDifference
					if (timeDifference.getSeconds() >= writeTimeDifference) {

						// Update the time and the counter
						this.schedulingAgent.getInternalDataModel().incrementSchedulingResultNextPeriod();

						System.out.println("Write new Value after " + timeDifference.getSeconds() + " s "
								+ " actual Period: " + nextPeriod + " No. Periods Scheduled " + numberScheduledPeriods);

						// Get new Setpoint
						double setpointNew = (double) resultNextPeriod.get("Setpoint");

						// Convert to data format according to OPC UA variables
						float setpointFloat = (float) setpointNew;

						// Write Values to OPC UA Node
						SetpointNode.writeValue(new Variant(setpointFloat));
					}

				}
			}

			// Get Values for Actual Period
			int actualPeriod = schedulingResults.getFirstPeriod();
			if (nextPeriod != schedulingResults.getFirstPeriod()) {
				actualPeriod = this.schedulingAgent.getInternalDataModel().getSchedulingResultNextPeriod() - 1;
			}

			if (schedulingComplete) {
				Map<String, Object> resultActualPeriod = schedulingResults.getResult(actualPeriod);
				setpoint = (double) resultActualPeriod.get("Setpoint");
				demand = (double) resultActualPeriod.get("Demand");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Extracts the agent number from the agent name.
	 * Assumes that the agent name follows the format "<instanceName>:PEA_Agent<number>".
	 * @param agentName the name of the agent
	 * @return the agent number
	 */
	public static int extractAgentNumber(String agentName) {
	    // Split the agent name by ":PEA_Agent" to get the part containing the agent number
	    String[] parts = agentName.split("--PEAAgent");

	    // Check if the agent name follows the expected format
	    if (parts.length == 2) {
	        // Extract the agent number from the second part and convert it to an integer
	        try {
	            return Integer.parseInt(parts[1]);
	        } catch (NumberFormatException e) {
	            // If the agent number is not a valid integer, return -1 to indicate an error
	            return -1;
	        }
	    } else {
	        // If the agent name does not follow the expected format, return -1 to indicate an error
	        return -1;
	    }
	}

}
