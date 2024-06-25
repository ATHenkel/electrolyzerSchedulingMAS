package net.agent.SchedulingAgent.Behaviour;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import jade.core.behaviours.TickerBehaviour;
import net.agent.DSMInformation.SchedulingResults;
import net.agent.SchedulingAgent.SchedulingAgent;

public class SetpointUpdater extends TickerBehaviour {
    private static final long serialVersionUID = -3300876452593347028L;
    private static final int UPDATE_PERIOD = 2000; // period in milliseconds
    private static final int WRITE_TIME_DIFFERENCE = 90; // time in seconds

    private SchedulingAgent schedulingAgent;

    public SetpointUpdater(SchedulingAgent agent) {
        super(agent, UPDATE_PERIOD);
        this.schedulingAgent = agent;
    }

    @Override
    protected void onTick() {
        updateSetpoints();
    }

    private void updateSetpoints() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime lastScheduleWriteTime = schedulingAgent.getInternalDataModel().getLastScheduleWriteTime();
        Duration timeDifference = Duration.between(lastScheduleWriteTime, currentTime);

        if (shouldWriteNewValue(timeDifference)) {
            writeNewValueToPLC(currentTime);
        }
    }

    private boolean shouldWriteNewValue(Duration timeDifference) {
        return timeDifference.getSeconds() >= WRITE_TIME_DIFFERENCE;
    }

    private void writeNewValueToPLC(LocalDateTime currentTime) {
        try {
            NodeId setpointNodeId = new NodeId(2, "AnaView_AEM1_OptimizedSetpoint.V");
            UaVariableNode setpointNode = (UaVariableNode) schedulingAgent.getInternalDataModel().getAddressSpace().getNode(setpointNodeId);

            if (schedulingAgent.getInternalDataModel().isSchedulingComplete()) {
                SchedulingResults results = schedulingAgent.getInternalDataModel().getSchedulingResults();
                Map<String, Object> nextPeriodResults = results.getResult(schedulingAgent.getInternalDataModel().getSchedulingResultNextPeriod());

                if (nextPeriodResults != null) {
                    double setpoint = (double) nextPeriodResults.get("Setpoint");
                    setpointNode.writeValue(new Variant((float) setpoint));
                    schedulingAgent.getInternalDataModel().incrementSchedulingResultNextPeriod();
                    schedulingAgent.getInternalDataModel().setLastScheduleWriteTime(currentTime);
                    System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " new setpoint written: " + String.format("%.2f", setpoint) + " at " + currentTime);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
