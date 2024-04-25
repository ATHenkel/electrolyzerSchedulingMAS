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
import net.agent.SchedulingAgent.SchedulingAgent;

public class instantiateAgents extends OneShotBehaviour {

	BrokerAgent brokerAgent;

	public instantiateAgents(BrokerAgent brokerAgent) {
		this.brokerAgent = brokerAgent;
	}

	@Override
	public void action() {
		
		//Get TopologyFile
		String topologyFilePath = "C:\\Program Files\\OrchestrationDesigner With800xA (2024)\\2024_Fair-Electrolysis-Plant\\Topology\\ElectrolysisPlant.mtd";
		
		List<Module> modules = parseTopologyFile(topologyFilePath);
		this.brokerAgent.getInternalDataModel().setModules(modules);

		// Iterate over modules using Iterator to avoid ConcurrentModificationException
		Iterator<Module> iterator = modules.iterator();
		while (iterator.hasNext()) {
			Module module = iterator.next();
			
			//Identify PEA types via DeviceClass of the PEAInformationLabel in the MTP
			String amlFilePath = "C:\\Program Files\\OrchestrationDesigner With800xA (2024)\\2024_Fair-Electrolysis-Plant\\MTP Lib\\"
			+ module.visibleName + ".aml";

			if (new File(amlFilePath).exists()) {
				//Check, if Module is an electrolyser 
				if (!containsElectrolyserInfo(amlFilePath)) {
					System.out.println(module.visibleName + " is not an Electrolyser. Removing from the list.");
					iterator.remove(); // Remove the module from the list
				}
			} else {
				System.out.println("File not found: " + amlFilePath);
			}
		}

		// Print the remaining Modules
		printModules();

		// Start the JADE-Plattform
		Runtime rt = Runtime.instance();
		Profile p = new ProfileImpl();
		AgentContainer container = rt.createMainContainer(p);

		// Create a list of scheduling agents
		List<SchedulingAgent> agentList = new ArrayList<>();
		
		//Get Number of PEA-Agents
		int numberofAgents = modules.size();

		// Iterate over the number of agents and instantiate scheduling agents
		for (int i = 0; i <= numberofAgents-1; i++) {
			try {
				//Get current Module
				Module currentModule = modules.get(i);
				
				//Add .mtp extension to the MTP file
				String mtpFileName = currentModule.visibleName + ".mtp";
				SchedulingAgent schedulingAgent = new SchedulingAgent(currentModule.endpointUrl, mtpFileName, numberofAgents); 
				// Add the agent to the list
				agentList.add(schedulingAgent);

				// Output
				System.out.println("PEA-agent " + (i+1) + " was instantiated.");
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

	
	// Method for reading out the MTP. The DeviceClass in the PEAInformationLabel is used to check whether it is an electrolyser
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

	//Definition of a module (PEA)
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

	// Method for parsing the plant topology and identifying PEAs 
	public List<Module> parseTopologyFile(String filePath) {

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			String visibleName = null; //Corresponds to the name of the MTP
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
						this.brokerAgent.getInternalDataModel().addModule(new Module(visibleName, endpointUrl));

						visibleName = null;
						endpointUrl = null;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// return modules;
		return this.brokerAgent.getInternalDataModel().getModules();
	}

	// Method for displaying all modules
	public void printModules() {
		List<Module> modules = this.brokerAgent.getInternalDataModel().getModules();
		for (Module module : modules) {
			System.out.println(module);
		}
	}

}
