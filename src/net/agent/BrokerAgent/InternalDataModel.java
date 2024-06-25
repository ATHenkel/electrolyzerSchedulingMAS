package net.agent.BrokerAgent;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import agentgui.core.common.AbstractUserObject;
import jade.core.AID;

/**
 * Internal data model of Broker-Agent for storing agent configurations and managing shared resources.
 */
public class InternalDataModel extends AbstractUserObject {
	private static final long serialVersionUID = 5964943261619302454L;
	
	// OPC UA specific attributes for transferring optimized setpoints
	private String endpointURL;
	private OpcUaClient opcUaClient;
	private List<EndpointDescription> endpoints;
	private OpcUaClientConfigBuilder cfg;
	private EndpointDescription configPoint;
	private AddressSpace addressSpace;
    private List<String> validEndpoints; 
    private List<AID> phoneBook;
    private List<Module> modules = new ArrayList<>();
    private boolean startOptimizationActivated = false;
    
    public boolean isStartOptimizationActivated() {
		return startOptimizationActivated;
	}

	public void setStartOptimizationActivated(boolean startOptimizationActivated) {
		this.startOptimizationActivated = startOptimizationActivated;
	}

	// Module management
    public void addModule(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    // Valid endpoints
    public List<String> getValidEndpoints() {
        if (validEndpoints == null) {
            validEndpoints = new ArrayList<>();
        }
        return validEndpoints;
    }

    public void setValidEndpoints(List<String> validEndpoints) {
        this.validEndpoints = validEndpoints;
    }

    
	// ---- OPC UA Communication ----
	
	/**
	 * Gets the endpoint URL for the OPC UA connection.
	 * @return the endpoinst URL
	 */
	public String getEndpointURL() {
	    return endpointURL;
	}

	/**
	 * Sets the endpoint URL for the OPC UA connection.
	 * @param endpointURL the new endpoint URL
	 */
	public void setEndpointURL(String endpointURL) {
	    this.endpointURL = endpointURL;
	}

	/**
	 * Retrieves the OPC UA client instance.
	 * @return the OPC UA client.
	 */
	public OpcUaClient getOpcUaClient() {
	    return opcUaClient;
	}

	/**
	 * Sets the OPC UA client instance.
	 * @param opcUaClient the OPC UA client to be set.
	 */
	public void setOpcUaClient(OpcUaClient opcUaClient) {
	    this.opcUaClient = opcUaClient;
	}

	/**
	 * Retrieves a list of endpoint descriptions. Initializes the list if it is null.
	 * @return a list of endpoint descriptions.
	 */
	public List<EndpointDescription> getEndpoints() {
	    if (endpoints == null) {
	        endpoints = new ArrayList<>();
	    }
	    return endpoints;
	}
	
	public List<EndpointDescription> getPeaEndpoints() {
	    if (endpoints == null) {
	    	endpoints = new ArrayList<>();
	    }
	    return endpoints;
	}

	/**
	 * Sets the list of endpoint descriptions.
	 * @param endpoints the list of endpoint descriptions to be set.
	 */
	public void setEndpoints(List<EndpointDescription> endpoints) {
	    this.endpoints = endpoints;
	}

	/**
	 * Retrieves the configuration point for the OPC UA connection.
	 * @return the configuration point.
	 */
	public EndpointDescription getConfigPoint() {
	    return configPoint;
	}
	
	/**
	 * Sets the configuration point for the OPC UA connection.
	 * @param configPoint the configuration point to be set.
	 */
	public void setConfigPoint(EndpointDescription configPoint) {
	    this.configPoint = configPoint;
	}
	

	/**
	 * Retrieves the OPC UA client's address space.
	 * @return the address space.
	 */
	public AddressSpace getAddressSpace() {
	    return addressSpace;
	}


	/**
	 * Sets the OPC UA client's address space.
	 * @param addressSpace the address space to be set.
	 */
	public void setAddressSpace(AddressSpace addressSpace) {
	    this.addressSpace = addressSpace;
	}
	

	/**
	 * Retrieves the OPC UA client configuration builder, initializing it if necessary.
	 * @return the configuration builder.
	 */
	public OpcUaClientConfigBuilder getCfg() {
	    if (cfg == null) {
	        cfg = new OpcUaClientConfigBuilder();
	    }
	    return cfg;
	}
	

	/**
	 * Sets the OPC UA client configuration builder.
	 * @param cfg the configuration builder to be set.
	 */
	public void setCfg(OpcUaClientConfigBuilder cfg) {
	    this.cfg = cfg;
	}
 

    // Phone book management
    public void addAID2PhoneBook(AID agentAID) {
        getPhoneBook().add(agentAID);
    }

    public List<AID> getPhoneBook() {
        if (phoneBook == null) {
            phoneBook = new ArrayList<>();
        }
        return phoneBook;
    }

        public void setPhoneBook(List<AID> phoneBook) {
        this.phoneBook = phoneBook;
    }
}
