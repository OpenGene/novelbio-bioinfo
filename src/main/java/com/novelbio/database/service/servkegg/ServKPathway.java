package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.mapper.kegg.MapKPathway;
import com.novelbio.database.service.SpringFactory;

public class ServKPathway implements MapKPathway {
	@Inject
	MapKPathway mapKPathway;
	public ServKPathway() {
		// TODO Auto-generated constructor stub
		mapKPathway = (MapKPathway)SpringFactory.getFactory().getBean("mapKPathway");
	}
	@Override
	public ArrayList<KGpathway> queryLsKGpathways(KGpathway kGpathway) {
		// TODO Auto-generated method stub
		return mapKPathway.queryLsKGpathways(kGpathway);
	}

	@Override
	public KGpathway queryKGpathway(KGpathway kGpathway) {
		// TODO Auto-generated method stub
		return mapKPathway.queryKGpathway(kGpathway);
	}

	public KGpathway queryKGpathway(String kGpathwayID) {
		// TODO Auto-generated method stub
		KGpathway kGpathway = new KGpathway();
		kGpathway.setPathName(kGpathwayID);
		return mapKPathway.queryKGpathway(kGpathway);
	}
	@Override
	public void insertKGpathway(KGpathway kGpathway) {
		// TODO Auto-generated method stub
		mapKPathway.insertKGpathway(kGpathway);
	}

	@Override
	public void updateKGpathway(KGpathway kGpathway) {
		// TODO Auto-generated method stub
		mapKPathway.updateKGpathway(kGpathway);
	}

}
