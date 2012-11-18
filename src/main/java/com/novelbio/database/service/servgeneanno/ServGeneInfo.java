package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.mapper.geneanno.MapGeneInfo;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServGeneInfo implements MapGeneInfo {
	private static Logger logger = Logger.getLogger(ServGeneInfo.class);
	@Inject
	private MapGeneInfo mapGeneInfo;
	public ServGeneInfo() {
		mapGeneInfo = (MapGeneInfo)SpringFactory.getFactory().getBean("mapGeneInfo");
	}
	@Override
	public GeneInfo queryGeneInfo(GeneInfo geneInfo) {
		return mapGeneInfo.queryGeneInfo(geneInfo);
	}
	public GeneInfo queryGeneInfo(String genUniID, int taxID) {
		GeneInfo geneInfoQ = new GeneInfo();
		geneInfoQ.setGeneUniID(genUniID);
		geneInfoQ.setTaxID(taxID);
		return mapGeneInfo.queryGeneInfo(geneInfoQ);
	}
	@Override
	public ArrayList<GeneInfo> queryLsGeneInfo(GeneInfo geneInfo) {
		return mapGeneInfo.queryLsGeneInfo(geneInfo);
	}
	@Override
	public void insertGeneInfo(GeneInfo geneInfo) {
		mapGeneInfo.insertGeneInfo(geneInfo);
	}
	@Override
	public void updateGeneInfo(GeneInfo geneInfo) {
		mapGeneInfo.updateGeneInfo(geneInfo);
	}
	/**
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param gene2Go
	 */
	public void updateGenInfo(String genUniID, int taxID, AGeneInfo geneInfo) {
		if (taxID != 0 && geneInfo.getTaxID() != 0 && taxID != geneInfo.getTaxID()) {
			logger.error("输入taxID和自带taxID不一致,输入taxID："+taxID + " 自带taxID：" + geneInfo.getTaxID());
		}
		GeneInfo geneInfoOld = queryGeneInfo(genUniID, taxID);
		if (geneInfoOld != null) {
			if (geneInfoOld.addInfo(geneInfo)) {
				////////////
//				geneInfoOld.copeyInfo(geneInfo);
//				geneInfoOld.setGeneUniID(genUniID);
				//////////////
				updateGeneInfo(geneInfoOld);
			}
		}
		else {
			GeneInfo geneInfoNew = new GeneInfo();
			geneInfoNew.copeyInfo(geneInfo);
			geneInfoNew.setGeneUniID(genUniID);
			try {
				insertGeneInfo(geneInfoNew);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
