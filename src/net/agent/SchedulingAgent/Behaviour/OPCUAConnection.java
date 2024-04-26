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

/**
 * Establishes a connection to an OPC UA server and configures the client.
 */
public class OPCUAConnection extends OneShotBehaviour {
	private static final long serialVersionUID = 1L;
	
	private SchedulingAgent schedulingAgent;

    public OPCUAConnection(SchedulingAgent schedulingAgent) {
        this.schedulingAgent = schedulingAgent;
    }

    @Override
    public void action() {
        String endpointURL = schedulingAgent.getInternalDataModel().getEndpointURL();
        initializeLogger();
        URI uri = parseEndpointURL(endpointURL);

        try {
            List<EndpointDescription> endpoints = discoverServerEndpoints(endpointURL, uri);
            setupClientConfig(endpoints, uri);
            connectToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        schedulingAgent.addBehaviour(new MonitorElectrolyzerState(schedulingAgent));
    }

    private void initializeLogger() {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
    }

    private URI parseEndpointURL(String endpointURL) {
        try {
            return new URI(endpointURL);
        } catch (URISyntaxException e) {
            System.err.println("Invalid URI: " + endpointURL);
            e.printStackTrace();
            return null;
        }
    }

    private List<EndpointDescription> discoverServerEndpoints(String endpointURL, URI uri) throws Exception {
        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointURL).get();
        schedulingAgent.getInternalDataModel().setEndpoints(endpoints);
        return endpoints;
    }

    private void setupClientConfig(List<EndpointDescription> endpoints, URI uri) throws Exception {
        EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(0), uri.getHost(), uri.getPort());
        schedulingAgent.getInternalDataModel().setConfigPoint(configPoint);
        
        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(configPoint);
        schedulingAgent.getInternalDataModel().getCfg().setEndpoint(configPoint);
    }

    private void connectToServer() throws Exception {
        OpcUaClient client = OpcUaClient.create(schedulingAgent.getInternalDataModel().getCfg().build());
        client.connect().get();
        AddressSpace addressSpace = client.getAddressSpace();
        schedulingAgent.getInternalDataModel().setAddressSpace(addressSpace);
        System.out.println("PEA-Agent:" + schedulingAgent.getLocalName() + " connected to OPC-UA MTP-Server");
    }
}



/*
 * BackUp ist funktionierender Code.
 
public class OPCUAConnection extends OneShotBehaviour {

	public OPCUAConnection(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	SchedulingAgent schedulingAgent;

	@Override
	public void action() {
		
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

*/
