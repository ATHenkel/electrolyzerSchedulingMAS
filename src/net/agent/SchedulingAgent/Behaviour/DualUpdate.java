package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * This class handles the periodic update and broadcasting of state and production data,
 * as well as computation and adjustment of lambda values based on operational constraints and targets.
 */
public class DualUpdate extends OneShotBehaviour {

    private SchedulingAgent schedulingAgent;

    /**
     * Constructs a ProductionStateUpdater behaviour with reference to its SchedulingAgent.
     * @param schedulingAgent the agent this behaviour is part of.
     */
    public DualUpdate(SchedulingAgent schedulingAgent) {
        this.schedulingAgent = schedulingAgent;
    }
    
    /**
     * Writes results of the current iteration into a CSV file.
     */
    private void writeResultsToCsv(String agentId, int period, int iteration, double ownProduction,
                                   double receivedProductionQuantity, double demand, double x, double z, double gradient,
                                   double lambda, double demandPercentage, long currentTimeMs,
                                   int shutdownOrderIndex, int shutdownElectrolyzer, boolean stateProduction,
                                   boolean stateStandby) {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String datePrefix = sdf.format(new Date());
        String filepath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\out\\" + datePrefix + "_DualUpdate.csv";
        String header = "";
        String data = agentId + ";" + period + ";" + iteration + ";" + ownProduction + ";" +
                      receivedProductionQuantity + ";" + demand + ";" + x + ";" + z + ";" + gradient + ";" +
                      lambda + ";" + demandPercentage + ";" + currentTimeMs + ";" + shutdownOrderIndex + ";" +
                      shutdownElectrolyzer + ";" + stateProduction + ";" + stateStandby;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
            if (iteration == 0 && period == 1 && Integer.valueOf(this.schedulingAgent.getLocalName()) == 1) {
                header = "Agent;Period;Iteration;Own Production;Received Production;Demand;X;Z;Gradient;Lambda;Demand Deviation %;Current Time;Shutdown Order Index;Shutdown Electrolyzer;State Production;State Standby";
                writer.write(header);
                writer.newLine();
            }
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the gradient of the Marginal Levelized Cost of Hydrogen (mLCOH).
     * @param x the x value to calculate the gradient for.
     * @return the calculated gradient.
     */
    private double calculateGradientmLCOH(double x) {
        int currentPeriod = schedulingAgent.getInternalDataModel().getCurrentPeriod();
        double PEL = schedulingAgent.getInternalDataModel().getPEL();
        double ProductionCoefficientA = schedulingAgent.getInternalDataModel().getProductionCoefficientA();
        double ProductionCoefficientB = schedulingAgent.getInternalDataModel().getProductionCoefficientB();
        double ProductionCoefficientC = schedulingAgent.getInternalDataModel().getProductionCoefficientC();
        double electricityPrice = schedulingAgent.getInternalDataModel().getDSMInformation().getElectricityPriceForPeriod(currentPeriod);

        return (electricityPrice * PEL) / (100 * (ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x + ProductionCoefficientC))
               - (electricityPrice * PEL * x * (2 * ProductionCoefficientA * x + ProductionCoefficientB))
               / (100 * Math.pow(ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x + ProductionCoefficientC, 2));
    }

    @Override
    public void action() {
        // Retrieve necessary data from internal data model
        int currentIteration = schedulingAgent.getInternalDataModel().getIteration();
        int currentPeriod = schedulingAgent.getInternalDataModel().getCurrentPeriod();
        double productionQuantity = schedulingAgent.getInternalDataModel().getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration);
        double demand = schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
        double sumProduction = schedulingAgent.getInternalDataModel().getSumProduction();
        double lambda = schedulingAgent.getInternalDataModel().getLambda();
        double penaltyFactor = schedulingAgent.getInternalDataModel().getPenaltyFactor();
        double z = schedulingAgent.getInternalDataModel().getZ();
        double x = schedulingAgent.getInternalDataModel().getX();
        double demandDeviation = productionQuantity + sumProduction - demand;
        double demandPercentage = Math.abs(demandDeviation / demand + 0.000000000001); // Avoid division by zero
        long currentMilliseconds = System.currentTimeMillis();
        int shutdownOrderIndex = schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
        int electrolyzerShutdown = schedulingAgent.getInternalDataModel().getShutdownOrderValue(shutdownOrderIndex);

        if (schedulingAgent.getInternalDataModel().isStateProduction()) {
            double delta = Math.abs(x - z);
            double k;
            double limitedExponent = delta > 0.01 ? 3.7 : 1.0;
            penaltyFactor = delta > 0.01 ? 0.62021 : 0.3;
            k = Math.exp(limitedExponent);
            double gradient = Math.abs(calculateGradientmLCOH(x)) + 0.00001;
            lambda = x > 0 ? lambda + penaltyFactor * (x - z) * gradient * k : 0;
        }
        
        writeResultsToCsv(schedulingAgent.getLocalName(), currentPeriod, currentIteration, productionQuantity, sumProduction, demand, x, z, calculateGradientmLCOH(x), lambda, demandPercentage, currentMilliseconds, shutdownOrderIndex, electrolyzerShutdown, schedulingAgent.getInternalDataModel().isStateProduction(), schedulingAgent.getInternalDataModel().isStateStandby());
        schedulingAgent.getInternalDataModel().setLambda(lambda);
        schedulingAgent.getInternalDataModel().incrementIteration();
        schedulingAgent.getInternalDataModel().setEnableMessageReceive(true);
        schedulingAgent.addBehaviour(new MinimizeX(schedulingAgent));
    }
}
