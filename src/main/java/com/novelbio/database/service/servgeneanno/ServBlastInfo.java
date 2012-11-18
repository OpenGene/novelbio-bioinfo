package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServBlastInfo implements MapBlastInfo {
	@Inject
	MapBlastInfo mapBlastInfo;
	
	public ServBlastInfo() {
		mapBlastInfo = (MapBlastInfo)SpringFactory.getFactory().getBean("mapBlastInfo");
	}
	@Override
	public BlastInfo queryBlastInfo(BlastInfo qBlastInfo) {
		return mapBlastInfo.queryBlastInfo(qBlastInfo);
	}
	@Override
	public ArrayList<BlastInfo> queryLsBlastInfo(BlastInfo qBlastInfo) {
		return mapBlastInfo.queryLsBlastInfo(qBlastInfo);
	}
	@Override
	public void insertBlastInfo(BlastInfo blastInfo) {
		mapBlastInfo.insertBlastInfo(blastInfo);
	}
	@Override
	public void updateBlastInfo(BlastInfo blastInfo) {
		// TODO Auto-generated method stub
		mapBlastInfo.updateBlastInfo(blastInfo);
	}
	/**
	 * ���ҷ��������ĵ�һ��BlastInfo
	 * @param queryID ������ID��һ����genUniID
	 * @param taxID ����ID
	 * @param evalue ���evalue <= -1��evalue >=5����������
	 * @return
	 */
	public BlastInfo queryBlastInfo(String queryID, int taxIDQ, int taxIDS, double evalue) {
		BlastInfo blastInfoTmp = new BlastInfo();
		blastInfoTmp.setEvalue(evalue);
		blastInfoTmp.setQueryID(queryID);
		if (taxIDQ > 0) {
			blastInfoTmp.setQueryTax(taxIDQ);
		}
		if (taxIDS > 0) {
			blastInfoTmp.setSubjectTax(taxIDS);
		}
		ArrayList<BlastInfo> lsBlastInfosTmp = queryLsBlastInfo(blastInfoTmp);
		if (lsBlastInfosTmp != null && lsBlastInfosTmp.size() > 0) 
		{
			Collections.sort(lsBlastInfosTmp);//����ѡ����С��һ��
			BlastInfo blastInfo = lsBlastInfosTmp.get(0);
			if (evalue < 5 && evalue > -1 && blastInfo.getEvalue() <= evalue) {
				return blastInfo;
			}
		}
		return null;
	}
	/**
	 * ����blastInfo����Ϣ��������ݿ��еı������Ѿ����˸ý������Ƚ�evalue���õ�evalue�ĸ��Ǹ�evalue��
	 * ���û�У������
	 * @param blastInfo
	 */
	public void updateBlast(BlastInfo blastInfo) {
		BlastInfo blastInfo2 = queryBlastInfo(blastInfo.getQueryID(), blastInfo.getQueryTax(), blastInfo.getSubjectTax(), 1);
		if (blastInfo2 == null) {
			mapBlastInfo.insertBlastInfo(blastInfo);
		}
		//evalueС�ڵ���blastinfo ����evalue��ͬ����identity����
		else if (blastInfo2.getEvalue() < blastInfo.getEvalue()
				|| (blastInfo2.getEvalue() == blastInfo.getEvalue()
				    && blastInfo2.getIdentities() > blastInfo.getIdentities()  )) {
			mapBlastInfo.updateBlastInfo(blastInfo);
		}
	}
}
