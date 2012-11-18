package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;
import com.novelbio.database.mapper.kegg.MapKNIdKeg;
import com.novelbio.database.service.SpringFactory;

/**
 * 将不是基因的ID--也就是一些化合物等转换为KeggID
 * @author zong0jie
 *
 */
public class ServKNIdKeg implements MapKNIdKeg {
	@Inject
	MapKNIdKeg mapKNIdKeg;
	public ServKNIdKeg() {
		mapKNIdKeg = (MapKNIdKeg)SpringFactory.getFactory().getBean("mapKNIdKeg");
	}
	@Override
	public ArrayList<KGNIdKeg> queryLsKGNIdKeg(KGNIdKeg kgnIdKeg) {
		// TODO Auto-generated method stub
		return mapKNIdKeg.queryLsKGNIdKeg(kgnIdKeg);
	}
	@Override
	public KGNIdKeg queryKGNIdKeg(KGNIdKeg kgnIdKeg) {
		// TODO Auto-generated method stub
		return mapKNIdKeg.queryKGNIdKeg(kgnIdKeg);
	}
	@Override
	public void insertKGNIdKeg(KGNIdKeg kgnIdKeg) {
		// TODO Auto-generated method stub
		mapKNIdKeg.insertKGNIdKeg(kgnIdKeg);
	}
	@Override
	public void updateKGNIdKeg(KGNIdKeg kgnIdKeg) {
		// TODO Auto-generated method stub
		mapKNIdKeg.updateKGNIdKeg(kgnIdKeg);
	}
	
}
