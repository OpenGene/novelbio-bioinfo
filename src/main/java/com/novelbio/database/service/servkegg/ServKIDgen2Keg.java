package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.mapper.kegg.MapKIDgen2Keg;
import com.novelbio.database.service.SpringFactory;

/**
 * geneID到KeggID的转换表
 * @author zong0jie
 */
@Service
public class ServKIDgen2Keg implements MapKIDgen2Keg{
	@Inject
	MapKIDgen2Keg mapKIDgen2Keg;
	public ServKIDgen2Keg() {
		// TODO Auto-generated constructor stub
		mapKIDgen2Keg = (MapKIDgen2Keg)SpringFactory.getFactory().getBean("mapKIDgen2Keg");
	}
	
	@Override
	public ArrayList<KGIDgen2Keg> queryLsKGIDgen2Keg(KGIDgen2Keg kgIDgen2Keg) {
		// TODO Auto-generated method stub
		return mapKIDgen2Keg.queryLsKGIDgen2Keg(kgIDgen2Keg);
	}

	@Override
	public KGIDgen2Keg queryKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg) {
		// TODO Auto-generated method stub
		return mapKIDgen2Keg.queryKGIDgen2Keg(kGIDgen2Keg);
	}

	@Override
	public void insertKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg) {
		// TODO Auto-generated method stub
		mapKIDgen2Keg.insertKGIDgen2Keg(kGIDgen2Keg);
	}

	@Override
	public void updateKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg) {
		// TODO Auto-generated method stub
		mapKIDgen2Keg.updateKGIDgen2Keg(kGIDgen2Keg);
	}

}
