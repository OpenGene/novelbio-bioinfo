package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * ��Microarray�뱾��������blast�Ľ���������ݿ�
 * <b>ע��Ҫ�趨�ȶԵ�����AccID����geneID��ͬʱҲ�������趨оƬ��ԴsetDbInfo</b>
 * @author zong0jie
 *
 */
public class MicroArrayBlast {
	double evalue = 1e-90;
	double identity = 90;
	String dbInfo = "";
	int taxID = 0;
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * �趨evalue��ֵ��ֻ�е�evalueС�ڵ��ڸ���ֵʱ�Żᵼ�����ݿ�
	 * @param evalue
	 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/**
	 * �趨identity��ֵ��ֻ�е�identity���ڵ��ڸ���ֵʱ�Żᵼ�����ݿ�
	 * @param evalue
	 */
	public void setIdentity(double identity) {
		this.identity = identity;
	}
	/**
	 * �趨���ݿ⣬ָ����Դ���ĸ�оƬ
	 * @param dbInfo
	 */
	public void setDbInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	
	String geneIDType = CopedID.IDTYPE_ACCID;
	/**
	 * blast����ID��accID����geneID����UniID
	 * @param blastID
	 */
	public void setGeneID(String geneIDType) {
		this.geneIDType = geneIDType;
	}
	
	/**
	 * ��ָ�����ļ��������ݿ⣬������ÿһ�ж��ܵ�������ı�
	 * �����Ҫ������У�Ʃ��amiGO����Ϣ���븲�Ǹ÷���
	 */
	public void updateFile(String gene2AccFile, boolean gzip) {
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		else 
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);

		ArrayList<String[]> lsInfo = txtGene2Acc.ExcelRead("\t", 1, 1, -1, -1, 1);
		//�Ÿ��򣬰���evalue��identity����
		Collections.sort(lsInfo, new Comparator<String[]>() {
			/**
			 * 0: queryID
			 * 1: blastID
			 * 2: identity
			 * 10: evalue
			 */
			@Override
			public int compare(String[] o1, String[] o2) {
				Double evalue1 = Double.parseDouble(o1[10]);
				Double identity1 = Double.parseDouble(o1[2]);
				Double evalue2 = Double.parseDouble(o2[10]);
				Double identity2 = Double.parseDouble(o2[2]);
				//evalueԽСԽ��
				int result = evalue1.compareTo(evalue2);
				if (result != 0)
					return result;
				//identityԽ��Խ��
				return -identity1.compareTo(identity2);
			}
		});
		//��������lsInfoȥ�ظ�
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		HashSet<String> hashID = new HashSet<String>();
		for (String[] strings : lsInfo) {
			if (hashID.contains(strings[0])) {
				continue;
			}
			lsFinal.add(strings);
		}
		//��ȥ�ظ�������ıȶԽ���������ݿ�
		for (String[] strings : lsFinal) {
			if (Double.parseDouble(strings[2]) < 90 || Double.parseDouble(strings[10]) > 1e-90) {
				continue;
			}
			
			CopedID copedID = new CopedID(strings[0], taxID);

			//������ݿ���û�����ID����ô�͵������ݿ�
			if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
				if (!geneIDType.equals(CopedID.IDTYPE_ACCID)) {
					copedID.setUpdateGeneID(strings[1], geneIDType);
				}
				else {
					copedID.setUpdateRefAccID(strings[1]);
				}
			}
			///////////////////////////���ݿ����Ѿ������ID�ˣ���ô���ȿ��Ƿ���ͬһ��ID��������ǵĻ�����evalue�Ƿ�Ϊ0��identity�Ƿ����99���ǵĻ��͸��ǵ�////////////////////////
			//////////////////////   �д�ʵ��   //////////////////////////////////////////////////////////////////////////////////
			else {
				//TODO
//				Double evalue = Double.parseDouble(strings[10]);
//				Double identity = Double.parseDouble(strings[2]);
//				//���Ǻ����ƾ�ȥ��
//				if (evalue > 1e-200 || identity < 99) {
//					return;
//				}
//				
//				CopedID copedID2 = null; String genUniID = "";
//				if (geneIDType.equals(CopedID.IDTYPE_ACCID)) {
//					copedID2 = new CopedID(strings[1], taxID);
//					genUniID = copedID2.getGenUniID();
//				}
//				if (copedID.getGenUniID().equals(genUniID)) {
//					return;
//				}
//				copedID.setUpdateAccID(accID);
			}
			copedID.setUpdateDBinfo(dbInfo, true);
			copedID.update(false);
		}
		
		
	}
	

}
