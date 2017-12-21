within CIM16_N44.PowerPlant;
model GENSAL_HYGOV_c6859b9e "annotation ()"
	OpenIPSL.Interfaces.PwPin plantaPin"automatically generated comment" annotation ();
	OpenIPSL.Electrical.Machines.PSSE.GENSAL sm_6700_4 (S_b=1000,M_b=1200,V_b=300,V_0=306,angle_0=-2.1611,P_0=-520.25,Q_0=-173.342,R_a=0.000001,Xl=0.1474,H=3.592,D=0,S10=0.1,S12=0.3,Tpd0=5.24,Tppd0=0.05,Tppq0=0.15,Xd=1.1044,Xpd=0.2548,Xppd=0.1706,Xq=0.6619,Xppq=0.000001) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
	OpenIPSL.Electrical.Controls.PSSE.TG.HYGOV tgov_HYGOV (R=0.06,r=0.4,T_r=5,T_f=0.05,T_g=0.2,VELM=0.2,G_MAX=1,G_MIN=0,T_w=1,A_t=1.1,D_turb=0.5,q_NL=0.1) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
	Modelica.Blocks.Sources.Constant const (k=0) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
equation
	connect(sm_6700_4.EFD0, sm_6700_4.EFD);
	connect(sm_6700_4.SPEED, tgov_HYGOV.SPEED);
	connect(sm_6700_4.PMECH0, tgov_HYGOV.PMECH0);
	connect(sm_6700_4.PMECH, tgov_HYGOV.PMECH);
	connect(sm_6700_4.p, plantaPin);
end GENSAL_HYGOV_c6859b9e;