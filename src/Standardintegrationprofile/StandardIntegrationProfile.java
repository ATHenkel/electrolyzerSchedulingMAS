
package Standardintegrationprofile;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class StandardIntegrationProfile {

    @SerializedName("identification")
    @Expose
    private Identification identification;
    @SerializedName("dataModelVersion")
    @Expose
    private String dataModelVersion;
    @SerializedName("mtpFileReference")
    @Expose
    private String mtpFileReference;
    @SerializedName("typeSpecification")
    @Expose
    private TypeSpecification typeSpecification;
    @SerializedName("properties")
    @Expose
    private List<Property> properties;

    public Identification getIdentification() {
        return identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public String getDataModelVersion() {
        return dataModelVersion;
    }

    public void setDataModelVersion(String dataModelVersion) {
        this.dataModelVersion = dataModelVersion;
    }

    public String getMtpFileReference() {
        return mtpFileReference;
    }

    public void setMtpFileReference(String mtpFileReference) {
        this.mtpFileReference = mtpFileReference;
    }

    public TypeSpecification getTypeSpecification() {
        return typeSpecification;
    }

    public void setTypeSpecification(TypeSpecification typeSpecification) {
        this.typeSpecification = typeSpecification;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

}
