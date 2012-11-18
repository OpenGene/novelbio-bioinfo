package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.service.SpringFactory;

@Service
public class ServUniProtID implements MapUniProtID{
	Logger logger = Logger.getLogger(ServUniProtID.class);
	@Inject
	MapUniProtID mapUniProtID;
	
	public ServUniProtID() {
		mapUniProtID = (MapUniProtID)SpringFactory.getFactory().getBean("mapUniProtID");
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
	 * ������ָ�������ݿ����UniProtID��
	 * ����ҵ��˾ͷ����ҵ��ĵ�һ����uniprotID����
	 * ���û�ҵ�����ȥ��dbinfo���ң������û�ҵ����ͷ���Null
	 * @param geneID
	 * @param taxID
	 * @param dbInfo
	 * @return
	 */
	public UniProtID queryGenUniID(String uniID, int taxID, String dbInfo) {
		if (dbInfo != null) 
			dbInfo = dbInfo.trim();
		else 
			dbInfo = "";
		
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(uniID); uniProtID.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			uniProtID.setDBInfo(dbInfo.trim());
		}
		ArrayList<UniProtID> lsUniProtIDs= queryLsUniProtID(uniProtID);
		//��������ݿ��û�ҵ������������ݿ�
		if (!dbInfo.equals("") && (lsUniProtIDs == null || lsUniProtIDs.size() < 1) ) {
			uniProtID.setDBInfo("");
			lsUniProtIDs = queryLsUniProtID(uniProtID);
		}
		if ((lsUniProtIDs == null || lsUniProtIDs.size() < 1)) {
			return null;
		}
		else {
			return lsUniProtIDs.get(0);
		}
	}
	
	/**
	 * û�и�ID�Ͳ��룬�и�ID�Ļ��������Ҫoverride�����override�����ݿⲻһ�����͸�������<br>
	 * ע������ֻ�Ƚϵ�һ��dbInfo
	 * @param nCBIID
	 * @param override
	 */
	public boolean updateUniProtID(UniProtID uniProtID, boolean override) {
		if (uniProtID.getAccID().length() > 30) {
			logger.error("accID̫����" + uniProtID.getAccID() + "\t" + uniProtID.getDBInfo());
			if (uniProtID.getAccID().contains("GO:")) {
				logger.error("stop");
			}
			return false;
		}
		
		String db = uniProtID.getDBInfo();
		//��ѯ��ʱ��Ϊ�˷�ֹ�鲻�����ȳ�ȥdbinfo����Ϣ
		uniProtID.setDBInfo("");
		if (uniProtID.getAccID() == null) {
			logger.error("accID�����ڣ���������");
			return false;
		}
		ArrayList<UniProtID> lsResult = mapUniProtID.queryLsUniProtID(uniProtID);
		if (lsResult == null || lsResult.size() == 0) {
			uniProtID.setDBInfo(db);
			try {
				mapUniProtID.insertUniProtID(uniProtID);
				return true;
			} catch (Exception e) {
				logger.error("cannot insert into database: " + uniProtID.getAccID());
				e.printStackTrace();
				return false;
			}
		}
		else {
			if (override && !lsResult.get(0).getDBInfo().equals(db)) {
				uniProtID.setDBInfo(db);
				try {
					mapUniProtID.updateUniProtID(uniProtID);
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
