package net.agent.SchedulingAgent.Behaviour;

import java.util.ArrayList;
import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * This behavior initializes the production goals and retrieves additional necessary data
 * from an SQL database for the PEA-agent.
 */
public class GetGoalOfProduction extends OneShotBehaviour {
	private static final long serialVersionUID = -5918598233604475430L;
	
	private SchedulingAgent schedulingAgent;

    /**
     * Constructor for initializing the behavior with a reference to its scheduling agent.
     * @param schedulingAgent the agent this behavior belongs to.
     */
    public GetGoalOfProduction(SchedulingAgent schedulingAgent) {
        this.schedulingAgent = schedulingAgent;
    }
    
    @Override
    public void action() {
        // Initialize or test the shutdown order list.
        setupShutdownOrder();

        // Connect to and read from SQL database.
        connectAndReadFromDatabase();

        // Schedule the next behavior in the agent's life cycle.
        transitionToNextBehavior();
    }

    /**
     * Sets up or tests the shutdown order list which might be used to determine the order
     * in which production facilities should reduce output or shut down.
     */
    private void setupShutdownOrder() {
    	//TODO: 1. Improve Shutdown-List
        ArrayList<Integer> shutdownOrderList = schedulingAgent.getInternalDataModel().getShutdownOrderList();
        shutdownOrderList.add(2);
        shutdownOrderList.add(1);
        shutdownOrderList.add(4);
        shutdownOrderList.add(3);
        shutdownOrderList.add(5);
        shutdownOrderList.add(6);
        shutdownOrderList.add(7);
        shutdownOrderList.add(8);
    }

    /**
     * Establishes a connection to the SQL database and reads necessary data.
     */
    private void connectAndReadFromDatabase() {
        System.out.println("Agent " + schedulingAgent.getLocalName() + " connecting to SQL database");
        SQLDatabaseConnector connector = new SQLDatabaseConnector(schedulingAgent);
        connector.readDataFromDatabase();
    }

    /**
     * Transitions to the next behavior, typically to start minimizing production costs or adjusting production levels.
     */
    private void transitionToNextBehavior() {
        MinimizeX minimizeX = new MinimizeX(schedulingAgent);
        schedulingAgent.addBehaviour(minimizeX);
    }
}
