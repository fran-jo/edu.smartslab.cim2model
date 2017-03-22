package cim2model;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import cim2model.cim.CIMModel;
import cim2model.cim.CIMTransformerEnd;
import cim2model.cim.SVProfileModel;
import cim2model.cim.TPProfileModel;
import cim2model.cim.map.*;
import cim2model.cim.map.ipsl.branches.*;
import cim2model.cim.map.ipsl.buses.*;
import cim2model.cim.map.ipsl.connectors.*;
import cim2model.cim.map.ipsl.controls.es.ESDC1AMap;
import cim2model.cim.map.ipsl.loads.*;
import cim2model.cim.map.ipsl.machines.*;
import cim2model.cim.map.ipsl.transformers.*;
import cim2model.io.ReaderCIM;

/**
 * Read mapping files and create appropriate objects ComponentMap, Get corresponding values from CIM model 
 * into objects ComponentMap, Save objects ComponentMap in memory
 * @author fran_jo
 *
 */
public class ModelDesigner 
{
	ArrayList<ConnectionMap> connections;
	Map<Resource, RDFNode> classes_EQ;
	Map<Resource, RDFNode> classes_TP;
	Map<Resource, RDFNode> classes_SV;
	ReaderCIM reader_EQ_profile;
	ReaderCIM reader_TP_profile;
	ReaderCIM reader_SV_profile;
	CIMModel profile_EQ;
	TPProfileModel profile_TP;
	SVProfileModel profile_SV;
	
	public ModelDesigner(String _source_EQ_profile, String _source_TP_profile, String _source_SV_profile)
	{
		reader_EQ_profile= new ReaderCIM(_source_EQ_profile);
		reader_TP_profile= new ReaderCIM(_source_TP_profile);
		reader_SV_profile= new ReaderCIM(_source_SV_profile);
		this.connections= new ArrayList<ConnectionMap>();
	}
	
	public Map<Resource, RDFNode> load_EQ_profile(String _xmlns_cim)
	{
		profile_EQ = new CIMModel(reader_EQ_profile.read_profile(_xmlns_cim));
		classes_EQ = profile_EQ.gatherComponents();
		
		return classes_EQ;
	}
	
	public Map<Resource, RDFNode> load_TP_profile(String _xmlns_cim)
	{
		profile_TP = new TPProfileModel(reader_TP_profile.read_profile(_xmlns_cim));
		classes_TP = profile_TP.gatherTopologicalNodes();
		
		return classes_TP;
	}
	
	public Map<Resource, RDFNode> load_SV_profile(String _xmlns_cim)
	{
		profile_SV = new SVProfileModel(reader_SV_profile.read_profile(_xmlns_cim));
		classes_SV = profile_SV.gatherSvPowerFlow();
		
		return classes_SV;
	}
	
	/**
	 * 
	 * @param _key
	 * @return
	 */
	public String[] get_CIMComponentName(Resource _key)
	{
		return profile_EQ.retrieveComponentName(_key);
	}

