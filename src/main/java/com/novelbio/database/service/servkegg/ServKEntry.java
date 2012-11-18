package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.mapper.kegg.MapKEntry;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServKEntry implements MapKEntry{
	@Inject
	MapKEntry mapKEntry;
	public ServKEntry() {
		mapKEntry = (MapKEntry)SpringFactory.getFactory().getBean("mapKEntry");
	}
	@Override
	public ArrayList<KGentry> queryLsKGentries(KGentry kGentry) {
		// TODO Auto-generated method stub
		return mapKEntry.queryLsKGentries(kGentry);
	}

	@Override
	public KGentry queryKGentry(KGentry kGentry) {
		// TODO Auto-generated method stub
		return mapKEntry.queryKGentry(kGentry);
	}

	@Override
	public void insertKGentry(KGentry kGentry) {
		// TODO Auto-generated method stub
		mapKEntry.insertKGentry(kGentry);
	}

	@Override
	public void updateKGentry(KGentry kGentry) {
		// TODO Auto-generated method stub
		mapKEntry.updateKGentry(kGentry);
	}

}
