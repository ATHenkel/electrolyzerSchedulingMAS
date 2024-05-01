
package caex;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceHierarchy {
	
	@XmlAttribute(name = "Name")
	String Name;
	
	@XmlAttribute(name = "ID")
	String ID;
	
	@XmlAttribute(name = "text")
	String text;
	
	@XmlElement(name = "InternalElement", namespace = "http://www.dke.de/CAEX")
	List<InternalElement> InternalElement;
	
	public List<InternalElement> getInternalElement() {
		return this.InternalElement;
	}

	public void setInternalElement(List<InternalElement> InternalElement) {
		this.InternalElement = InternalElement;
	}

	public String getName() {
		return this.Name;
	}

	public void setName(String Name) {
		this.Name = Name;
	}

	public String getID() {
		return this.ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String gettext() {
		return this.text;
	}

	public void settext(String text) {
		this.text = text;
	}

}