	/**
	 * 
	 * @return
	 */
	public ConnectionMap get_CurrentConnectionMap(){
		int last= this.connections.size()- 1;
		return this.connections.get(last);
	}
	/**
	 * 
	 * @return
	 */
	public ArrayList<ConnectionMap> get_ConnectionMap(){
		return this.connections;
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
	
	private void add_newConnectionMap(PwPinMap _mapTerminal, Hashtable<String,Resource> _pinComponents){
		//object with pin rfdif, conducting equipment and topologicalnode
		//rfdid use, by modelbuilder, to retrieve instanc_name of pin
		ConnectionMap nuevaConnection = new ConnectionMap(
				_mapTerminal.getRfdId(),
				_pinComponents.get("ConductingEquipment").toString().split("#")[1],
				_pinComponents.get("TopologicalNode").toString().split("#")[1]);
		nuevaConnection.setConductingEquipment(_pinComponents.get("ConductingEquipment"));
		nuevaConnection.setTopologicalNode(_pinComponents.get("TopologicalNode"));
		
//		this.connections.add(new ConnectionMap(,
//				_cimClassMap.get("Terminal.ConductingEquipment").toString().split("#")[1],
//				_cimClassMap.get("Terminal.TopologicalNode").toString().split("#")[1]));
		
		this.connections.add(nuevaConnection);
	}
	/**
	 * 
	 * @param key
	 * @param _source
	 * @param _subjectID
	 * @return
	 */
	public PwPinMap create_TerminalModelicaMap(Resource key, String _source, String[] _subjectID)
	{
		PwPinMap mapTerminal= pwpinXMLToObject(_source);
		/* load corresponding tag cim:Terminal */
		Map<String, Object> profile_EQ_map= profile_EQ.retrieveAttributesTerminal(key);
		//TODO check if the Terminal has svPowerFlow 
		//TODO check if the Terminal only have svVoltage -> via TopologicalNode no, only in the create_toponode_map
		Map<String, Object> profile_SV_map= profile_SV.retrieveAttributesTerminal(_subjectID);
		Map<String, Object> profile_TP_map= profile_TP.retrieveAttributesTerminal(_subjectID);
		/* iterate through map attributes, for storing proper cim values */
		ArrayList<AttributeMap> mapAttList= (ArrayList<AttributeMap>)mapTerminal.getAttributeMap();
		Iterator<AttributeMap> imapAttList= mapAttList.iterator();
		AttributeMap currentmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)profile_SV_map.get(currentmapAtt.getCimName()));
//			System.out.println("currentmapAtt "+ currentmapAtt.toString());
		}
		mapTerminal.setConductingEquipment(profile_EQ_map.get("Terminal.ConductingEquipment").toString());
		mapTerminal.setTopologicalNode(profile_TP_map.get("Terminal.TopologicalNode").toString());
//		// add cim id, used as reference from terminal and connections to other components 
		mapTerminal.setRfdId(_subjectID[0]);
		mapTerminal.setCimName(_subjectID[1]);
//		// create new entrance to the connection map, is used to draw the connections between components
//		Hashtable<String,Resource> pinComponents= new Hashtable<String,Resource>();
//		pinComponents.put("ConductingEquipment", (Resource)profile_EQ_map.get("Terminal.ConductingEquipment"));
//		pinComponents.put("TopologicalNode", (Resource)cimClassMap.get("Terminal.TopologicalNode"));
//		this.add_newConnectionMap(mapTerminal, pinComponents);
//		pinComponents.clear(); pinComponents= null;
//		modelCIM.clearAttributes();
		
		return mapTerminal;
	}
	
