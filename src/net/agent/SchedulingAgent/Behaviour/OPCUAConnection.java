package net.agent.SchedulingAgent.Behaviour;

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
import net.agent.SchedulingAgent.InternalDataModel;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * Establishes connections to OPC UA servers for both monitoring and setpoint configuration.
 */
public class OPCUAConnection extends OneShotBehaviour {
    private static final long serialVersionUID = -7587807736983972875L;
    private SchedulingAgent schedulingAgent;

    public OPCUAConnection(SchedulingAgent agent) {
        this.schedulingAgent = agent;
    }

    @Override
    public void action() {
        //TODO: Test for Setting Endpoint URL of PEA
        this.schedulingAgent.getInternalDataModel().setPeaEndpointURL("opc.tcp://139.11.207.61:8001");
        configureLogger();
        
        establishConnection(
            schedulingAgent.getInternalDataModel().getEndpointURL(),
            schedulingAgent.getInternalDataModel(),
            false // False for MAS-Server
        );
        establishConnection(
            schedulingAgent.getInternalDataModel().getPeaEndpointURL(),
            schedulingAgent.getInternalDataModel(),
            true // True for PEA-Server
        );

        schedulingAgent.addBehaviour(new SetpointUpdater(schedulingAgent));
        schedulingAgent.addBehaviour(new MonitorElectrolyzerState(schedulingAgent));
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

            if (isPea) {
                dataModel.setPeaEndpoints(endpoints);
                dataModel.setPeaConfigPoint(configPoint);
                dataModel.setPeaCfg(cfg);
                dataModel.setPeaAddressSpace(addressSpace);
            } else {
                dataModel.setEndpoints(endpoints);
                dataModel.setConfigPoint(configPoint);
                dataModel.setCfg(cfg);
                dataModel.setAddressSpace(addressSpace);
            }

            System.out.println("Agent:" + this.schedulingAgent.getLocalName() + " connected to OPC-UA Server at: " + endpointURL);
        } catch (Exception e) {
            System.err.println("Failed to connect to OPC-UA Server at: " + endpointURL);
            e.printStackTrace();
        }
    }
}
