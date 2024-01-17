package net.agent.SchedulingAgent.Behaviour;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import Standardintegrationprofile.ProductionCurveData;
import Standardintegrationprofile.Property;
import Standardintegrationprofile.StandardIntegrationProfile;
import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class initializeKnowledgebase extends OneShotBehaviour {

	SchedulingAgent schedulingAgent;

	public initializeKnowledgebase(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	@Override
	public void action() {

			System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
					+ " parse Standardintegrationsprofile to initalize knowledgeBase");

			List<Property> properties = parseJsonList();
//			for (Property property : properties) {
//				System.out.println("Name: " + property.getName());
//				System.out.println("Value: " + property.getValue());
//				System.out.println("Unit: " + property.getUnit());
//				System.out.println("Description: " + property.getDescription());
//				System.out.println("----------------------");
//			}

			for (Property property : properties) {
				switch (property.getName()) {
				case "Stack-Power":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel().setPEL(Double.parseDouble((String) value));
							System.out.println("Stack-Power successfully initialized");
						} else {
							System.err.println("Error: Property 'Stack-Power' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "CapEx":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel().setCapEx(Double.parseDouble((String) value));
							System.out.println("CapEx successfully initialized");
						} else {
							System.err.println("Error: Property 'CapEx' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "OM-Costs":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel().setOMFactor(Double.parseDouble((String) value));
							System.out.println("OM-Costs successfully initialized");
						} else {
							System.err.println("Error: Property 'OM-Costs' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "Utilization-Time":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel()
									.setUtilizationtime(Integer.parseInt((String) value));
							System.out.println("Utilization-Time successfully initialized");
						} else {
							System.err.println("Error: Property 'Utilization-Time' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "Load-Factor":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel()
									.setLoadFactor(Double.parseDouble((String) value));
							System.out.println("Load-Factor successfully initialized");
						} else {
							System.err.println("Error: Property 'Load-Factor' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "Discount-Rate":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel()
									.setDiscountrate(Double.parseDouble((String) value));
							System.out.println("Discount-Rate successfully initialized");
						} else {
							System.err.println("Error: Property 'Discount-Rate' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "Operation-Limit-Low":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel().setMinPower(Double.parseDouble((String) value));
							System.out.println("Operating-Limit-Low successfully initialized");
						} else {
							System.err.println("Error: Property 'Operation-Limit-Low' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "Operation-Limit-High":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel().setMaxPower(Double.parseDouble((String) value));
							System.out.println("Operating-Limit-High successfully initialized");
						} else {
							System.err.println("Error: Property 'Operation-Limit-High' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "Cold-Start-Time":
					try {
						Object value = property.getValue();
						if (value instanceof String) {
							this.schedulingAgent.getInternalDataModel()
									.setStartUpDuration(Double.parseDouble((String) value));
							System.out.println("Cold-Start-Time successfully initialized");
						} else {
							System.err.println("Error: Property 'Cold-Start-Time' is not a String");
						}
					} catch (NumberFormatException e) {
						System.err.println("Error during conversion of the data type");
					}
					break;

				case "Production-Curve-Data":
					try {
						Object value = property.getValue();
						if (value instanceof List<?>) {
							Gson gson = new Gson();
							String json = gson.toJson(value);
							Type listType = new TypeToken<List<ProductionCurveData>>() {
							}.getType();
							List<ProductionCurveData> productionCurveDataList = gson.fromJson(json, listType);
							
							System.out.println("Print Production Curve");
							
							for (ProductionCurveData data : productionCurveDataList) {
							    System.out.println("X: " + data.getX());
							    System.out.println("Y: " + data.getY());
							    System.out.println("----------------------");}

							// Data points of the hydrogen production curve 
				            double[] utilization = productionCurveDataList.stream().mapToDouble(ProductionCurveData::getX).toArray();
				            double[] productionRate = productionCurveDataList.stream().mapToDouble(ProductionCurveData::getY).toArray();
				            
				            // Fitting
				            double[] coefficients = fitter.fit(pointsToList(utilization, productionRate, 0));
				            coefficients = adjustCoefficientsForShift(coefficients, utilization, productionRate);
				            
				            WeightedObservedPoints points = new WeightedObservedPoints();
				            for (int i = 0; i < utilization.length; i++) {
				                points.add(utilization[i], productionRate[i]);
				            }
				            
				            // Calculation of the R^2 value
				            double SSR = 0.0; // Sum of Squared Residuals
				            double SST = 0.0; // Total Sum of Squares
				            double meanY = points.toList().stream().mapToDouble(WeightedObservedPoint::getY).average().orElse(0.0);

				            for (WeightedObservedPoint point : points.toList()) {
				                double predictedY = evaluatePolynomial(coefficients, point.getX());
				                SSR += Math.pow(point.getY() - predictedY, 2);
				                SST += Math.pow(point.getY() - meanY, 2);
				            }
				            double rSquared = 1.0 - (SSR / SST);
				            
				            // Coefficient output
				            System.out.println("Coefficients of the quadratic approximation:");
				            for (int i = 0; i < coefficients.length; i++) {
				                System.out.println("Coefficient " + i + ": " + coefficients[i]);
				            }
				            System.out.println("----------------------");
				            System.out.println("R^2-Value: " + rSquared);
				            
				            // Set Coefficient f(x) = A*x^2 + B*x + C
				            this.schedulingAgent.getInternalDataModel().setProductionCoefficientA(coefficients[2]); 
				            this.schedulingAgent.getInternalDataModel().setProductionCoefficientB(coefficients[1]);
				            this.schedulingAgent.getInternalDataModel().setProductionCoefficientC(coefficients[0]);
				            
							System.out.println("Production-Curve-Data successfully initialized");
						} else {
							System.err.println("Error: Property 'Production-Curve-Data' is not a List");
						}
					} catch (Exception e) {
						System.err.println("Error during parsing of Production-Curve-Data");
						e.printStackTrace();
					}
					break;
				}
			}
		}

	
	public static List<Property> parseJsonList() {
        List<Property> properties = new ArrayList<>();

        String zipFilePath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\02_eModule\\AP3 - Prozessführung\\Enapter MTPs\\EnapterElectrolyser_extended.mtp";
        String jsonFileName = "Integrationprofile.json";

        try (InputStream fis = Files.newInputStream(Paths.get(zipFilePath));
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().equals(jsonFileName)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }

                    String jsonString = baos.toString();
                    Gson gson = new GsonBuilder().create();

                    StandardIntegrationProfile profile = gson.fromJson(jsonString,
                            StandardIntegrationProfile.class);

                    // Zugriff auf die Eigenschaften des geparsten Objekts
                    properties.addAll(profile.getProperties());
                    break; // Wir haben die Datei gefunden, also beenden wir die Schleife
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }
	
	//Code for quadratic Approximation of production curve
	private static PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
	
    private static double[] adjustCoefficientsForShift(double[] coefficients, double[] utilization, double[] productionRate) {
        double minProduction = Double.MAX_VALUE;

        for (int i = 0; i < utilization.length; i++) {
            double production = coefficients[0] + coefficients[1] * utilization[i] + coefficients[2] * Math.pow(utilization[i], 2);
            if (production < minProduction) {
                minProduction = production;
            }
        }

        if (minProduction < 0) {
            for (double shift = 0; ; shift -= 0.000001) {
                coefficients = fitter.fit(pointsToList(utilization, productionRate, shift));
                minProduction = Double.MAX_VALUE;
                for (int i = 0; i < utilization.length; i++) {
                    double production = coefficients[0] + coefficients[1] * utilization[i] + coefficients[2] * Math.pow(utilization[i], 2);
                    if (production < minProduction) {
                        minProduction = production;
                    }
                }

                if (minProduction >= 0) {
                    break;
                }
            }
        }

        return coefficients;
    }

    private static Collection<WeightedObservedPoint> pointsToList(double[] x, double[] y, double shift) {
        List<WeightedObservedPoint> points = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            points.add(new WeightedObservedPoint(1, x[i], y[i] - shift));
        }
        return points;
    }
    
    private static double evaluatePolynomial(double[] coefficients, double x) {
        double result = 0.0;
        for (int i = 0; i < coefficients.length; i++) {
         result += coefficients[i] * Math.pow(x, i);
        }
        return result;
    }
	
	
}
