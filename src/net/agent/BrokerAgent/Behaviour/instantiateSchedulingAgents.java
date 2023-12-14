package net.agent.BrokerAgent.Behaviour;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agentgui.core.jade.Platform;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import jade.core.behaviours.OneShotBehaviour;
import net.agent.BrokerAgent.BrokerAgent;
import net.agent.SchedulingAgent.SchedulingAgent;

public class instantiateSchedulingAgents extends OneShotBehaviour {

	BrokerAgent brokerAgent;

	public instantiateSchedulingAgents(BrokerAgent brokerAgent) {
		this.brokerAgent = brokerAgent;
	}

	@Override
	public void action() {
		Configurator.initialize(new DefaultConfiguration());
		Configurator.setRootLevel(Level.INFO);
		
		// Get XML-File
		String xmlFilePath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\08_Veröffentlichungen\\2024\\Energies\\AggregationConfig.xml";
		
		// Get valid Endpoints 
		List<String> uniqueEndpoints = readXmlAndGetEndpoints(xmlFilePath);
		this.brokerAgent.getInternalDataModel().setValidEndpoints(testAndFilterEndpoints(uniqueEndpoints));
		List<String> validEndpoints = this.brokerAgent.getInternalDataModel().getValidEndpoints();
		
		for (String string : validEndpoints) {
			System.out.println("Gültige Endpoint-URLs: " + string);
			System.out.println("Anzahl an Agenten: " + validEndpoints.size());
		}

      
		//Define the number of scheduling agents that need to be instantiated 
		int numberOfAgents = validEndpoints.size();

        // Start the JADE-Plattform
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        AgentContainer container = rt.createMainContainer(p);

        // Create a list of scheduling agents
        List<SchedulingAgent> agentList = new ArrayList<>();

        // Iterate over the number of agents and instantiate scheduling agents
        //TODO: Hier eigentlich validEndpoints.size() verwenden 
        for (int i = 1; i <= 3; i++) {
            try {
                SchedulingAgent schedulingAgent = new SchedulingAgent();
                // Add the agent to the list
                agentList.add(schedulingAgent);
                // Output
                System.out.println("SchedulingAgent " + i + " wurde erstellt.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

      // Iterate over the list and start each agent
        for (int i = 0; i < agentList.size(); i++) {
            try {
            	container.acceptNewAgent(String.valueOf(i + 1), agentList.get(i)).start();
				
			} catch (StaleProxyException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("SchedulingAgent " + (i + 1) + " wurde gestartet.");
        }
		
		// Next Behaviour to be executed
	}

	public static List<String> testAndFilterEndpoints(List<String> endpoints) {
		List<String> validEndpoints = new ArrayList<>();

		for (String endpoint : endpoints) {
			try {
				List<EndpointDescription> endpointDescription;
				EndpointDescription configPoint;

				// Split Endpoint URL
				String[] endpointParts = endpoint.split(":");
				String currentHost = endpointParts[1].substring(2); // Remove "//" from the beginning
				int currentPort = Integer.parseInt(endpointParts[2].trim());

				OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
				endpointDescription = DiscoveryClient.getEndpoints(endpoint).get();
				configPoint = EndpointUtil.updateUrl(endpointDescription.get(0), currentHost, currentPort);

				cfg.setEndpoint(configPoint);
				OpcUaClient client = OpcUaClient.create(cfg.build());

				AddressSpace addressSpace = new AddressSpace(client);
				client.connect().get();

				// Define OPC UA Node-ID
				NodeId componentNameNodeId = new NodeId(2, "VirtualElektrolyzerPEAInformationLabel.ComponentName");

				// Define corresponding OPC UA Nodes
				UaVariableNode componentNameNode = (UaVariableNode) addressSpace.getNode(componentNameNodeId);

				// Read Value from OPC UA Node
				String componentName = (String) componentNameNode.readValue().getValue().getValue();

				System.out.println("ComponentName for " + endpoint + ": " + componentName);

				// Check if componentName contains "electrolyzer"
				// TODO: ATTENTION - elektrolyzer spelling
				if (componentName.toLowerCase().contains("elektrolyzer")) {
					validEndpoints.add(endpoint);
				}

				client.disconnect().get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return validEndpoints;
	}

	public static List<String> readXmlAndGetEndpoints(String filePath) {
		List<String> endpoints = new ArrayList<>();

		try {
			File xmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);

			doc.getDocumentElement().normalize();

			NodeList onlineAggregatedList = doc.getElementsByTagName("ihua:OnlineAggregated");
			for (int i = 0; i < onlineAggregatedList.getLength(); i++) {
				Node node = onlineAggregatedList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String uri = element.getAttribute("uri");
					endpoints.add(uri);
				}
			}

			NodeList serverList = doc.getElementsByTagName("ihua:Server");
			for (int i = 0; i < serverList.getLength(); i++) {
				Node node = serverList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String uri = element.getAttribute("uri");
					endpoints.add(uri);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Use a set to remove duplicates
		Set<String> uniqueEndpointsSet = new HashSet<>(endpoints);
		endpoints.clear();
		endpoints.addAll(uniqueEndpointsSet);

		return endpoints;
	}

}
