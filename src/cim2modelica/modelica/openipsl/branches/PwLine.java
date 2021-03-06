package cim2modelica.modelica.openipsl.branches;

import cim2modelica.modelica.MOAttribute;
import cim2modelica.modelica.MOClass;

public class PwLine extends MOClass 
{
	public PwLine(String _name) {
		super(_name);
		this.annotation = "annotation (Placement(transformation(extent={{34,26},{46,34}})))";
	}
	
	@Override
	public String to_ModelicaInstance()
	{
		String code= "";
		StringBuilder pencil= new StringBuilder();
		
		if (!this.visibility.equals("public"))
		{
			pencil.append(this.visibility); 
			pencil.append(" ");
		}
//		pencil.append(this.variability);
//		pencil.append(" ");
		pencil.append(this.pakage);
		pencil.append(".");
		pencil.append(this.name);
		pencil.append(" ");
		pencil.append(this.instanceName);
		pencil.append("(");
		for (MOAttribute item: this.attributes)
		{
			if (!item.get_Name().equals("length")){
				if (item.get_Variability().equals("parameter")){
					pencil.append(item.get_Name());
					pencil.append("=");
					pencil.append(item.get_Value());
					pencil.append(",");
				}
			}
		}
		pencil.deleteCharAt(pencil.lastIndexOf(","));
		pencil.append(") ");
		pencil.append('"');
		pencil.append(this.comment);
		pencil.append('"'); 
		pencil.append(" ");
		pencil.append(this.annotation);
		pencil.append(";\n");
		code= pencil.toString();
		
		code= pencil.toString();
		
		return code;
	}
}
