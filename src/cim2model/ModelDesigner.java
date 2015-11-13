package cim2model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import cim2model.io.CIMReaderJENA;
import cim2model.mapping.modelica.MapAttribute;
import cim2model.mapping.modelica.PwBusMap;
import cim2model.mapping.modelica.PwLineMap;
import cim2model.mapping.modelica.PwLoadPQMap;
import cim2model.mapping.modelica.PwPinMap;
import cim2model.mapping.modelica.ConnectMap;
import cim2model.model.cim.CIMModel;

/**
 * Read mapping files and create appropriate objects ComponentMap, 
 * Get corresponding values from CIM model, into objects ComponentMap
 * Save objects ComponentMap in memory
 * @author fran_jo
 *
 */
public class ModelDesigner 
{
	Map<String, String> connections;
//	ArrayList<ConnectMap> connections;
	Map<Object, String> equipment;
	Map<Resource, RDFNode> components;
	CIMReaderJENA cimReader;
	CIMModel modelCIM;
	
	public ModelDesigner(String _cimSource)
	{
		cimReader= new CIMReaderJENA(_cimSource);
	}
	
	public Map<Resource, RDFNode> load_CIMModel()
	{
		modelCIM = new CIMModel(cimReader.readModel());
		components = modelCIM.gatherComponents();
		this.connections= new HashMap<String,String>();
//		this.connections= new ArrayList<ConnectMap>();
		this.equipment= new HashMap<Object, String>();
		
		return components;
	}
	
	/**
	 * 
	 * @param _key
	 * @return
	 */
	public String[] get_CIMComponentName(Resource _key)
	{
		return modelCIM.retrieveComponentName(_key);
	}
	
	public Map<String, String> get_ConnectionMap(){
		return this.connections;
	}
	
//	public ArrayList<ConnectMap> get_ConnectionMap(){
//		return this.connections;
//	}
	
	public Map<Object, String> get_EquipmentMap(){
		return this.equipment;
	}
	
	private static PwPinMap pwpinXMLToObject(String _xmlmap) {
		JAXBContext context;
		Unmarshaller un;
		
		try{
			context = JAXBContext.newInstance(PwPinMap.class);
	        un = context.createUnmarshaller();
	        PwPinMap map = (PwPinMap) un.unmarshal(new File(_xmlmap));
	        return map;
        } 
        catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
	/**
	 * 
	 * @param key
	 * @param _source
	 * @param _subjectID
	 * @return
	 */
	public ConnectMap create_TerminalModelicaMap(Resource key, String _source, String[] _subjectID)
	{
		PwPinMap mapTerminal= pwpinXMLToObject(_source);
		ConnectMap connection;
		/* load corresponding tag cim:Terminal */
		Map<String, Object> cimClassMap= modelCIM.retrieveAttributesTerminal(key);
		/* iterate through map attributes, for storing proper cim values */
		ArrayList<MapAttribute> mapAttList= (ArrayList<MapAttribute>)mapTerminal.getMapAttribute();
		Iterator<MapAttribute> imapAttList= mapAttList.iterator();
		MapAttribute currentmapAtt, newmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//			System.out.println("currentmapAtt "+ currentmapAtt.toString());
		}
		mapTerminal.setConductingEquipment(cimClassMap.get("Terminal.ConductingEquipment").toString());
		mapTerminal.setTopologicalNode(cimClassMap.get("Terminal.TopologicalNode").toString());
		try 
		{ //TODO: calculate value for ii from v,angle,p,q
			double voltage= Double.valueOf(mapTerminal.getMapAttribute("vr").getContent());
			double apower= Double.valueOf(mapTerminal.getMapAttribute("P").getContent());
			double angle= Double.valueOf(mapTerminal.getMapAttribute("vi").getContent());
			double current= apower/(voltage*Math.cos(angle));
			newmapAtt= new MapAttribute();
			newmapAtt.setCimName("ir");
			newmapAtt.setMoName("ir");
			newmapAtt.setContent(Double.toString(current));
			newmapAtt.setDatatype("Real");
			newmapAtt.setVariability("none");
			newmapAtt.setVisibility("public");
			newmapAtt.setFlow("true");
			mapTerminal.setMapAttribute(newmapAtt);
			newmapAtt= new MapAttribute();
			newmapAtt.setCimName("ii");
			newmapAtt.setMoName("ii");
			newmapAtt.setContent(Double.toString(current));
			newmapAtt.setDatatype("Real");
			newmapAtt.setVariability("none");
			newmapAtt.setVisibility("public");
			newmapAtt.setFlow("true");
			mapTerminal.setMapAttribute(newmapAtt);
		}
		catch (NumberFormatException nfe)
		{
			System.err.println(nfe.getLocalizedMessage());
		}
		catch (NullPointerException npe)
		{
			System.out.println("This Terminal does not have power flow associated!");
			System.err.println(npe.getLocalizedMessage());
		}
		// add cim id, used as reference from terminal and connections to other components 
		mapTerminal.setRfdId(_subjectID[0]);
		mapTerminal.setCimName(_subjectID[1]);
		
		this.connections.put(mapTerminal.getRfdId(), 
				cimClassMap.get("Terminal.ConductingEquipment").toString().split("#")[1]);
		this.connections.put(mapTerminal.getRfdId(), 
				cimClassMap.get("Terminal.TopologicalNode").toString().split("#")[1]);
		
		connection= new ConnectMap(mapTerminal, 
				(Resource)cimClassMap.get("Terminal.ConductingEquipment"),
				(Resource)cimClassMap.get("Terminal.TopologicalNode"));
		connection.set_Ce_id(cimClassMap.get("Terminal.ConductingEquipment").toString().split("#")[1]);
		connection.set_Tn_id(cimClassMap.get("Terminal.TopologicalNode").toString().split("#")[1]);
		
		return connection;
	}
	
