package com.novelbio.database.service.servkegg;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.kegg.noGene.KGNCompInfo;
import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;
import com.novelbio.database.mapper.kegg.MapKNCompInfo;
import com.novelbio.database.service.SpringFactory;

/**
 * keggID化合物的具体信息
 * @author zong0jie
 *
 */
@Service
public class ServKNCompInfo implements MapKNCompInfo{
	@Inject
	MapKNCompInfo mapKNCompInfo;
	public ServKNCompInfo() {
		// TODO Auto-generated constructor stub
		mapKNCompInfo = (MapKNCompInfo)SpringFactory.getFactory().getBean("mapKNCompInfo");
	}
	@Override
	public ArrayList<KGNCompInfo> queryLsKGNCompInfo(KGNIdKeg kgnIdKeg) {
		// TODO Auto-generated method stub
		return mapKNCompInfo.queryLsKGNCompInfo(kgnIdKeg);
	}
	@Override
	public KGNCompInfo queryKGNCompInfo(KGNIdKeg kgnIdKeg) {
		// TODO Auto-generated method stub
		return mapKNCompInfo.queryKGNCompInfo(kgnIdKeg);
	}
	@Override
	public void insertKGNCompInfo(KGNCompInfo kgnCompInfo) {
		// TODO Auto-generated method stub
		mapKNCompInfo.insertKGNCompInfo(kgnCompInfo);
	}
	@Override
	public void updateKGNCompInfo(KGNCompInfo kgnCompInfo) {
		// TODO Auto-generated method stub\
		mapKNCompInfo.updateKGNCompInfo(kgnCompInfo);
	}
	
}
