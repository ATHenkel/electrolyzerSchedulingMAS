package net.agent.SchedulingAgent.Behaviour;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class OPCUAConnection extends OneShotBehaviour {

	public OPCUAConnection(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	SchedulingAgent schedulingAgent;

	private boolean checkConnection() {
		try {
			// Address of Simulation
			List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints("opc.tcp://139.11.207.61:8001").get();
			// Weitere Verbindungstests hier, wenn nötig
			return true; // Die Verbindung war erfolgreich
		} catch (Exception e) {
			e.printStackTrace();
			return false; // Die Verbindung ist fehlgeschlagen
		}
	}

	@Override
	public void action() {

		Configurator.initialize(new DefaultConfiguration());
		Configurator.setRootLevel(Level.INFO);

		// Internal Data Model
		List<EndpointDescription> endpoints = this.schedulingAgent.getInternalDataModel().getEndpoints();
		EndpointDescription configPoint = this.schedulingAgent.getInternalDataModel().getConfigPoint();
		OpcUaClientConfigBuilder cfg = this.schedulingAgent.getInternalDataModel().getCfg();
		OpcUaClient client = this.schedulingAgent.getInternalDataModel().getOpcUaClient();
		AddressSpace addressSpace = this.schedulingAgent.getInternalDataModel().getAddressSpace();

		try {
			// Address of Simulation
			endpoints = DiscoveryClient.getEndpoints("opc.tcp://139.11.207.61:8001").get();
			configPoint = EndpointUtil.updateUrl(endpoints.get(0), "139.11.207.61", 8001);
			cfg.setEndpoint(configPoint);
			// cfg.setIdentityProvider(new UsernameProvider("admin", "wago")); //set Password, if necessary

			// Store the OPC UA information in the internal data model
			this.schedulingAgent.getInternalDataModel().setEndpoints(endpoints);
			this.schedulingAgent.getInternalDataModel().setConfigPoint(configPoint);
			this.schedulingAgent.getInternalDataModel().getCfg().setEndpoint(configPoint);

			// Connect to client
			System.out.println("Agent:" + this.schedulingAgent.getLocalName() + "connect to OPC-UA PEA-Server");
			client = OpcUaClient.create(cfg.build());
			client.connect().get();
			addressSpace = client.getAddressSpace();
			this.schedulingAgent.getInternalDataModel().setAddressSpace(addressSpace);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Next Behaviour to be executed
		MonitorElectrolyzerState monitorElectrolyzerState = new MonitorElectrolyzerState(schedulingAgent);
		this.schedulingAgent.addBehaviour(monitorElectrolyzerState);
	}

}
