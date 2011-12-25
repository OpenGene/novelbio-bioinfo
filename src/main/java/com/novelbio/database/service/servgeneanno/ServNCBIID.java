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
	//TODO 正规写法
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
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
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
	 * 如果存在则返回第一个找到的geneID
	 * 不存在就返回null
	 * @param geneID 输入geneID
	 * @param taxID 物种ID
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
