package net.agent.BrokerAgent.Behaviour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import net.agent.SchedulingAgent.SchedulingAgent;

public class instantiateAgents extends OneShotBehaviour {

	BrokerAgent brokerAgent;

	public instantiateAgents(BrokerAgent brokerAgent) {
		this.brokerAgent = brokerAgent;
	}

	@Override
	public void action() {
		String topologyFilePath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\02_eModule\\AP3 - Prozessführung\\Electrolysis\\Topology\\topology1.mtd";
		List<Module> modules = parseTopologyFile(topologyFilePath);
		printModules(modules);
		System.out.println("-------------------");

		for (Module module : modules) {
			String amlFilePath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\02_eModule\\AP3 - Prozessführung\\Electrolysis\\MTP Lib\\"
					+ module.visibleName + ".aml";

			if (new File(amlFilePath).exists()) {
				if (containsElectrolyserInfo(amlFilePath)) {
					System.out.println(module.visibleName + " is an Electrolyser.");
				} else {
					System.out.println(module.visibleName + " is not an Electrolyser.");
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

		// Iterate over the number of agents and instantiate scheduling agents
		// TODO: Hier eigentlich validEndpoints.size() verwenden
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

	public static class Module {
		String visibleName;
		String endpointUrl;

		public Module(String visibleName, String endpointUrl) {
			this.visibleName = visibleName;
			this.endpointUrl = endpointUrl;
		}

		@Override
		public String toString() {
			return "VisibleName: " + visibleName + ", EndpointUrl: " + endpointUrl;
		}
	}

	public static List<Module> parseTopologyFile(String filePath) {
		List<Module> modules = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			String visibleName = null;
			String endpointUrl = null;

			while ((line = br.readLine()) != null) {
				if (line.contains("<VisibleName>")) {
					visibleName = line.trim().replace("<VisibleName>", "").replace("</VisibleName>", "");
				} else if (line.contains("<OPCServer ID=")) {
					String opcServerLine = line.trim();
					int idStartIndex = opcServerLine.indexOf("\"") + 1;
					int idEndIndex = opcServerLine.indexOf("\"", idStartIndex);
					String opcServerId = opcServerLine.substring(idStartIndex, idEndIndex);
					endpointUrl = opcServerLine.substring(opcServerLine.indexOf(">") + 1,
							opcServerLine.indexOf("</OPCServer>"));
				} else if (line.contains("</IsInstance>")) {
					if (visibleName != null && endpointUrl != null) {
						modules.add(new Module(visibleName, endpointUrl));
						visibleName = null;
						endpointUrl = null;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return modules;
	}

	public static void printModules(List<Module> modules) {
		for (Module module : modules) {
			System.out.println(module);
		}
	}

}
