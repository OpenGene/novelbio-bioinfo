package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServNCBIID extends AbsGetSpring implements MapNCBIID{
	@Inject
	private MapNCBIID mapNCBIID;

	
//	static{
//		mapNCBIID = (MapNCBIID) factory.getBean("mapNCBIID");
//	}
	public ServNCBIID()  
	{
		mapNCBIID = (MapNCBIID) factory.getBean("mapNCBIID");
	}
	//TODO ����д��
//	@Inject
//	protected MapNCBIID mapNCBIID;
//	private static ServGeneAnno info; 
//	@PostConstruct
//	public void init()
//	{
//	    info = this;
//	    info.mapNCBIID = this.mapNCBIID;
//	}
	@Override
	public NCBIID queryNCBIID(NCBIID QueryNCBIID) {
		return mapNCBIID.queryNCBIID(QueryNCBIID);
	}
	@Override
	public ArrayList<NCBIID> queryLsNCBIID(NCBIID QueryNCBIID) {
		return mapNCBIID.queryLsNCBIID(QueryNCBIID);
	}
	@Override
	public void insertNCBIID(NCBIID nCBIID) {
		mapNCBIID.insertNCBIID(nCBIID);
		
	}
	@Override
	public void updateNCBIID(NCBIID nCBIID) {
		mapNCBIID.updateNCBIID(nCBIID);
		
	}
	/**
	 * û�и�ID�Ͳ��룬�и�ID�Ļ��������Ҫoverride�����override�����ݿⲻһ�����͸�������
	 * @param nCBIID
	 * @param override
	 */
	public void updateNCBIID(NCBIID ncbiid, boolean override) {
		ArrayList<NCBIID> lsResult = mapNCBIID.queryLsNCBIID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			mapNCBIID.insertNCBIID(ncbiid);
		}
		else {
			if (override && !lsResult.get(0).getDBInfo().equals(ncbiid.getDBInfo())) {
				mapNCBIID.updateNCBIID(ncbiid);
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
	public NCBIID queryNCBIID(int geneID, int taxID) {
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);
		ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsResult = mapNCBIID.queryLsNCBIID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			return null;
		}
		else {
			return lsResult.get(0);
		}
		
	}
	
	
}
