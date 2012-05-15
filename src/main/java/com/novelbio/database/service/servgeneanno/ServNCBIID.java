package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServNCBIID extends AbsGetSpring implements MapNCBIID{
	private static Logger logger = Logger.getLogger(ServNCBIID.class);
	@Inject
	private MapNCBIID mapNCBIID;
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
	 * ������ָ�������ݿ����NCBIID��
	 * ����ҵ��˾ͷ����ҵ��ĵ�һ����ncbiid����
	 * ���û�ҵ�����ȥ��dbinfo���ң������û�ҵ����ͷ���Null
	 * @param geneID
	 * @param taxID
	 * @param dbInfo Ϊnull��ʾ������
	 * @return
	 */
	public NCBIID queryGenUniID(int geneID, int taxID, String dbInfo) {
		if (dbInfo != null) 
			dbInfo = dbInfo.trim();
		else 
			dbInfo = "";
		
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID); ncbiid.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			ncbiid.setDBInfo(dbInfo.trim());
		}
		ArrayList<NCBIID> lsNcbiids= queryLsNCBIID(ncbiid);
		//��������ݿ��û�ҵ������������ݿ�
		if (!dbInfo.equals("") && (lsNcbiids == null || lsNcbiids.size() < 1) ) {
			ncbiid.setDBInfo("");
			lsNcbiids= queryLsNCBIID(ncbiid);
		}
		if ((lsNcbiids == null || lsNcbiids.size() < 1)) {
			return null;
		}
		else {
			return lsNcbiids.get(0);
		}
	}
	
	/**
	 * <b>û��accID����������</b>
	 * û�и�ID�Ͳ��룬�и�ID�Ļ��������Ҫoverride�����override�����ݿⲻһ�����͸�������
	 * @param nCBIID
	 * @param override
	 */
	public boolean updateNCBIID(NCBIID ncbiid, boolean override) {
		String db = ncbiid.getDBInfo();
		//��ѯ��ʱ��Ϊ�˷�ֹ�鲻�����ȳ�ȥdbinfo����Ϣ
		ncbiid.setDBInfo("");
		if (ncbiid.getAccID() == null) {
			logger.error("accID�����ڣ���������");
			return false;
		}
		ArrayList<NCBIID> lsResult = mapNCBIID.queryLsNCBIID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			//�����ʱ���ټ���
			ncbiid.setDBInfo(db);
			try {
				mapNCBIID.insertNCBIID(ncbiid);
				return true;
			} catch (Exception e) {
				logger.error("cannot insert into database: " + ncbiid.getAccID());
				e.printStackTrace();
				return false;
			}
		}
		else {
			if (override && !lsResult.get(0).getDBInfo().equals(db)) {
				ncbiid.setDBInfo(db);
				try {
					mapNCBIID.updateNCBIID(ncbiid);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * ��������򷵻ص�һ���ҵ���geneID
	 * �����ھͷ���null
	 * @param geneID ����geneID
	 * @param taxID ����ID
	 * @return
	 */
	public NCBIID queryNCBIID(int geneID, int taxID) {
		if (geneID <= 0) {
			return null;
		}
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
