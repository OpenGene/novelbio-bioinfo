package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.kegg.KGrelation;
import com.novelbio.database.mapper.kegg.MapKRelation;
import com.novelbio.database.service.SpringFactory;

public class ServKRelation implements MapKRelation{
	@Inject
	MapKRelation mapKRelation;
	public ServKRelation() {
		// TODO Auto-generated constructor stub
		mapKRelation = (MapKRelation)SpringFactory.getFactory().getBean("mapKRelation");
	}
	@Override
	public ArrayList<KGrelation> queryLsKGrelations(KGrelation kGrelation) {
		// TODO Auto-generated method stub
		return mapKRelation.queryLsKGrelations(kGrelation);
	}
	@Override
	public KGrelation queryKGrelation(KGrelation kGrelation) {
		// TODO Auto-generated method stub
		return mapKRelation.queryKGrelation(kGrelation);
	}
	@Override
	public void insertKGrelation(KGrelation kGrelation) {
		// TODO Auto-generated method stub
		mapKRelation.insertKGrelation(kGrelation);
	}
	@Override
	public void updateKGrelation(KGrelation kGrelation) {
		// TODO Auto-generated method stub
		mapKRelation.updateKGrelation(kGrelation);
	}
}
