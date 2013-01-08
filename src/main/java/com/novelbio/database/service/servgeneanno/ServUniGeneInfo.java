package com.novelbio.database.service.servgeneanno;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.mapper.geneanno.MapUniGeneInfo;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.service.SpringFactory;

@Service
public class ServUniGeneInfo implements MapUniGeneInfo{
	private static Logger logger = Logger.getLogger(ServUniGeneInfo.class);
	@Inject
	private MapUniGeneInfo mapUniGeneInfo;
	
	public ServUniGeneInfo()
	{
		mapUniGeneInfo = (MapUniGeneInfo)SpringFactory.getFactory().getBean("mapUniGeneInfo");
	}
	
	@Override
	public UniGeneInfo queryUniGeneInfo(UniGeneInfo uniGeneInfo) {
		return mapUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
	}
	public UniGeneInfo queryUniGeneInfo(String uniGenID, int taxID) {
		UniGeneInfo uniGeneInfo = new UniGeneInfo();
		uniGeneInfo.setGeneUniID(uniGenID);
		uniGeneInfo.setTaxID(taxID);
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
	public void updateUniGenInfo(String genUniID, int taxID,AGeneInfo geneInfo)
	{
		if (taxID != 0 && geneInfo.getTaxID() != 0 && taxID != geneInfo.getTaxID()) {
			logger.error("输入taxID和自带taxID不一致,输入taxID："+taxID + " 自带taxID：" + geneInfo.getTaxID());
		}
		UniGeneInfo uniGeneInfoOld = queryUniGeneInfo(genUniID, taxID);
		if (uniGeneInfoOld != null) {
			if (uniGeneInfoOld.addInfo(geneInfo)) {
				////////////
//				uniGeneInfoOld.copeyInfo(geneInfo);
//				uniGeneInfoOld.setGeneUniID(genUniID);
				//////////////
				updateUniGeneInfo(uniGeneInfoOld);
			}
		}
		else {
			UniGeneInfo uniGeneInfo = new UniGeneInfo();
			uniGeneInfo.copeyInfo(geneInfo);
			uniGeneInfo.setGeneUniID(genUniID);
			try {
				insertUniGeneInfo(uniGeneInfo);
			} catch (Exception e) {
			e.printStackTrace();
			}
		}
	}
}
