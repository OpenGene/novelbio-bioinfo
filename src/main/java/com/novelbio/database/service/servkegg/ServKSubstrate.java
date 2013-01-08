package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.kegg.KGsubstrate;
import com.novelbio.database.mapper.kegg.MapKSubstrate;
import com.novelbio.database.service.SpringFactory;

public class ServKSubstrate implements MapKSubstrate{
	@Inject
	MapKSubstrate mapKSubstrate;
	public ServKSubstrate() {
		// TODO Auto-generated constructor stub
		mapKSubstrate = (MapKSubstrate)SpringFactory.getFactory().getBean("mapKSubstrate");
	}
	@Override
	public ArrayList<KGsubstrate> queryLskgKGsubstrates(KGsubstrate kGsubstrate) {
		// TODO Auto-generated method stub
		return mapKSubstrate.queryLskgKGsubstrates(kGsubstrate);
	}
	@Override
	public KGsubstrate queryKGsubstrate(KGsubstrate kGsubstrate) {
		// TODO Auto-generated method stub
		return mapKSubstrate.queryKGsubstrate(kGsubstrate);
	}
	@Override
	public void insertKGsubstrate(KGsubstrate kGsubstrate) {
		// TODO Auto-generated method stub
		mapKSubstrate.insertKGsubstrate(kGsubstrate);
	}
	@Override
	public void updateKGsubstrate(KGsubstrate kGsubstrate) {
		// TODO Auto-generated method stub
		mapKSubstrate.updateKGsubstrate(kGsubstrate);
	}
	
}
