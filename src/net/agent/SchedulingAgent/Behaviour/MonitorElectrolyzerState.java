package net.agent.SchedulingAgent.Behaviour;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import jade.core.behaviours.TickerBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class MonitorElectrolyzerState extends TickerBehaviour {

    public MonitorElectrolyzerState(SchedulingAgent schedulingAgent) {
        super(schedulingAgent, 3000); // period in milliseconds
        this.schedulingAgent = schedulingAgent;
    }

    SchedulingAgent schedulingAgent;

    @Override
    protected void onTick() {

        try {
        	// Get the OPC UA information from the internal data model
            AddressSpace addressSpace = this.schedulingAgent.getInternalDataModel().getAddressSpace();
            
            System.out.println("Agent:" + this.schedulingAgent.getLocalName() + " Ausgabe der Nodes alle 3 Sekunden:");
            NodeId H2ProductionRateVOpNodeId = new NodeId(2, "H2ProductionRate.VOp");
//            NodeId H2ProductionRateVReqNodeId = new NodeId(2, "H2ProductionRate.VReq");
//            NodeId H2ProductionRateVOutNodeId = new NodeId(2, "H2ProductionRate.VOut");
//            NodeId H2ProductionRateApplyExtNodeId = new NodeId(2, "H2ProductionRate.ApplyExt");
//            NodeId ElectrolysisStateCurNodeId = new NodeId(2, "Electrolysis.StateCur");
            
            Float H2ProductionRateVOp = (Float) ((UaVariableNode) addressSpace.getNode(H2ProductionRateVOpNodeId)).readValue().getValue().getValue();

            System.out.println("Der Wert von der H2ProductionRate lautet: " + H2ProductionRateVOp);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
