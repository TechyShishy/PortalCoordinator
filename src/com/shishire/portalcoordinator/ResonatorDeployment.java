package com.shishire.portalcoordinator;

import android.util.Pair;

public class ResonatorDeployment extends Pair<String, String> {

	public ResonatorDeployment(String portal, String position) {
		super(portal, position);
	}
	
	public String getPortal()
	{
		return this.first;
	}
	
	public String getPosition()
	{
		return this.second;
	}
}
