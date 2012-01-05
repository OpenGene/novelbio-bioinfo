package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServBlastInfo extends AbsGetSpring implements MapBlastInfo {
	@Inject
	MapBlastInfo mapBlastInfo;
	
	public ServBlastInfo()
	{
		mapBlastInfo = (MapBlastInfo)factory.getBean("mapBlastInfo");
	}
	
	@Override
	public BlastInfo queryBlastInfo(BlastInfo qBlastInfo) {
		// TODO Auto-generated method stub
		return mapBlastInfo.queryBlastInfo(qBlastInfo);
	}

	@Override
	public ArrayList<BlastInfo> queryLsBlastInfo(BlastInfo qBlastInfo) {
		// TODO Auto-generated method stub
		return mapBlastInfo.queryLsBlastInfo(qBlastInfo);
	}

	@Override
	public void insertBlastInfo(BlastInfo blastInfo) {
		// TODO Auto-generated method stub
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
	public BlastInfo queryBlastInfo(String queryID, int taxID, double evalue)
	{
		BlastInfo blastInfoTmp = new BlastInfo();
		blastInfoTmp.setEvalue(evalue);
		blastInfoTmp.setQueryID(queryID);
		blastInfoTmp.setSubjectTax(taxID);
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
		BlastInfo blastInfo2 = queryBlastInfo(blastInfo.getQueryID(), blastInfo.getQueryTax(), -1);
		if (blastInfo2 == null) {
			mapBlastInfo.insertBlastInfo(blastInfo);
		}
		else if (blastInfo2.getEvalue() < blastInfo.getEvalue()) {
			mapBlastInfo.updateBlastInfo(blastInfo);
		}
	}
	
	
}
