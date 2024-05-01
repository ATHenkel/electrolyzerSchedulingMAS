package net.agent.BrokerAgent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a topology file and extracts modules for a BrokerAgent.
 */
public class PlantTopologyParser {
	
    private BrokerAgent brokerAgent;

    /**
     * Constructs a ModuleParser with a reference to the BrokerAgent.
     * @param brokerAgent The BrokerAgent that will manage the extracted modules.
     */
    public PlantTopologyParser(BrokerAgent brokerAgent) {
        this.brokerAgent = brokerAgent;
    }
	
    /**
     * Reads the specified topology file and extracts module information.
     * Each module is defined by its visible name, endpoint URL, and instance name.
     *
     * @param filePath The file path of the topology file to be parsed.
     * @return A list of extracted modules.
     */
    public List<Module> parseTopologyFile(String filePath) {
        List<Module> modules = new ArrayList<>();
        String line;
        String visibleName = null;
        String endpointUrl = null;
        String instanceName = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                if (line.contains("<VisibleName>")) {
                    visibleName = line.trim().replace("<VisibleName>", "").replace("</VisibleName>", "");
                    // Check whether all information is available after VisibleName has been set
                    if (visibleName != null && endpointUrl != null && instanceName != null) {
                        modules.add(new Module(visibleName, endpointUrl, instanceName));
                        System.out.println("Module added when VisibleName is set: " +
                            "\n  VisibleName: " + visibleName +
                            "\n  EndpointURL: " + endpointUrl +
                            "\n  InstanceName: " + instanceName);
                        // Reset the variables to add new modules correctly
                        visibleName = null;
                        endpointUrl = null;
                        instanceName = null;
                    }
                } else if (line.contains("<OPCServer ID=")) {
                    int startIdx = line.indexOf(">") + 1;
                    int endIdx = line.indexOf("</OPCServer>");
                    if (startIdx != 0 && endIdx != -1 && endIdx > startIdx) {
                        endpointUrl = line.substring(startIdx, endIdx);
                    }
                    // Check whether all information is available after EndpointURL has been set
                    if (visibleName != null && endpointUrl != null && instanceName != null) {
                        modules.add(new Module(visibleName, endpointUrl, instanceName));
                        System.out.println("Module added when EndpointURL is set: " +
                            "\n  VisibleName: " + visibleName +
                            "\n  EndpointURL: " + endpointUrl +
                            "\n  InstanceName: " + instanceName);
                     // Reset the variables
                        visibleName = null;
                        endpointUrl = null;
                        instanceName = null;
                    }
                } else if (line.trim().startsWith("<Tag ID=")) {
                    instanceName = line.trim().replaceFirst(".*<Tag ID=\"\\d+\">", "").replace("</Tag>", "");
                    // Check whether all information is available after InstanceName has been set
                    if (visibleName != null && endpointUrl != null && instanceName != null) {
                        modules.add(new Module(visibleName, endpointUrl, instanceName));
                        System.out.println("Module added when InstanceName is set: " +
                            "\n  VisibleName: " + visibleName +
                            "\n  EndpointURL: " + endpointUrl +
                            "\n  InstanceName: " + instanceName);
                     // Reset the variables
                        visibleName = null;
                        endpointUrl = null;
                        instanceName = null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save the modules in the internal data model
        this.brokerAgent.getInternalDataModel().setModules(modules);
        
        // Return of the modules from the internal data model 
        return this.brokerAgent.getInternalDataModel().getModules();
    }
}