	private static PwLoadPQMap pwloadpqXMLToObject(String _xmlmap) {
		JAXBContext context;
		Unmarshaller un;
		
		try{
			context = JAXBContext.newInstance(PwLoadPQMap.class);
	        un = context.createUnmarshaller();
	        PwLoadPQMap map = (PwLoadPQMap) un.unmarshal(new File(_xmlmap));
	        return map;
        } 
        catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
	/**
	 * 
	 * @param key
	 * @param _source
	 * @param _subjectID
	 * @return
	 */
	public PwLoadPQMap create_LoadModelicaMap(Resource key, String _source, String[] _subjectID)
	{
		PwLoadPQMap mapEnergyC= pwloadpqXMLToObject(_source);
		Map<String, Object> cimClassMap= modelCIM.retrieveAttributesEnergyC(key);
		ArrayList<MapAttribute> mapAttList= (ArrayList<MapAttribute>)mapEnergyC.getMapAttribute();
		Iterator<MapAttribute> imapAttList= mapAttList.iterator();
		MapAttribute currentmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
		}
//		mapEnergyC.setLoadResponse(cimClassMap.get("EnergyConsumer.LoadResponse").toString());
		// add cim id, used as reference from terminal and connections to other components 
		mapEnergyC.setRfdId(_subjectID[0]);
		mapEnergyC.setCimName(_subjectID[1]);
		this.equipment.put(mapEnergyC, mapEnergyC.getClass().getName());
		
		return mapEnergyC;
	}
	
	private static PwLineMap pwlineXMLToObject(String _xmlmap) {
		JAXBContext context;
		Unmarshaller un;
		
		try{
			context = JAXBContext.newInstance(PwLineMap.class);
	        un = context.createUnmarshaller();
	        PwLineMap map = (PwLineMap) un.unmarshal(new File(_xmlmap));
	        return map;
        } 
        catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
	public PwLineMap create_LineModelicaMap(Resource key, String _source, String[] _subjectID)
	{
		PwLineMap mapACLine= pwlineXMLToObject(_source);
		Map<String, Object> cimClassMap= modelCIM.retrieveAttributes(key);
		ArrayList<MapAttribute> mapAttList= (ArrayList<MapAttribute>)mapACLine.getMapAttribute();
		Iterator<MapAttribute> imapAttList= mapAttList.iterator();
		MapAttribute currentmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
		}
		mapACLine.setRfdId(_subjectID[0]);
		mapACLine.setCimName(_subjectID[1]);
		this.equipment.put(mapACLine, mapACLine.getClass().getName());

		return mapACLine;
	}
	
	private static PwBusMap pwbusXMLToObject(String _xmlmap) {
		JAXBContext context;
		Unmarshaller un;
		
		try{
			context = JAXBContext.newInstance(PwBusMap.class);
	        un = context.createUnmarshaller();
	        PwBusMap map = (PwBusMap) un.unmarshal(new File(_xmlmap));
	        return map;
        } 
        catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
	public PwBusMap create_BusModelicaMap(Resource key, String _source, String[] _subjectID)
	{
		PwBusMap mapTopoNode= pwbusXMLToObject(_source);
		Map<String, Object> cimClassMap= modelCIM.retrieveAttributesTopoNode(key);
		ArrayList<MapAttribute> mapAttList= (ArrayList<MapAttribute>)mapTopoNode.getMapAttribute();
		Iterator<MapAttribute> imapAttList= mapAttList.iterator();
		MapAttribute currentmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
		}
		mapTopoNode.setRfdId(_subjectID[0]);
		mapTopoNode.setCimName(_subjectID[1]);
		this.equipment.put(mapTopoNode, mapTopoNode.getClass().getName());
		
		return mapTopoNode;
	}
	
	/* extends QuiescentModelWithInheritance(gamma=0.3, delta=0.01); 
	 * will focus on create the high level model from cim, with
	 * component instances
	 * connect equations
	 * loading the components from the library, that means that the first step of the conversion
	 * will use the components of the library
	 */
}
