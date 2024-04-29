package net.agent.BrokerAgent.Behaviour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import net.agent.BrokerAgent.BrokerAgent;
import net.agent.BrokerAgent.ModuleParser;
import net.agent.SchedulingAgent.SchedulingAgent;

public class instantiateAgents extends OneShotBehaviour {
	private static final long serialVersionUID = 5703081042603709680L;

	BrokerAgent brokerAgent;

	public instantiateAgents(BrokerAgent brokerAgent) {
		this.brokerAgent = brokerAgent;
	}

	@Override
	public void action() {

		// Get TopologyFile
		String topologyFilePath = "C:\\Program Files\\OrchestrationDesigner With800xA (2024)\\2024_Fair-Electrolysis-Plant\\Topology\\ElectrolysisPlant.mtd";
		System.out.println("-----");
		
		ModuleParser parser = new ModuleParser(brokerAgent);
		// Parse die Module aus der Topologie-Datei
        List<net.agent.BrokerAgent.Module> modules = parser.parseTopologyFile(topologyFilePath);
		
		this.brokerAgent.getInternalDataModel().setModules(modules);

		// Iterate over modules using Iterator to avoid ConcurrentModificationException
		Iterator<net.agent.BrokerAgent.Module> iterator = modules.iterator();
		while (iterator.hasNext()) {
			net.agent.BrokerAgent.Module module = iterator.next();

			// Identify PEA types via DeviceClass of the PEAInformationLabel in the MTP
			String amlFilePath = "C:\\Program Files\\OrchestrationDesigner With800xA (2024)\\2024_Fair-Electrolysis-Plant\\MTP Lib\\"
					+ module.visibleName + ".aml";

			if (new File(amlFilePath).exists()) {
				// Check, if Module is an electrolyser
				if (!containsElectrolyserInfo(amlFilePath)) {
					System.out.println(module.visibleName + " is not an Electrolyser. Removing from the list.");
					iterator.remove(); // Remove the module from the list
				}
			} else {
				System.out.println("File not found: " + amlFilePath);
			}
		}

		// Start the JADE-Plattform
		Runtime rt = Runtime.instance();
		Profile p = new ProfileImpl();
		AgentContainer container = rt.createMainContainer(p);

		// Create a list of scheduling agents
		List<SchedulingAgent> agentList = new ArrayList<>();

		// Get Number of PEA-Agents
		int numberofAgents = modules.size();

		// Iterate over the number of agents and instantiate scheduling agents
		for (int i = 0; i <= numberofAgents - 1; i++) {
			try {
				// Get current Module
				net.agent.BrokerAgent.Module currentModule = modules.get(i);

				// Add .mtp extension to the MTP file
				String mtpFileName = currentModule.visibleName + ".mtp";
				SchedulingAgent schedulingAgent = new SchedulingAgent(currentModule.endpointUrl, mtpFileName,
						numberofAgents);
				// Add the agent to the list
				agentList.add(schedulingAgent);

				// Output
				System.out.println("PEA-agent " + (i + 1) + " was instantiated.");
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
			System.out.println("PEA-agent " + (i + 1) + " was started.");
		}
	}

	// Method for reading out the MTP. The DeviceClass in the PEAInformationLabel is
	// used to check whether it is an electrolyser
	public static boolean containsElectrolyserInfo(String filePath) {
		try {
			File inputFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);

			doc.getDocumentElement().normalize();

			NodeList externalInterfaceList = doc.getElementsByTagName("ExternalInterface");

			for (int temp = 0; temp < externalInterfaceList.getLength(); temp++) {
				Node externalInterfaceNode = externalInterfaceList.item(temp);

				if (externalInterfaceNode.getNodeType() == Node.ELEMENT_NODE) {
					Element externalInterfaceElement = (Element) externalInterfaceNode;

					NodeList valueList = externalInterfaceElement.getElementsByTagName("Value");

					for (int i = 0; i < valueList.getLength(); i++) {
						Node valueNode = valueList.item(i);

						if (valueNode.getNodeType() == Node.ELEMENT_NODE) {
							String valueContent = valueNode.getTextContent().toLowerCase();

							if (valueContent.contains("electrolyser")) {
								return true; // Electrolyser information found
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false; // Electrolyser information not found
	}

}
