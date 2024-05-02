package net.agent.BrokerAgent.Behaviour;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import mtpModification.AMLSectionGenerator;
import net.agent.BrokerAgent.BrokerAgent;
import net.agent.BrokerAgent.PlantTopologyParser;
import net.agent.SchedulingAgent.SchedulingAgent;
import opcuaServer.*;

public class instantiateAgents extends OneShotBehaviour {
    private static final long serialVersionUID = 5703081042603709680L;
    private BrokerAgent brokerAgent;

    public instantiateAgents(BrokerAgent brokerAgent) {
        this.brokerAgent = brokerAgent;
    }

    @Override
    public void action() {
        List<net.agent.BrokerAgent.Module> modules = loadModules();
        filterModules(modules);
        List<AID> phoneBook = createPhoneBook(modules);
        initializeAgentPlatform(modules);
//        handleAMLModification(phoneBook);
//        
//        try {
//            Server_Agent.initializeAndStartServer();
//        } catch (Exception e) {
//            System.err.println("Fehler beim Starten des OPC-UA Servers: " + e.getMessage());
//        }
//        
//        System.out.println("OPC Server gestartet");
    }

    /**
     * Loads modules from a specified topology file and updates the internal data model of the broker agent.
     * This method uses the {@link PlantTopologyParser} to parse the topology file located at a predefined path.
     * It ensures that the list of modules parsed from the file is stored in the broker agent's internal data model.
     *
     * @return A list of {@link net.agent.BrokerAgent.Module} objects representing the modules parsed from the topology file.
     */
    private List<net.agent.BrokerAgent.Module> loadModules() {
        String topologyFilePath = "C:\\Program Files\\OrchestrationDesigner With800xA (2024)\\2024_Fair-Electrolysis-Plant\\Topology\\ElectrolysisPlant.mtd";
        PlantTopologyParser parser = new PlantTopologyParser(brokerAgent);
        List<net.agent.BrokerAgent.Module> modules = parser.parseTopologyFile(topologyFilePath);
        brokerAgent.getInternalDataModel().setModules(modules);
        return modules;
    }

