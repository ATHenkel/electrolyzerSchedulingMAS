package net.agent.BrokerAgent;

/**
 * Represents a single module in a system. This class holds the details
 * necessary to uniquely identify and describe a module within the BrokerAgent's context.
 */
public class Module {
    // Fields to store the visible name, endpoint URL, and instance name of the module.
    public String visibleName;
    public String endpointUrl;
    public String instanceName;

    /**
     * Constructs a new Module with specified characteristics.
     * 
     * @param visibleName The publicly visible name of the module. This name is used for display and identification purposes.
     * @param endpointUrl The endpoint URL where the module can be accessed or communicated with. This URL is critical for network operations involving this module.
     * @param instanceName The unique name or identifier for this instance of the module. This helps in differentiating multiple instances of the same type of module.
     */
    public Module(String visibleName, String endpointUrl, String instanceName) {
        this.visibleName = visibleName;
        this.endpointUrl = endpointUrl;
        this.instanceName = instanceName;
    }

    /**
     * Retrieves the visible name of the module.
     * 
     * @return The visible name of the module.
     */
    public String getVisibleName() {
        return visibleName;
    }

    /**
     * Retrieves the endpoint URL of the module.
     * 
     * @return The endpoint URL of the module.
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * Retrieves the instance name of the module.
     * 
     * @return The instance name of the module.
     */
    public String getInstanceName() {
        return instanceName;
    }
}
