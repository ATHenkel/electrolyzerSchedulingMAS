package net.agent.SchedulingAgent.Behaviour;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

public class MonitorElectrolyzerState extends TickerBehaviour {

    public MonitorElectrolyzerState(SchedulingAgent schedulingAgent) {
        super(schedulingAgent, 2000); // period in milliseconds
        this.schedulingAgent = schedulingAgent;
    }

    SchedulingAgent schedulingAgent;
    
    public void writeOpcUaDataToExcel(String filepath, String AgentID, int Period, double Demand, double setpoint, String formattedTime,
            Float H2ProductionRateVOp, Float H2ProductionRateVOut,Float H2Flowrate) {
        String header;
        String data;
        boolean headerWritten = this.schedulingAgent.getInternalDataModel().isHeaderWritten();

        //Create Header 
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
		data = AgentID + ";" + Period + ";" + Demand + ";" + setpoint + ";" + formattedTime + ";"
				+ H2ProductionRateVOp + ";" + H2ProductionRateVOut + ";" + H2Flowrate;
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
			{
				writer.write(data);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    @Override
    protected void onTick() {
    	
    	//-----------------
        //Test
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
		//-----------------
		
		
    	// Get information from the internal data model
        AddressSpace addressSpace = this.schedulingAgent.getInternalDataModel().getAddressSpace();
        Boolean schedulingComplete = this.schedulingAgent.getInternalDataModel().isSchedulingComplete();
        LocalDateTime lastScheduleWriteTime = this.schedulingAgent.getInternalDataModel().getLastScheduleWriteTime();
        LocalDateTime currentTime = LocalDateTime.now(); //Get current Time
        String formattedTime = String.format("%02d:%02d:%02d", currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond());// Formatted Time
        double writeTimeDifference = 15; //Time difference between writing values to the PLC in seconds 
        int nextPeriod = this.schedulingAgent.getInternalDataModel().getSchedulingResultNextPeriod();

        SchedulingResults schedulingResults = this.schedulingAgent.getInternalDataModel().getSchedulingResults(); //Get Scheduling Results
        int numberScheduledPeriods = schedulingResults.getNumberScheduledPeriods();
        Map<String, Object> resultNextPeriod = schedulingResults.getResult(nextPeriod);
        
        // Calculate the difference between the times
        Duration timeDifference = Duration.between(lastScheduleWriteTime, currentTime);
        
        //Write Data to Excel
        String agentID = this.schedulingAgent.getLocalName();
        String filepath = "D:\\\\Dokumente\\\\OneDrive - Helmut-Schmidt-Universität\\\\04_Programmierung\\\\ElectrolyseurScheduling JADE\\\\OPCUA_Agent1.csv";

        try {
            // Define OPC UA Node-IDs
            NodeId H2ProductionRateVOpNodeId = new NodeId(2, "H2ProductionRate.VOp");
            NodeId H2ProductionRateVReqNodeId = new NodeId(2, "H2ProductionRate.VReq");
            NodeId H2ProductionRateVOutNodeId = new NodeId(2, "H2ProductionRate.VOut");
            NodeId H2ProductionRateApplyOpNodeId = new NodeId(2, "H2ProductionRate.ApplyOp");
            NodeId H2FlowrateVNodeId = new NodeId(2, "H2FlowRate.V");
            NodeId ElectrolysisStateCurNodeId = new NodeId(2, "Electrolysis.StateCur");
            
            //Define corresponding OPC UA Nodes
            UaVariableNode H2ProductionRateVOpNode = (UaVariableNode) addressSpace.getNode(H2ProductionRateVOpNodeId);
            UaVariableNode H2ProductionRateVReqNode = (UaVariableNode) addressSpace.getNode(H2ProductionRateVReqNodeId);
            UaVariableNode H2ProductionRateVOutNode = (UaVariableNode) addressSpace.getNode(H2ProductionRateVOutNodeId);
            UaVariableNode H2ProductionRateApplyOpNode = (UaVariableNode) addressSpace.getNode(H2ProductionRateApplyOpNodeId);
            UaVariableNode H2FlowrateNode = (UaVariableNode) addressSpace.getNode(H2FlowrateVNodeId);

            //Read Value from OPC UA Nodes 
            Float H2ProductionRateVOp = (Float) H2ProductionRateVOpNode.readValue().getValue().getValue();
            Float H2ProductionRateVReq = (Float) H2ProductionRateVReqNode.readValue().getValue().getValue();
            Float H2ProductionRateVOut = (Float) H2ProductionRateVOutNode.readValue().getValue().getValue();
            Boolean H2ProductionRateApplyOp = (Boolean) H2ProductionRateApplyOpNode.readValue().getValue().getValue();
            Float H2Flowrate = (Float) H2FlowrateNode.readValue().getValue().getValue();
            H2Flowrate = (float) (H2Flowrate * 0.0708);
            
			if (schedulingComplete && agentId == 1) {
				if (nextPeriod <= numberScheduledPeriods) {
			        //Check if new value must be written to the PLC
			        if (timeDifference.getSeconds() >= writeTimeDifference) {
			        	
						//Update the time and the counter 
						this.schedulingAgent.getInternalDataModel().setLastScheduleWriteTime(LocalDateTime.now());
						this.schedulingAgent.getInternalDataModel().incrementSchedulingResultNextPeriod();
			        	
			        	System.out.println("Write new Value after " + timeDifference.getSeconds() + " s " + " actual Period: " + nextPeriod + " No. Periods Scheduled " + numberScheduledPeriods);
						
			        	//Get new Setpoint
			        	double setpointNew = (double) resultNextPeriod.get("Setpoint");
						
					    //Convert to data format according to OPC UA variables 
						float setpointFloat = (float) setpointNew;
						
						// Write Values to OPC UA Node
						H2ProductionRateVOpNode.writeValue(new Variant(setpointFloat));
						H2ProductionRateApplyOpNode.writeValue(new Variant(true));
					}
				}
				
			}
            
			//Get Values for Actual Period
			int actualPeriod = schedulingResults.getFirstPeriod();
			if (nextPeriod != schedulingResults.getFirstPeriod()) {
				actualPeriod = this.schedulingAgent.getInternalDataModel().getSchedulingResultNextPeriod() - 1;
			}

	        Map<String, Object> resultActualPeriod = schedulingResults.getResult(actualPeriod);
	        double setpoint = (double) resultActualPeriod.get("Setpoint");
	        double demand = (double) resultActualPeriod.get("Demand");
			
			if (agentId == 1) {
	            System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Ausgabe der Nodes alle 2 Sekunden:");
	            System.out.println("H2ProductionRate: " + H2ProductionRateVOp);
	           writeOpcUaDataToExcel(filepath, agentID, actualPeriod, demand, setpoint, formattedTime, H2ProductionRateVOp, H2ProductionRateVOut, H2Flowrate);
			}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
