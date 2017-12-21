within CIM16_N44.PowerPlant;
model GENSAL_HYGOV_c6859a59 "annotation ()"
	OpenIPSL.Interfaces.PwPin plantaPin"automatically generated comment" annotation ();
	OpenIPSL.Electrical.Machines.PSSE.GENSAL sm_3249_7 (S_b=1000,M_b=1357,V_b=420,V_0=420,angle_0=11.1366,P_0=-439.841,Q_0=-7.17,R_a=0.000001,Xl=0.11538,H=4.543,D=0,S10=0.10239,S12=0.2742,Tpd0=10.13,Tppd0=0.06,Tppq0=0.1,Xd=1.036,Xpd=0.28,Xppd=0.21,Xq=0.63,Xppq=0.000001) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
	OpenIPSL.Electrical.Controls.PSSE.TG.HYGOV tgov_HYGOV (R=0.06,r=0.4,T_r=5,T_f=0.05,T_g=0.2,VELM=0.1,G_MAX=1,G_MIN=0,T_w=1,A_t=1.1,D_turb=0.5,q_NL=0.1) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
	Modelica.Blocks.Sources.Constant const (k=0) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
equation
	connect(sm_3249_7.EFD0, sm_3249_7.EFD);
	connect(sm_3249_7.SPEED, tgov_HYGOV.SPEED);
	connect(sm_3249_7.PMECH0, tgov_HYGOV.PMECH0);
	connect(sm_3249_7.PMECH, tgov_HYGOV.PMECH);
	connect(sm_3249_7.p, plantaPin);
end GENSAL_HYGOV_c6859a59;