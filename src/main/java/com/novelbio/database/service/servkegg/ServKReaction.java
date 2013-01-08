package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.kegg.KGreaction;
import com.novelbio.database.mapper.kegg.MapKReaction;
import com.novelbio.database.service.SpringFactory;

public class ServKReaction implements MapKReaction{
	@Inject
	MapKReaction mapKReaction;
	
	public ServKReaction() {
		// TODO Auto-generated constructor stub
		mapKReaction = (MapKReaction)SpringFactory.getFactory().getBean("mapKReaction");
	}

	@Override
	public ArrayList<KGreaction> queryLsKGreactions(KGreaction kGreaction) {
		// TODO Auto-generated method stub
		return mapKReaction.queryLsKGreactions(kGreaction);
	}

	@Override
	public KGreaction queryKGreaction(KGreaction kGreaction) {
		// TODO Auto-generated method stub
		return mapKReaction.queryKGreaction(kGreaction);
	}

	@Override
	public void insertKGreaction(KGreaction kGreaction) {
		// TODO Auto-generated method stub
		mapKReaction.insertKGreaction(kGreaction);
	}

	@Override
	public void updateKGreaction(KGreaction kGreaction) {
		// TODO Auto-generated method stub
		mapKReaction.updateKGreaction(kGreaction);
	}
	
}
