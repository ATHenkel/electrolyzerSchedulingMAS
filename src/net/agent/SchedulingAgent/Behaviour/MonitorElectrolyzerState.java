package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import net.agent.DSMInformation.SchedulingResults;
import net.agent.SchedulingAgent.SchedulingAgent;

public class MonitorElectrolyzerState extends TickerBehaviour {
	private static final long serialVersionUID = -679811318507195166L;

	public MonitorElectrolyzerState(SchedulingAgent schedulingAgent) {
		super(schedulingAgent, 2000); // period in milliseconds
		this.schedulingAgent = schedulingAgent;
	}

	SchedulingAgent schedulingAgent;

	@Override
	protected void onTick() {

		// Increment Counter
		this.schedulingAgent.getInternalDataModel().incrementCounter();
		int counter = this.schedulingAgent.getInternalDataModel().getCounter();

		// Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}

		// Initialize the values for operating point and demand
		double setpoint = 0;
		double demand = 0;

		// Get OPC-UA Information from Internal Data Model
		AddressSpace peaAddressSpace = this.schedulingAgent.getInternalDataModel().getPeaAddressSpace();
		Boolean schedulingComplete = this.schedulingAgent.getInternalDataModel().isSchedulingComplete();
		LocalDateTime lastScheduleWriteTime = this.schedulingAgent.getInternalDataModel().getLastScheduleWriteTime();
		boolean reschedulingActivated = this.schedulingAgent.getInternalDataModel().isReschedulingActivated();

		// Get current Time
		LocalDateTime currentTime = LocalDateTime.now();

		// Formatted Time
		String formattedTime = String.format("%02d:%02d:%02d", currentTime.getHour(), currentTime.getMinute(),
				currentTime.getSecond());

		// Time difference between writing values to the PLC in seconds
		double writeTimeDifference = 180; // 3 Minutes
		int nextPeriod = this.schedulingAgent.getInternalDataModel().getSchedulingResultNextPeriod();

		// Get Scheduling Results
		SchedulingResults schedulingResults = this.schedulingAgent.getInternalDataModel().getSchedulingResults();
		int numberScheduledPeriods = schedulingResults.getNumberScheduledPeriods();
		Map<String, Object> resultNextPeriod = schedulingResults.getResult(nextPeriod);

		// Calculate the difference between the times
		Duration timeDifference = Duration.between(lastScheduleWriteTime, currentTime);

		// Write Data to Excel
		String agentID = this.schedulingAgent.getLocalName();

		try {
			// Define OPC UA Node-IDs
			NodeId H2ProductionRateVOpNodeId = new NodeId(2, "H2ProductionRate.VOp");
			NodeId H2ProductionRateVReqNodeId = new NodeId(2, "H2ProductionRate.VReq");
			NodeId H2ProductionRateVOutNodeId = new NodeId(2, "H2ProductionRate.VOut");
			NodeId H2ProductionRateApplyOpNodeId = new NodeId(2, "H2ProductionRate.ApplyOp");
			NodeId H2FlowrateVNodeId = new NodeId(2, "H2FlowRate.V");
			NodeId ElectrolysisStateCurNodeId = new NodeId(2, "Electrolysis.StateCur");

			// Define corresponding OPC UA Nodes
			UaVariableNode H2ProductionRateVOpNode = (UaVariableNode) peaAddressSpace.getNode(H2ProductionRateVOpNodeId);
			UaVariableNode H2ProductionRateVReqNode = (UaVariableNode) peaAddressSpace.getNode(H2ProductionRateVReqNodeId);
			UaVariableNode H2ProductionRateVOutNode = (UaVariableNode) peaAddressSpace.getNode(H2ProductionRateVOutNodeId);
			UaVariableNode H2ProductionRateApplyOpNode = (UaVariableNode) peaAddressSpace
					.getNode(H2ProductionRateApplyOpNodeId);
			UaVariableNode H2FlowrateNode = (UaVariableNode) peaAddressSpace.getNode(H2FlowrateVNodeId);

			// Read Value from OPC UA Nodes
			Float H2ProductionRateVOp = (Float) H2ProductionRateVOpNode.readValue().getValue().getValue();
			Float H2ProductionRateVReq = (Float) H2ProductionRateVReqNode.readValue().getValue().getValue();
			Float H2ProductionRateVOut = (Float) H2ProductionRateVOutNode.readValue().getValue().getValue();
			Boolean H2ProductionRateApplyOp = (Boolean) H2ProductionRateApplyOpNode.readValue().getValue().getValue();
			Float H2Flowrate = (Float) H2FlowrateNode.readValue().getValue().getValue();
			H2Flowrate = (float) (H2Flowrate * (0.002015 / 22.41)); // convert Value from Nl/h in kg/h
			
			System.out.println("H2ProductionRateVOp: " + H2ProductionRateVOp);

			// TODO: Test for Agent 1 und 3
			if (schedulingComplete && agentId == 1) {
				if (nextPeriod <= numberScheduledPeriods) {

					// Check if new value must be written to the PLC in case of TimeDifference
					if (timeDifference.getSeconds() >= writeTimeDifference) {

						// Update the time and the counter
						// this.schedulingAgent.getInternalDataModel().setLastScheduleWriteTime(LocalDateTime.now());
						this.schedulingAgent.getInternalDataModel().incrementSchedulingResultNextPeriod();

						System.out.println("Write new Value after " + timeDifference.getSeconds() + " s "
								+ " actual Period: " + nextPeriod + " No. Periods Scheduled " + numberScheduledPeriods);

						// Get new Setpoint
						double setpointNew = (double) resultNextPeriod.get("Setpoint");

						// Convert to data format according to OPC UA variables
						float setpointFloat = (float) setpointNew;

						// Write Values to OPC UA Node
						H2ProductionRateVOpNode.writeValue(new Variant(setpointFloat));
						H2ProductionRateApplyOpNode.writeValue(new Variant(true));
					}

					// Check if new value must be written to the PLC in case of Rescheduling
					if (reschedulingActivated && schedulingComplete) {

						// Get Scheduling Results
						int reschedulingPeriod = this.schedulingAgent.getInternalDataModel().getReschedulingPeriod();
						Map<String, Object> resultActualPeriod = schedulingResults.getResult(reschedulingPeriod);
						double setpointNew = (double) resultActualPeriod.get("Setpoint");

						// Convert to data format according to OPC UA variables
						float setpointFloat = (float) setpointNew;

						// Write Values to OPC UA Node
						H2ProductionRateVOpNode.writeValue(new Variant(setpointFloat));
						H2ProductionRateApplyOpNode.writeValue(new Variant(true));

						System.out.println("Write new Value after " + timeDifference.getSeconds() + " s "
								+ " actual Period: " + nextPeriod + " No. Periods Scheduled " + numberScheduledPeriods);

						// Set reschedulingActivated to false, if new PLC value was written to PLC
						this.schedulingAgent.getInternalDataModel().setReschedulingActivated(false);
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

			// TODO Output of values for PEA-agent 1 und 3
//			if (agentId == 1) {
//				String filepath = "D:\\\\Dokumente\\\\OneDrive - Helmut-Schmidt-Universität\\\\04_Programmierung\\\\ElectrolyseurScheduling JADE\\\\OPCUA_Agent1.csv";
//				System.out.println(
//						"Agent: " + this.schedulingAgent.getLocalName() + " Ausgabe der Nodes alle 2 Sekunden. "
//								+ "H2ProductionRate: " + H2ProductionRateVOp + " Counter: " + counter);
//				writeOpcUaDataToExcel(filepath, agentID, actualPeriod, demand, setpoint, formattedTime,
//						H2ProductionRateVOp, H2ProductionRateVOut, H2Flowrate);
//			}
//
//			// Hier Ausgabe für Agenten 3
//			if (agentId == 3) {
//				String filepath = "D:\\\\Dokumente\\\\OneDrive - Helmut-Schmidt-Universität\\\\04_Programmierung\\\\ElectrolyseurScheduling JADE\\\\OPCUA_Agent3.csv";
//				System.out.println(
//						"Agent: " + this.schedulingAgent.getLocalName() + " Ausgabe der Nodes alle 2 Sekunden. "
//								+ "H2ProductionRate: " + H2ProductionRateVOp + " Counter: " + counter);
//				writeOpcUaDataToExcel(filepath, agentID, actualPeriod, demand, setpoint, formattedTime,
//						H2ProductionRateVOp, H2ProductionRateVOut, H2Flowrate);
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeOpcUaDataToExcel(String filepath, String AgentID, int Period, double Demand, double setpoint,
			String formattedTime, Float H2ProductionRateVOp, Float H2ProductionRateVOut, Float H2Flowrate) {
		String header;
		String data;
		boolean headerWritten = this.schedulingAgent.getInternalDataModel().isHeaderWritten();

		// Create Header
		if (headerWritten == true) {
			header = "Agent;Periode;Demand;X;Time;H2ProductionRateVOp;H2ProductionRateVOut;H2Flowrate";
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
				{
					writer.write(header);
					writer.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.schedulingAgent.getInternalDataModel().setHeaderWritten(false);
		}

		// Create Data
		data = AgentID + ";" + Period + ";" + Demand + ";" + setpoint + ";" + formattedTime + ";" + H2ProductionRateVOp
				+ ";" + H2ProductionRateVOut + ";" + H2Flowrate;
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
			{
				writer.write(data);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