//	private static GENCLSMap genclsXMLToObject(String _xmlmap) {
//		JAXBContext context;
//		Unmarshaller un;
//		
//		try{
//			context = JAXBContext.newInstance(GENCLSMap.class);
//	        un = context.createUnmarshaller();
//	        GENCLSMap map = (GENCLSMap) un.unmarshal(new File(_xmlmap));
//	        return map;
//        } 
//        catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//	private static GENROUMap genrouXMLToObject(String _xmlmap) {
//		JAXBContext context;
//		Unmarshaller un;
//		
//		try{
//			context = JAXBContext.newInstance(GENROUMap.class);
//	        un = context.createUnmarshaller();
//	        GENROUMap map = (GENROUMap) un.unmarshal(new File(_xmlmap));
//	        return map;
//        } 
//        catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//	private static GENSALMap gensalXMLToObject(String _xmlmap) {
//		JAXBContext context;
//		Unmarshaller un;
//		
//		try{
//			context = JAXBContext.newInstance(GENSALMap.class);
//	        un = context.createUnmarshaller();
//	        GENSALMap map = (GENSALMap) un.unmarshal(new File(_xmlmap));
//	        return map;
//        } 
//        catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//	private static GENROEMap genroeXMLToObject(String _xmlmap) {
//		JAXBContext context;
//		Unmarshaller un;
//		
//		try{
//			context = JAXBContext.newInstance(GENROEMap.class);
//	        un = context.createUnmarshaller();
//	        GENROEMap map = (GENROEMap) un.unmarshal(new File(_xmlmap));
//	        return map;
//        } 
//        catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//	public String typeOfSynchronousMachine(Resource key)
//	{
//		String rotorType= profile_DY.checkSynchronousMachineType(key);
//		
//		return rotorType;
//	}
//	/**
//	 * 
//	 * @param key
//	 * @param _source
//	 * @param _subjectID
//	 * @return
//	 */
//	public GENCLSMap create_GENCLSModelicaMap(Resource key, String _source, String[] _subjectID)
//	{
//		GENCLSMap mapSyncMach= genclsXMLToObject(_source);
//		Map<String, Object> cimClassMap= profile_DY.retrieveAttributesSyncMach(key);
//		Iterator<AttributeMap> imapAttList= mapSyncMach.getAttributeMap().iterator();
//		AttributeMap currentmapAtt;
//		while (imapAttList.hasNext()) {
//			currentmapAtt= imapAttList.next();
//			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//		}
//		mapSyncMach.setName("GENCLS");
//		mapSyncMach.setRfdId(_subjectID[0]);
//		mapSyncMach.setCimName(_subjectID[1]);
//
//		profile_DY.clearAttributes();
//		
//		return mapSyncMach;
//	}
//	/**
//	 * 
//	 * @param key
//	 * @param _source
//	 * @param _subjectID
//	 * @return
//	 */
//	public GENROUMap create_GENROUModelicaMap(Resource key, String _source, String[] _subjectID)
//	{
//		GENROUMap mapSyncMach= genrouXMLToObject(_source);
//		Map<String, Object> cimClassMap= profile_EQ.retrieveAttributesSyncMach(key);
//		Iterator<AttributeMap> imapAttList= mapSyncMach.getAttributeMap().iterator();
//		AttributeMap currentmapAtt;
//		while (imapAttList.hasNext()) {
//			currentmapAtt= imapAttList.next();
//			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//		}
//		mapSyncMach.setName("GENROU");
//		mapSyncMach.setRfdId(_subjectID[0]);
//		mapSyncMach.setCimName(_subjectID[1]);
//
//		profile_DY.clearAttributes();
//		
//		return mapSyncMach;
//	}
//	/**
//	 * 
//	 * @param key
//	 * @param _source
//	 * @param _subjectID
//	 * @return
//	 */
//	public GENSALMap create_GENSALModelicaMap(Resource key, String _source, String[] _subjectID)
//	{
//		GENSALMap mapSyncMach= gensalXMLToObject(_source);
//		Map<String, Object> cimClassMap= profile_EQ.retrieveAttributesSyncMach(key);
//		Iterator<AttributeMap> imapAttList= mapSyncMach.getAttributeMap().iterator();
//		AttributeMap currentmapAtt;
//		while (imapAttList.hasNext()) {
//			currentmapAtt= imapAttList.next();
//			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//		}
//		mapSyncMach.setName("GENSAL");
//		mapSyncMach.setRfdId(_subjectID[0]);
//		mapSyncMach.setCimName(_subjectID[1]);
//
//		modelCIM.clearAttributes();
//		
//		return mapSyncMach;
//	}
//	/**
//	 * 
//	 * @param key
//	 * @param _source
//	 * @param _subjectID
//	 * @return
//	 */
//	public GENROEMap create_GENROEModelicaMap(Resource key, String _source, String[] _subjectID)
//	{
//		GENROEMap mapSyncMach= genroeXMLToObject(_source);
//		Map<String, Object> cimClassMap= modelCIM.retrieveAttributesSyncMach(key);
//		Iterator<AttributeMap> imapAttList= mapSyncMach.getAttributeMap().iterator();
//		AttributeMap currentmapAtt;
//		while (imapAttList.hasNext()) {
//			currentmapAtt= imapAttList.next();
//			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//		}
//		mapSyncMach.setName("GENROE");
//		mapSyncMach.setRfdId(_subjectID[0]);
//		mapSyncMach.setCimName(_subjectID[1]);
//
//		modelCIM.clearAttributes();
//		
//		return mapSyncMach;
//	}
	
