package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.mapper.geneanno.MapGeneInfo;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServGeneInfo extends AbsGetSpring implements MapGeneInfo {
	@Inject
	private MapGeneInfo mapGeneInfo;
	public ServGeneInfo()  
	{
		mapGeneInfo = (MapGeneInfo) factory.getBean("mapGeneInfo");
	}
	@Override
	public GeneInfo queryGeneInfo(GeneInfo geneInfo) {
		return mapGeneInfo.queryGeneInfo(geneInfo);
	}
	public GeneInfo queryGeneInfo(String genUniID) {
		GeneInfo geneInfoQ = new GeneInfo();
		geneInfoQ.setGeneUniID(genUniID);
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
	 * ����geneUniID�Լ���������ݣ����Ƿ���Ҫ����
	 * �ܲ���Ͳ��룬�Ѿ����˾��ж������ݿ����Ƿ�һ�£���һ�¾�����
	 * @param genUniID
	 * @param gene2Go
	 */
	public void updateGenInfo(String genUniID, AGeneInfo geneInfo)
	{
		GeneInfo geneInfoOld = queryGeneInfo(genUniID);
		if (geneInfoOld != null) {
			if (geneInfoOld.addInfo(geneInfo)) {
				updateGeneInfo(geneInfoOld);
			}
		}
		else {
			GeneInfo geneInfoNew = new GeneInfo();
			geneInfoNew.copeyInfo(geneInfo);
			geneInfoNew.setGeneUniID(genUniID);
			insertGeneInfo(geneInfoNew);
		}
	}
}
