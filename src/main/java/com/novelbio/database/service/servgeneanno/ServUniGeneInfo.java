package com.novelbio.database.service.servgeneanno;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.mapper.geneanno.MapUniGeneInfo;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.service.AbsGetSpring;

@Service
public class ServUniGeneInfo extends AbsGetSpring implements MapUniGeneInfo{
	@Inject
	private MapUniGeneInfo mapUniGeneInfo;
	
	public ServUniGeneInfo()
	{
		mapUniGeneInfo = (MapUniGeneInfo) factory.getBean("mapUniGeneInfo");
	}
	
	@Override
	public UniGeneInfo queryUniGeneInfo(UniGeneInfo uniGeneInfo) {
		return mapUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}
	public UniGeneInfo queryUniGeneInfo(String uniGenID) {
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setGeneUniID(uniGenID);
		return mapUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}
	@Override
	public void insertUniGeneInfo(UniGeneInfo uniGeneInfo) {
		// TODO Auto-generated method stub
		mapUniGeneInfo.insertUniGeneInfo(uniGeneInfo);
	}

	@Override
	public void updateUniGeneInfo(UniGeneInfo uniGeneInfo) {
		// TODO Auto-generated method stub
		mapUniGeneInfo.updateUniGeneInfo(uniGeneInfo);
	}
	/**
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param gene2Go
	 */
	public void updateUniGenInfo(String genUniID, AGeneInfo geneInfo)
	{
		UniGeneInfo uniGeneInfoOld = queryUniGeneInfo(genUniID);
		if (uniGeneInfoOld != null) {
			if (uniGeneInfoOld.addInfo(geneInfo)) {
				////////////
				uniGeneInfoOld.copeyInfo(geneInfo);
				uniGeneInfoOld.setGeneUniID(genUniID);
				//////////////
				updateUniGeneInfo(uniGeneInfoOld);
			}
		}
		else {
			UniGeneInfo uniGeneInfo = new UniGeneInfo();
			uniGeneInfo.copeyInfo(geneInfo);
			uniGeneInfo.setGeneUniID(genUniID);
			insertUniGeneInfo(uniGeneInfo);
		}
	}
}
