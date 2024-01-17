package net.agent.BrokerAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import agentgui.core.common.AbstractUserObject;
import jade.core.AID;
import net.agent.BrokerAgent.Behaviour.instantiateAgents.Module;

public class InternalDataModel extends AbstractUserObject {
	
	// OPC UA 
	private OpcUaClient opcUaClient;
	private List<EndpointDescription> endpoints;
	private OpcUaClientConfigBuilder cfg;
	private EndpointDescription configPoint;
	private AddressSpace addressSpace;
	
	//MAS instantiation
	List<String> validEndpoints; 
	
	//MAS information 
	List<AID> phoneBook;
	
	//Module-List
	List<Module> modules = new ArrayList<>();
	
	// ---- Getter & Setter ----
	
    public void addModule(Module module) {
        modules.add(module);
    }
	
	public List<Module> getModules() {
		return modules;
	}

	public void setModules(List<Module> modules) {
		this.modules = modules;
	}
	public List<String> getValidEndpoints() {
		if (validEndpoints == null) {
			validEndpoints = new ArrayList<>();
		}
		return validEndpoints;
	}

	public void setValidEndpoints(List<String> validEndpoints) {
		this.validEndpoints = validEndpoints;
	}
	
	public OpcUaClient getOpcUaClient() {
		return opcUaClient;
	}

	public void setOpcUaClient(OpcUaClient opcUaClient) {
		this.opcUaClient = opcUaClient;
	}

	public List<EndpointDescription> getEndpoints() {
	    if (endpoints == null) {
	        endpoints = new ArrayList<>(); 
	    }
	    return endpoints;
	}

	public void setEndpoints(List<EndpointDescription> endpoints) {
		this.endpoints = endpoints;
	}

	public EndpointDescription getConfigPoint() {
		return configPoint;
	}

	public void setConfigPoint(EndpointDescription configPoint) {
		this.configPoint = configPoint;
	}

	public AddressSpace getAddressSpace() {
		return addressSpace;
	}

	public void setAddressSpace(AddressSpace addressSpace) {
		this.addressSpace = addressSpace;
	}
	
	public OpcUaClientConfigBuilder getCfg() {
		if (cfg == null) {
			cfg = new OpcUaClientConfigBuilder();
		}
		return cfg;
	}

	public void setCfg(OpcUaClientConfigBuilder cfg) {
		this.cfg = cfg;
	}
	
	
	// ---- PHONE-BOOK ----
	public void addAID2PhoneBook(AID agentAID) {
		phoneBook = getPhoneBook();
		phoneBook.add(agentAID);
	}

	public List<AID> getPhoneBook() {
		if (phoneBook == null) {
			phoneBook = new ArrayList<AID>();
		}

		return phoneBook;
	}

	public void setPhoneBook(List<AID> phoneBook) {
		this.phoneBook = phoneBook;
	}

}
