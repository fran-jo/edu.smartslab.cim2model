package cim2model.cim;

import cim2model.ipsl.cimmap.TwoWindingTransformerMap;

import org.apache.jena.rdf.model.Resource;

/**
 * Keeps the relationship of Terminal class with ConductingEquipment and TopologicalNode
 * @author fragom
 *
 */
public class CIMTransformerEnd 
{
	private TwoWindingTransformerMap twtmap;
	private Resource powerTransformer;
	private String pt_id;
	private Resource terminal;
	private String t_id;
	
	public CIMTransformerEnd(TwoWindingTransformerMap _twtmap, 
			Resource _powerTransformer,  
			Resource _terminal) {
		super();
		this.twtmap = _twtmap;
		this.powerTransformer = _powerTransformer;
		this.terminal = _terminal;
		this.pt_id= ""; 
		this.t_id= "";
	}
	
	/**
	 * @return the terminal
	 */
	public TwoWindingTransformerMap get_TransformerMap() {
		return twtmap;
	}
	/**
	 * @param terminal the terminal to set
	 */
	public void set_TransformerMap(TwoWindingTransformerMap _twtmap) {
		this.twtmap = _twtmap;
	}
	/**
	 * @return the conductingEquipment
	 */
	public Resource get_PowerTransformerMap() {
		return powerTransformer;
	}
	/**
	 * @param conductingEquipment the conductingEquipment to set
	 */
	public void set_PowerTransformerMap(Resource _powerTransformer) {
		this.powerTransformer = _powerTransformer;
	}
	
	/**
	 * @return the terminalEnd
	 */
	public Resource get_TerminalMap() {
		return terminal;
	}
	/**
	 * @param _terminalEnd the terminalEnd to set
	 */
	public void set_TerminalMap(Resource _terminal) {
		this.terminal = _terminal;
	}
	
	/**
	 * @return the ce_id
	 */
	public String get_Pt_id() {
		return pt_id;
	}
	/**
	 * @param ce_id the ce_id to set
	 */
	public void set_Pt_id(String _pt_id) {
		this.pt_id = _pt_id;
	}
	/**
	 * @return the te_id
	 */
	public String get_Te_id() {
		return t_id;
	}
	/**
	 * @param te_id the te_id to set
	 */
	public void set_Te_id(String _t_id) {
		this.t_id = _t_id;
	}
}