//	public Entry<String, Resource> typeOfExcitationSystem(Resource key)
//	{
//		Entry<String, Resource> excsData= profile_DY.checkExcitationSystemType(key);
//		
//		return excsData;
//	}
//	private static ESDC1AMap esdc1aXMLToObject(String _xmlmap) {
//		JAXBContext context;
//		Unmarshaller un;
//		
//		try{
//			context = JAXBContext.newInstance(ESDC1AMap.class);
//	        un = context.createUnmarshaller();
//	        ESDC1AMap map = (ESDC1AMap) un.unmarshal(new File(_xmlmap));
//	        return map;
//        } 
//        catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//	/**
//	 * 
//	 * @param key
//	 * @param _source
//	 * @param _subjectID
//	 * @return
//	 */
//	public ESDC1AMap create_ESDC1ModelicaMap(Resource _key, String _source, String _subjectName)
//	{
//		ESDC1AMap mapExcSys= esdc1aXMLToObject(_source);
//		Map<String, Object> cimClassMap= profile_DY.retrieveAttributesExcSys(_key);
//		Iterator<AttributeMap> imapAttList= mapExcSys.getAttributeMap().iterator();
//		AttributeMap currentmapAtt;
//		while (imapAttList.hasNext()) {
//			currentmapAtt= imapAttList.next();
//			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//		}
//		String[] dynamicResource= profile_DY.retrieveComponentName(_key);
//		mapExcSys.setRfdId(dynamicResource[0]);
//		mapExcSys.setCimName(dynamicResource[1]);
//
//		profile_DY.clearAttributes();
//		
//		return mapExcSys;
//	}
	
	private static LoadMap loadXMLToObject(String _xmlmap) {
		JAXBContext context;
		Unmarshaller un;
		
		try{
			context = JAXBContext.newInstance(LoadMap.class);
	        un = context.createUnmarshaller();
	        LoadMap map = (LoadMap) un.unmarshal(new File(_xmlmap));
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
	public LoadMap create_LoadModelicaMap(Resource key, String _source, String[] _subjectID)
	{
		LoadMap mapEnergyC= loadXMLToObject(_source);
		Map<String, Object> cimClassMap= profile_EQ.retrieveAttributesEnergyC(key);
		Iterator<AttributeMap> imapAttList= mapEnergyC.getAttributeMap().iterator();
		AttributeMap currentmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
		}
//		mapEnergyC.setLoadResponse(cimClassMap.get("EnergyConsumer.LoadResponse").toString());
		// add cim id, used as reference from terminal and connections to other components 
		mapEnergyC.setRfdId(_subjectID[0]);
		mapEnergyC.setCimName(_subjectID[1]);

		profile_EQ.clearAttributes();
		
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
		Map<String, Object> cimClassMap= profile_EQ.retrieveAttributes(key);
		Iterator<AttributeMap> imapAttList= mapACLine.getAttributeMap().iterator();
		AttributeMap currentmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
		}
		mapACLine.setRfdId(_subjectID[0]);
		mapACLine.setCimName(_subjectID[1]);

		profile_EQ.clearAttributes();
		
		return mapACLine;
	}
	
	private static TwoWindingTransformerMap twtXMLToObject(String _xmlmap) {
		JAXBContext context;
		Unmarshaller un;
		
		try{
			context = JAXBContext.newInstance(TwoWindingTransformerMap.class);
	        un = context.createUnmarshaller();
	        TwoWindingTransformerMap map = (TwoWindingTransformerMap) un.unmarshal(new File(_xmlmap));
	        return map;
        } 
        catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
	public CIMTransformerEnd create_TransformerModelicaMap(Resource key, String _source, String[] _subjectID)
	{
		TwoWindingTransformerMap mapPowTrans= twtXMLToObject(_source);
		CIMTransformerEnd transformerEnd;
		
		Map<String, Object> cimClassMap= profile_EQ.retrieveAttributesTransformer(key);
		Iterator<AttributeMap> imapAttList= mapPowTrans.getAttributeMap().iterator();
		AttributeMap currentmapAtt;
		while (imapAttList.hasNext()) { //get the values of the attributes
			currentmapAtt= imapAttList.next();
			if (cimClassMap.get(currentmapAtt.getCimName())!= null)
				currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//			System.out.println("currentmapatt: "+ currentmapAtt.getCimName()+ "= "+ currentmapAtt.getContent()+ "; "+ currentmapAtt.getMoName());
		}
//		mapPowTrans.setPowerTransformer(cimClassMap.get("TransformerEnd.RatioTapChanger").toString());
		mapPowTrans.setTerminal(cimClassMap.get("TransformerEnd.Terminal").toString());
//		System.out.println("TwT terminal: "+ mapPowTrans.getTerminal());
		// add cim id, used as reference from terminal and connections to other components 
		mapPowTrans.setRfdId(cimClassMap.get("PowerTransformerEnd.PowerTransformer").toString().split("#")[1]);
		mapPowTrans.setCimName(_subjectID[1]);
		
		transformerEnd= new CIMTransformerEnd(mapPowTrans, 
				(Resource)cimClassMap.get("PowerTransformerEnd.PowerTransformer"),
//				(Resource)cimClassMap.get("TransformerEnd.RatioTapChanger")
				(Resource)cimClassMap.get("TransformerEnd.Terminal"));
		transformerEnd.set_Pt_id(cimClassMap.get("PowerTransformerEnd.PowerTransformer").toString().split("#")[1]);
//		transformerEnd.set_Rtc_id(cimClassMap.get("TransformerEnd.RatioTapChanger").toString().split("#")[1]);
		transformerEnd.set_Te_id(cimClassMap.get("TransformerEnd.Terminal").toString().split("#")[1]);

		profile_EQ.clearAttributes();
		
		return transformerEnd;
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
		Map<String, Object> cimClassMap= profile_TP.retrieveAttributesTopoNode(key);
		Iterator<AttributeMap> imapAttList= mapTopoNode.getAttributeMap().iterator();
		AttributeMap currentmapAtt;
		while (imapAttList.hasNext()) {
			currentmapAtt= imapAttList.next();
			currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
		}
		mapTopoNode.setRfdId(_subjectID[0]);
		mapTopoNode.setCimName(_subjectID[1]);

		profile_TP.clearAttributes();
		
		return mapTopoNode;
	}

//	private static PwFaultMap pwfaultXMLToObject(String _xmlmap) {
//		JAXBContext context;
//		Unmarshaller un;
//		
//		try{
//			context = JAXBContext.newInstance(PwFaultMap.class);
//	        un = context.createUnmarshaller();
//	        PwFaultMap map = (PwFaultMap) un.unmarshal(new File(_xmlmap));
//	        return map;
//        } 
//        catch (JAXBException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//	public PwFaultMap create_FaultModelicaMap(Resource key, String _source, String[] _subjectID) 
//	{
//		PwFaultMap mapFault= pwfaultXMLToObject(_source);
//		Map<String, Object> cimClassMap= modelCIM.retrieveAttributesFault(key);
//		Iterator<AttributeMap> imapAttList= mapFault.getAttributeMap().iterator();
//		AttributeMap currentmapAtt;
//		while (imapAttList.hasNext()) {
//			currentmapAtt= imapAttList.next();
//			// condition to process attributes from ipsl not present in CIM
//			if (!currentmapAtt.getCimName().equals("none"))
//				currentmapAtt.setContent((String)cimClassMap.get(currentmapAtt.getCimName()));
//		}
//		mapFault.setRfdId(_subjectID[0]);
//		mapFault.setCimName(_subjectID[1]);

		// create the connection here
//		this.connections.add(new ConnectionMap(mapTerminal.getRfdId(),
//				cimClassMap.get("Terminal.ConductingEquipment").toString().split("#")[1],
//				cimClassMap.get("Terminal.TopologicalNode").toString().split("#")[1]));
//		
//		modelCIM.clearAttributes();
//		
//		return mapFault;
//	}
}
