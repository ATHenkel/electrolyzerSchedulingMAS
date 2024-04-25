package net.agent.SchedulingAgent;

public class LambdaController {
    private double lambda = 0.1; // Startwert für Lambda
    private double stepSize = 0.1; // Start-Schrittweite für Lambda
    private double lastDelta = Double.MAX_VALUE; // Speichert die letzte Diskrepanz

    public void updateLambda(double currentX, double currentZ) {
        double currentDelta = Math.abs(currentX - currentZ);

        // Reduziere die Schrittweite, wenn sich die Diskrepanz verringert
        if (currentDelta < lastDelta) {
            stepSize *= 0.9; // Reduziere Schrittweite um 10%
        } else {
            stepSize *= 1.1; // Erhöhe Schrittweite um 10%, falls keine Verbesserung
        }

        // Update Lambda basierend auf der Richtung der Veränderung
        if (currentX > currentZ) {
            lambda += stepSize;
        } else if (currentX < currentZ) {
            lambda -= stepSize;
        }

        // Speichere die aktuelle Diskrepanz für den nächsten Update
        lastDelta = currentDelta;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }
}
