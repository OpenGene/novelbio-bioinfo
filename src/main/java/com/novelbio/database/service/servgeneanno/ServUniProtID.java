package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.service.AbsGetSpring;

@Service
public class ServUniProtID extends AbsGetSpring implements MapUniProtID{
	@Inject
	MapUniProtID mapUniProtID;
	
	public ServUniProtID() {
		mapUniProtID = (MapUniProtID) factory.getBean("mapUniProtID");
	}
	
	@Override
	public UniProtID queryUniProtID(UniProtID QueryUniProtID) {
		return mapUniProtID.queryUniProtID(QueryUniProtID);
	}

	@Override
	public ArrayList<UniProtID> queryLsUniProtID(UniProtID QueryUniProtID) {
		return mapUniProtID.queryLsUniProtID(QueryUniProtID);
	}

	@Override
	public void insertUniProtID(UniProtID UniProtID) {
		mapUniProtID.insertUniProtID(UniProtID);
		
	}

	@Override
	public void updateUniProtID(UniProtID UniProtID) {
		mapUniProtID.updateUniProtID(UniProtID);
		
	}
	/**
	 * û�и�ID�Ͳ��룬�и�ID�Ļ��������Ҫoverride�����override�����ݿⲻһ�����͸�������<br>
	 * ע������ֻ�Ƚϵ�һ��dbInfo
	 * @param nCBIID
	 * @param override
	 */
	public void updateUniProtID(UniProtID uniProtID, boolean override) {
		ArrayList<UniProtID> lsResult = mapUniProtID.queryLsUniProtID(uniProtID);
		if (lsResult == null || lsResult.size() == 0) {
			mapUniProtID.insertUniProtID(uniProtID);
		}
		else {
			if (override && !lsResult.get(0).getDBInfo().equals(uniProtID.getDBInfo())) {
				mapUniProtID.updateUniProtID(uniProtID);
			}
		}
	}
	/**
	 * ��������򷵻ص�һ���ҵ���geneID
	 * �����ھͷ���null
	 * @param geneID ����geneID
	 * @param taxID ����ID
	 * @return
	 */
	public UniProtID getUniProtID(String genUniID, int taxID) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setGenUniID(genUniID);
		uniProtID.setTaxID(taxID);
		List<UniProtID> lsUniIds = mapUniProtID.queryLsUniProtID(uniProtID);
		if (lsUniIds == null || lsUniIds.size() == 0) {
			return null;
		}
		else
			return lsUniIds.get(0);
	}

}
