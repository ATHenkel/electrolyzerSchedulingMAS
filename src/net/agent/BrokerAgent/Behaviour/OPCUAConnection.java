package net.agent.BrokerAgent.Behaviour;

import java.net.URI;
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
import net.agent.BrokerAgent.BrokerAgent;
import net.agent.BrokerAgent.InternalDataModel;

/**
 * Establishes connections to OPC UA servers for both monitoring and setpoint configuration.
 */
public class OPCUAConnection extends OneShotBehaviour {
    private static final long serialVersionUID = -7587807736983972875L;
    private BrokerAgent brokerAgent;

    public OPCUAConnection(BrokerAgent agent) {
        this.brokerAgent = agent;
    }

    @Override
    public void action() {
    	//Set Endpoint URL of MAS-MTP-Server
        this.brokerAgent.getInternalDataModel().setEndpointURL("opc.tcp://127.0.0.1:4200");
        configureLogger();
        
        establishConnection(
            brokerAgent.getInternalDataModel().getEndpointURL(),
            brokerAgent.getInternalDataModel(),
            false // False for MAS-Server
        );

        //Add next behaviour here 
        this.brokerAgent.addBehaviour(new MonitorOptimizationService(brokerAgent));
    }

    /**
     * Configures the logger to the INFO level.
     */
    private void configureLogger() {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
    }

    /**
     * Establishes connection to an OPC UA server.
     * @param endpointURL The URL of the server.
     * @param dataModel The internal data model containing configuration details.
     * @param isPea Indicates if the connection is for the PEA server.
     */
    private void establishConnection(String endpointURL, InternalDataModel dataModel, boolean isPea) {
        try {
            URI uri = new URI(endpointURL);
            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointURL).get();
            EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(0), uri.getHost(), uri.getPort());
            OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder().setEndpoint(configPoint);

            OpcUaClient client = OpcUaClient.create(cfg.build());
            client.connect().get();
            AddressSpace addressSpace = client.getAddressSpace();
            
                dataModel.setEndpoints(endpoints);
                dataModel.setConfigPoint(configPoint);
                dataModel.setCfg(cfg);
                dataModel.setAddressSpace(addressSpace);
                
            System.out.println("Agent:" + this.brokerAgent.getLocalName() + " connected to OPC-UA Server at: " + endpointURL);
        } catch (Exception e) {
            System.err.println("Failed to connect to OPC-UA Server at: " + endpointURL);
            e.printStackTrace();
        }
    }
}
