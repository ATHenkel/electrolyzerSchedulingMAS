package net.agent.SchedulingAgent.Behaviour;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.util.annotations.UInt32Primitive;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;

import jade.core.behaviours.TickerBehaviour;
import net.agent.DSMInformation.SchedulingResults;
import net.agent.SchedulingAgent.SchedulingAgent;

public class MonitorElectrolyzerState extends TickerBehaviour {

    public MonitorElectrolyzerState(SchedulingAgent schedulingAgent) {
        super(schedulingAgent, 2000); // period in milliseconds
        this.schedulingAgent = schedulingAgent;
    }

    SchedulingAgent schedulingAgent;

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
        double writeTimeDifference = 15; //Time difference between writing values to the PLC in seconds 
        SchedulingResults schedulingResults = this.schedulingAgent.getInternalDataModel().getSchedulingResults(); //Get Scheduling Results
        int numberScheduledPeriods = schedulingResults.getNumberScheduledPeriods();
        int writeScheduleCount = this.schedulingAgent.getInternalDataModel().getWriteScheduleCount();
    	
        // Calculate the difference between the times
        Duration timeDifference = Duration.between(lastScheduleWriteTime, currentTime);

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
            
			if (schedulingComplete && agentId == 1) {
				if (writeScheduleCount <= numberScheduledPeriods) {
			        //Check if new value must be written to the PLC
			        if (timeDifference.getSeconds() >= writeTimeDifference) {
			        	
			        	System.out.println("Write new Value after " + timeDifference.getSeconds() + " s " + " acutal Period: " + writeScheduleCount + " No. Periods Scheduled " + numberScheduledPeriods);
			        	
						Map<String, Object> result = schedulingResults.getResult(writeScheduleCount);
						double setpoint = (double) result.get("Setpoint");
						float setpointFloat = (float) setpoint;
						
						// Write Values to OPC UA Node
						H2ProductionRateVOpNode.writeValue(new Variant(setpointFloat));
						H2ProductionRateApplyOpNode.writeValue(new Variant(true));
						
						//Update the time and the counter 
						this.schedulingAgent.getInternalDataModel().setLastScheduleWriteTime(LocalDateTime.now());
						this.schedulingAgent.getInternalDataModel().incrementWriteScheduleCount();
					}
				}
				
			}
            
			if (agentId == 1) {
	            System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Ausgabe der Nodes alle 2 Sekunden:");
	            System.out.println("H2ProductionRate: " + H2ProductionRateVOp);
			}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
