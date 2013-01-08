package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.kegg.KGpathRelation;
import com.novelbio.database.mapper.kegg.MapKPathRelation;
import com.novelbio.database.service.SpringFactory;

public class ServKPathRelation implements MapKPathRelation{
	@Inject
	MapKPathRelation mapKPathRelation;
	public ServKPathRelation() {
		mapKPathRelation = (MapKPathRelation)SpringFactory.getFactory().getBean("mapKPathRelation");
	}
	@Override
	public ArrayList<KGpathRelation> queryLskGpathRelations(
			KGpathRelation kGpathRelation) {
		// TODO Auto-generated method stub
		return mapKPathRelation.queryLskGpathRelations(kGpathRelation);
	}
	@Override
	public KGpathRelation queryKGpathRelation(KGpathRelation kGpathRelation) {
		// TODO Auto-generated method stub
		return mapKPathRelation.queryKGpathRelation(kGpathRelation);
	}
	@Override
	public void insertKGpathRelation(KGpathRelation kGpathRelation) {
		// TODO Auto-generated method stub
		mapKPathRelation.insertKGpathRelation(kGpathRelation);
	}
	@Override
	public void updateKGpathRelation(KGpathRelation kGpathRelation) {
		// TODO Auto-generated method stub
		mapKPathRelation.updateKGpathRelation(kGpathRelation);
	}
}