    /**
     * Filters out modules that are not electrolyzers from the provided list of modules.
     * This method iterates through each module in the list and checks if the module is an electrolyzer
     * by calling the {@link #isElectrolyser(net.agent.BrokerAgent.Module)} method. Non-electrolyzer modules are removed.
     * @param modules The list of modules to be filtered, which is modified in place to remove non-electrolyzer modules.
     */
    private void filterModules(List<net.agent.BrokerAgent.Module> modules) {
        Iterator<net.agent.BrokerAgent.Module> iterator = modules.iterator();
        while (iterator.hasNext()) {
            if (!isElectrolyser(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private boolean isElectrolyser(net.agent.BrokerAgent.Module module) {
        String amlFilePath = "C:\\Program Files\\OrchestrationDesigner With800xA (2024)\\2024_Fair-Electrolysis-Plant\\MTP Lib\\" + module.visibleName + ".aml";
        if (new File(amlFilePath).exists()) {
            return containsElectrolyserInfo(amlFilePath);
        } else {
            System.out.println("File not found: " + amlFilePath);
            return false;
        }
    }

    
    /**
     * Initializes the agent platform by setting up the JADE container, creating a phone book, 
     * and instantiating scheduling agents based on the provided list of modules.
     * @param modules The list of modules used to initialize the agent platform. 
     */
    private void initializeAgentPlatform(List<net.agent.BrokerAgent.Module> modules) {
        AgentContainer container = startJadePlatform();
        List<AID> phoneBook = createPhoneBook(modules);
        instantiateSchedulingAgents(container, modules, phoneBook);
    }

    private AgentContainer startJadePlatform() {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        return rt.createMainContainer(p);
    }

    /**
     * Creates a phone book for the agent system by generating unique agent identifiers (AID) from the list of modules.
     * Each module's instance name is used to construct an AID, with a numerical suffix indicating its order.
     * The modules are sorted by the numerical part of their instance names to ensure consistent ordering.
     * @param modules The list of modules to be used for generating the agent identifiers.
     * @return A list of AIDs representing each agent in a sorted order based on the module names.
     */
    private List<AID> createPhoneBook(List<net.agent.BrokerAgent.Module> modules) {
        // Sortiere die Module basierend auf der nummerischen Komponente im Namen
        modules.sort(Comparator.comparingInt(module -> extractNumber(module.instanceName)));

        List<AID> phoneBook = new ArrayList<>();
        for (int i = 0; i < modules.size(); i++) {
            String agentName = modules.get(i).instanceName + "--PEAAgent" + (i + 1);
            phoneBook.add(new AID(agentName, AID.ISLOCALNAME));
        }
        return phoneBook;
    }

    /**
     * Instantiates and starts scheduling agents for each module using the specified JADE container.
     * Each agent is associated with a module and has a unique agent identifier from the phone book.
     * The method initializes each scheduling agent with its corresponding module details and starts them in the container.
     * @param container The JADE container where agents are to be instantiated and started.
     * @param modules The list of modules, each corresponding to one scheduling agent.
     * @param phoneBook A list of AIDs that contains the identifiers for all agents to be started.
     */
    private void instantiateSchedulingAgents(AgentContainer container, List<net.agent.BrokerAgent.Module> modules, List<AID> phoneBook) {
        for (int i = 0; i < phoneBook.size(); i++) {
            String mtpFileName = modules.get(i).visibleName + ".mtp";
            AID agentAID = phoneBook.get(i);
            SchedulingAgent schedulingAgent = new SchedulingAgent(agentAID.getLocalName(), mtpFileName, modules.size(), phoneBook);
            try {
                container.acceptNewAgent(agentAID.getLocalName(), schedulingAgent).start();
                System.out.println("PEA-agent named " + agentAID.getLocalName() + " was instantiated and started.");
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Instantiates and starts scheduling agents for each module based on the provided phone book.
     * This method iterates through the phone book, creating a scheduling agent for each AID (Agent IDentifier) using
     * the corresponding module's information. Each agent is then added to the specified container and started.
     * @param container The JADE container in which the agents will be instantiated.
     * @param modules The list of modules containing configuration information for each agent.
     * @param phoneBook The list of AIDs corresponding to each agent, used for naming and linking agents.
     */
    private void handleAMLModification(List<AID> phoneBook) {
        Path sourcePath = Paths.get("D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\MTP-Template\\in\\Manifest.aml");
        Path targetPath = Paths.get("D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\MTP-Template\\out\\Manifest.aml");
        String zipFilePath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\MTP-Template\\out\\AP4_HSU_AgentenIntegration.mtp";

        try {
            // Kopiere die Basis-AML-Datei an den Zielort
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Füge für jeden Agenten die _State und _Setpoint Variablen hinzu
            for (AID aid : phoneBook) {
                String baseName = aid.getLocalName().split(":")[0]; // Extrahiere den Basisnamen aus dem Agenten-Namen
                AMLSectionGenerator.addSetpointToAML(targetPath.toString(), baseName + "_State", true, false);
                AMLSectionGenerator.addSetpointToAML(targetPath.toString(), baseName + "_Setpoint", false, true);
            }

            System.out.println("File successfully copied and modified.");
            // Verpacke die modifizierte Datei in ein Zip-Archiv
            AMLSectionGenerator.addFileToZip(zipFilePath, targetPath.toString());
            System.out.println("File added to zip successfully.");
        } catch (IOException e) {
            System.err.println("Error when copying or modifying the file: " + e.getMessage());
        }
    }
    
    private int extractNumber(String name) {
        Matcher matcher = Pattern.compile("\\d+").matcher(name);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0; //throw an exception if the format is unexpected
    }

    /**
     * Checks if the given file contains information about electrolyzers.
     */
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
                                return true; // Electrolyzer information found
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Electrolyzer information not found
    }
}
