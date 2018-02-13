within CIM16_Kundur_2Area.PowerPlant;
model GENROU_SEXS_8a6b544c "annotation ()"
	OpenIPSL.Interfaces.PwPin plantaPin"automatically generated comment" annotation ();
	OpenIPSL.Electrical.Machines.PSSE.GENROU sm_1_1 (S_b=100,M_b=900,V_b=20,V_0=1.03,angle_0=20.0702,P_0=700,Q_0=185.003,R_a=0.0025,Xl=0.2,H=6.5,D=0.02,S10=0.186,S12=0.802,Tpd0=8,Tpq0=0.4,Tppd0=0.03,Tppq0=0.05,Xd=1.8,Xpd=0.3,Xppd=0.25,Xq=1.7,Xpq=0.55,Xppq=0.25) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
	OpenIPSL.Electrical.Controls.PSSE.ES.SEXS es_SEXS (T_AT_B=1,T_B=1,K=200,T_E=0.01,E_MIN=0,E_MAX=4) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
	Modelica.Blocks.Sources.Constant const (k=0) "automatically generated comment" annotation (Placement(transformation(extent={{30,-40},{50,-20}})));
equation
	connect(sm_1_1.EFD0, es_SEXS.EFD0);
	connect(sm_1_1.ETERM, es_SEXS.ECOMP);
	connect(es_SEXS.EFD, sm_1_1.EFD);
	connect(sm_1_1.PMECH0, sm_1_1.PMECH);
	connect(sm_1_1.p, plantaPin);
	connect(const.y, es_SEXS.VOTHSG);
	connect(const.y, es_SEXS.VUEL);
	connect(const.y, es_SEXS.VOEL);
	connect(const.y, es_SEXS.XADIFD);
end GENROU_SEXS_8a6b544c;