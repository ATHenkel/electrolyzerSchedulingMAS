//
//package Standardintegrationprofile;
//
//import javax.annotation.Generated;
//import com.google.gson.annotations.Expose;
//import com.google.gson.annotations.SerializedName;
//
//@Generated("jsonschema2pojo")
//public class Property {
//
//    @SerializedName("name")
//    @Expose
//    private String name;
//    @SerializedName("value")
//    @Expose
//    private String value;
//    @SerializedName("valueDataType")
//    @Expose
//    private String valueDataType;
//    @SerializedName("unit")
//    @Expose
//    private String unit;
//    @SerializedName("description")
//    @Expose
//    private String description;
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public String getValueDataType() {
//        return valueDataType;
//    }
//
//    public void setValueDataType(String valueDataType) {
//        this.valueDataType = valueDataType;
//    }
//
//    public String getUnit() {
//        return unit;
//    }
//
//    public void setUnit(String unit) {
//        this.unit = unit;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//}

package Standardintegrationprofile;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Generated("jsonschema2pojo")
public class Property {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("value")
    @Expose
    private Object value; // Verwende Object, da der Wert verschiedene Typen haben kann
    @SerializedName("valueDataType")
    @Expose
    private String valueDataType;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("description")
    @Expose
    private String description;

    // Wenn die Eigenschaft "value" eine Liste ist, enthält sie ProductionCurveData-Objekte
    private List<ProductionCurveData> productionCurveData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getValueDataType() {
        return valueDataType;
    }

    public void setValueDataType(String valueDataType) {
        this.valueDataType = valueDataType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ProductionCurveData> getProductionCurveData() {
        return productionCurveData;
    }

    public void setProductionCurveData(List<ProductionCurveData> productionCurveData) {
        this.productionCurveData = productionCurveData;
    }
}


