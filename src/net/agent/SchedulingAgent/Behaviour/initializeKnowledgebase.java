package net.agent.SchedulingAgent.Behaviour;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import Standardintegrationprofile.ProductionCurveData;
import Standardintegrationprofile.Property;
import Standardintegrationprofile.StandardIntegrationProfile;
import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;
import net.bytebuddy.asm.Advice.This;

public class initializeKnowledgebase extends OneShotBehaviour {

	SchedulingAgent schedulingAgent;

	public initializeKnowledgebase(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	@Override
	public void action() {

		System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
				+ " parse Standardintegrationsprofile to initalize knowledgeBase");
		String mtpFileName = this.schedulingAgent.getInternalDataModel().getMtpFileName();

		// Get Properties by Parsing Standardintegrationprofile and save in List
		List<Property> properties = parseJsonList(mtpFileName);

		// Initialize knowledge base with values from the standard integration profile
		for (Property property : properties) {
			switch (property.getName()) {
			case "Stack-Power":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel().setPEL(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Stack-Power successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'Stack-Power' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "CapEx":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel().setCapEx(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName() + " CapEx of "
								+ Double.parseDouble((String) value) + "€ successfully initialized");
					} else {

						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'CapEx' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "OM-Costs":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel().setOMFactor(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " OM-Costs successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'OM-Costs' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "Utilization-Time":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel()
								.setUtilizationtime(Integer.parseInt((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Utilization-Time successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'Utilization-Time' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "Load-Factor":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel().setLoadFactor(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Load-Factor successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'Load-Factor' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "Discount-Rate":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel().setDiscountrate(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Discount-Rate successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'Discount-Rate' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "Operation-Limit-Low":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel().setMinPower(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Operating-Limit-Low successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'Operation-Limit-Low' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "Operation-Limit-High":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel().setMaxPower(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Operating-Limit-High successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'Operation-Limit-High' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
				}
				break;

			case "Cold-Start-Time":
				try {
					Object value = property.getValue();
					if (value instanceof String) {
						this.schedulingAgent.getInternalDataModel()
								.setStartUpDuration(Double.parseDouble((String) value));
						System.out.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Cold-Start-Time successfully initialized");
					} else {
						System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
								+ " Error: Property 'Cold-Start-Time' is not a String");
					}
				} catch (NumberFormatException e) {
					System.err.println("PEA-Agent: " + this.schedulingAgent.getLocalName()
							+ " Error during conversion of the data type");
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

						/**
						 * The code for the automated generation of the quadratic regression based on
						 * the data points of the hydrogen production curve is listed below
						 */

						// Data points of the hydrogen production curve
						double[] utilization = productionCurveDataList.stream().mapToDouble(ProductionCurveData::getX)
								.toArray();
						double[] productionRate = productionCurveDataList.stream()
								.mapToDouble(ProductionCurveData::getY).toArray();

						WeightedObservedPoints points = new WeightedObservedPoints();
						for (int i = 0; i < utilization.length; i++) {
							points.add(utilization[i], productionRate[i]);
						}

						// Fitting
						double[] coefficients = fitter.fit(pointsToList(utilization, productionRate, 0));
						// coefficients = adjustCoefficientsForShift(coefficients, utilization,
						// productionRate);

						// Calculation of the R^2 value
						double SSR = 0.0; // Sum of Squared Residuals
						double SST = 0.0; // Total Sum of Squares
						double meanY = points.toList().stream().mapToDouble(WeightedObservedPoint::getY).average()
								.orElse(0.0);

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

	public static List<Property> parseJsonList(String mtpFileName) {
		List<Property> properties = new ArrayList<>();

		// FilePath of the MTP database
		String zipDirectoryPath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\02_eModule\\AP4 - Integration\\Messe\\2024";

		// Create complete path to the MTP file
		String zipFilePath = Paths.get(zipDirectoryPath, mtpFileName).toString();
		String jsonFileName = "Integrationprofile.json";

		File mtpFile = new File(zipFilePath);

		// Check, if MTP-File exists
		if (mtpFile.exists()) {
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

						// Access to the properties of the parsed object
						properties.addAll(profile.getProperties());
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Error: MTP file not found - " + zipFilePath);
		}

		return properties;
	}

	// Code for quadratic Approximation of production curve
	private static PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);

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

	private static void writeDataToColumn(XSSFSheet sheet, double[] data, int column) {
		int rowCount = 1; // Start with the second line (index 1) so as not to overwrite the headings
		for (int i = 0; i < data.length; i++) {
			Row row = sheet.getRow(rowCount);
			if (row == null) {
				row = sheet.createRow(rowCount);
			}
			Cell cell = row.createCell(column);
			cell.setCellValue(data[i]);
			rowCount++; // Only increase rowCount if a new data value is written
		}
	}

	private void writeDataToExcel(String filepath, XSSFSheet sheet, double[] utilization, double[] productionRate,
			XSSFWorkbook workbook, double coefficient0) {
		try (workbook) {

			// Write Data to .xlsx file
			writeDataToColumn(sheet, utilization, 0);
			writeDataToColumn(sheet, productionRate, 1);

			// Write coefficients 0 in the table
			Row row = sheet.getRow(8);
			Cell cell = row.createCell(5);
			cell.setCellValue(coefficient0);

			workbook.setForceFormulaRecalculation(true);

			CellType coefficient2 = workbook.getSheet("Data").getRow(5).getCell(5).getCachedFormulaResultType();
			String coefficient1 = workbook.getSheet("Data").getRow(6).getCell(5).getRawValue();

			System.out.println("Coefficient A: " + coefficient1);
			System.out.println("Coefficient B: " + coefficient2);

			try (FileOutputStream outFile = new FileOutputStream(filepath)) {
				workbook.write(outFile);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
