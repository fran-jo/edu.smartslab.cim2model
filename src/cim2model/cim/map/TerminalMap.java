//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.12.14 at 08:30:25 PM CET 
//


package cim2model.cim.map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="cim_name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="cim_rfdid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="datatype" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mo_name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "mapTerminal")
public class TerminalMap {

    @XmlAttribute(name = "cim_name", required = true)
    protected String cimName;
    @XmlAttribute(name = "cim_rfdid", required = true)
    protected String cimRfdid;
    @XmlAttribute(name = "datatype", required = true)
    protected String datatype;
    @XmlAttribute(name = "mo_name", required = true)
    protected String moName;

    /**
     * Gets the value of the cimName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCimName() {
        return cimName;
    }

    /**
     * Sets the value of the cimName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCimName(String value) {
        this.cimName = value;
    }

    /**
     * Gets the value of the cimRfdid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCimRfdid() {
        return cimRfdid;
    }

    /**
     * Sets the value of the cimRfdid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCimRfdid(String value) {
        this.cimRfdid = value;
    }

    /**
     * Gets the value of the datatype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * Sets the value of the datatype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatatype(String value) {
        this.datatype = value;
    }

    /**
     * Gets the value of the moName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMoName() {
        return moName;
    }

    /**
     * Sets the value of the moName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMoName(String value) {
        this.moName = value;
    }

}
