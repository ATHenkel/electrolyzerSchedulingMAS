package net.agent.SchedulingAgent.Behaviour;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class OPCUAConnection extends OneShotBehaviour {

	public OPCUAConnection(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	SchedulingAgent schedulingAgent;

	@Override
	public void action() {
		
    	//Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
		
		//TODO: OPC UA Connection for Agent 1 and 3
		if (agentId == 1 || agentId == 3){
			
		// Internal Data Model
		List<EndpointDescription> endpoints = this.schedulingAgent.getInternalDataModel().getEndpoints();
		EndpointDescription configPoint = this.schedulingAgent.getInternalDataModel().getConfigPoint();
		OpcUaClientConfigBuilder cfg = this.schedulingAgent.getInternalDataModel().getCfg();
		OpcUaClient client = this.schedulingAgent.getInternalDataModel().getOpcUaClient();
		AddressSpace addressSpace = this.schedulingAgent.getInternalDataModel().getAddressSpace();
		String endpointURL = this.schedulingAgent.getInternalDataModel().getEndpointURL();
		
		//Decompose the endpointURL into host and port
		URI uri;
		String host = null;
		int port = 0;
		try {
			uri = new URI(endpointURL);
		       host = uri.getHost();
		       port = uri.getPort();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
 
		Configurator.initialize(new DefaultConfiguration());
		Configurator.setRootLevel(Level.INFO);

		try {
			// Address of Simulation
			endpoints = DiscoveryClient.getEndpoints(endpointURL).get(); 
			configPoint = EndpointUtil.updateUrl(endpoints.get(0), host, port);
			
			cfg.setEndpoint(configPoint);
			// cfg.setIdentityProvider(new UsernameProvider("admin", "xx")); //set Password, if necessary

			// Store the OPC UA information in the internal data model
			this.schedulingAgent.getInternalDataModel().setEndpoints(endpoints);
			this.schedulingAgent.getInternalDataModel().setConfigPoint(configPoint);
			this.schedulingAgent.getInternalDataModel().getCfg().setEndpoint(configPoint);

			// Connect to client
			System.out.println("PEA-Agent:" + this.schedulingAgent.getLocalName() + "connect to OPC-UA MTP-Server");
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
}
