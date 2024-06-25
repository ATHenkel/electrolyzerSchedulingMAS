package net.agent.BrokerAgent.Behaviour;

import java.util.List;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import net.agent.BrokerAgent.BrokerAgent;

public class MonitorOptimizationService extends TickerBehaviour {
	private static final long serialVersionUID = -679811318507195166L;

	public MonitorOptimizationService(BrokerAgent brokerAgent) {
		super(brokerAgent, 2000); // period in milliseconds
		this.brokerAgent = brokerAgent;
	}

	BrokerAgent brokerAgent;

	@Override
	protected void onTick() {
	    /**
	     * Constants
	     */
		final int STATECUR_EXECUTE = 64;
		
	    /**
	     * Data from InternalDataModel
	     */
		AddressSpace AddressSpace = this.brokerAgent.getInternalDataModel().getAddressSpace();
		boolean startOptimizationActivated = this.brokerAgent.getInternalDataModel().isStartOptimizationActivated();

		try {
			// Define OPC UA Node-IDs
			NodeId ElectrolysisStateCurNodeId = new NodeId(2, "ServiceControl_Optimization.StateCur");

			// Define corresponding OPC UA Nodes
			UaVariableNode optimizationStateCurNode = (UaVariableNode) AddressSpace.getNode(ElectrolysisStateCurNodeId);

			// Read Value from OPC UA Nodes
			Integer optimizationStateCur = (Integer) optimizationStateCurNode.readValue().getValue().getValue();
			
			if (optimizationStateCur == STATECUR_EXECUTE && startOptimizationActivated == false) {
				System.out.println("Optimization Service - Execute detected. Start Optimization");
				List<AID> phoneBook = this.brokerAgent.getInternalDataModel().getPhoneBook();
				String uniformAddress = "http://127.0.0.1:7778/acc";

	            AID myAID = brokerAgent.getAID();

	            for (AID agentId : phoneBook) {
	            	if (!agentId.equals(myAID)) {
	            	agentId.addAddresses(uniformAddress);	
	            		
	            	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	                msg.addReceiver(agentId);
	                msg.setSender(myAID);
	                msg.setContent("StartOptimization");
	                this.brokerAgent.send(msg);
	            	}
	            }
	            this.brokerAgent.getInternalDataModel().setStartOptimizationActivated(true);
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
