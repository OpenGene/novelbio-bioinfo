package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.mapper.kegg.MapKIDKeg2Ko;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServKIDKeg2Ko implements MapKIDKeg2Ko{
	@Inject
	MapKIDKeg2Ko mapKIDKeg2Ko;
	public ServKIDKeg2Ko() {
		mapKIDKeg2Ko = (MapKIDKeg2Ko)SpringFactory.getFactory().getBean("mapKIDKeg2Ko");
	}
	@Override
	public ArrayList<KGIDkeg2Ko> queryLsKGIDkeg2Ko(KGIDkeg2Ko kgiDkeg2Ko) {
		// TODO Auto-generated method stub
		return mapKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
	}
	@Override
	public KGIDkeg2Ko queryKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko) {
		// TODO Auto-generated method stub
		return mapKIDKeg2Ko.queryKGIDkeg2Ko(kGIDkeg2Ko);
	}
	@Override
	public void insertKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko) {
		// TODO Auto-generated method stub
		mapKIDKeg2Ko.insertKGIDkeg2Ko(kGIDkeg2Ko);
	}
	@Override
	public void updateKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko) {
		// TODO Auto-generated method stub
		mapKIDKeg2Ko.updateKGIDkeg2Ko(kGIDkeg2Ko);
	}
}
