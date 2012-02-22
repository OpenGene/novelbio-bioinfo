package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.database.model.modcopeid.CopedID;
/**
 * ����affy��ע���ļ���������affy̽�룬����ע��ͨͨ������<br>
 * <b>������벻��ȥ�����ǽ�ss[8]����ɸѡ����</b><br>
 * ������ȡ����һ�е�ע��
 * <b>����֮ǰ��׼������</b><br>
 * 1. ����ʽ����Ϊtab������ȥ����������<br>
 * 2. �������޹�ID���Լ�control̽��ȫ��ȥ��
 * 3. �趨�ӵڶ��п�ʼ����
 * @author zong0jie
 *
 */
public class NormAffy extends ImportPerLine
{
	String dbInfo = "";
	/**
	 * �趨оƬ��Դ��
	 * ��NovelBioConst.DBINFO_ATH_TAIR��
	 * @param dbInfo
	 */
	public void setDbInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("#")) {
			return true;
		}
		String[] ss = lineContent.split("\t");
		if (ss[0].startsWith("Probe")) {
			return true;
		}
		CopedID copedID = new CopedID(ss[0], taxID);
		copedID.setUpdateDBinfo(dbInfo, true);
		if (!ss[18].equals("---")) {
			String[] ssGeneID = ss[18].split("///");
			copedID.setUpdateGeneID(ssGeneID[0].trim(), CopedID.IDTYPE_GENEID);
		}
		ArrayList<String> lsRefAccID = new ArrayList<String>();
//		addRefAccID(lsRefAccID, ss[8]);
		addRefAccID(lsRefAccID, ss[10]); addRefAccID(lsRefAccID, ss[14]); addRefAccID(lsRefAccID, ss[17]);
		addRefAccID(lsRefAccID, ss[19]); addRefAccID(lsRefAccID, ss[22]);
		addRefAccID(lsRefAccID, ss[23]); addRefAccID(lsRefAccID, ss[25]);
		copedID.setUpdateRefAccID(lsRefAccID);
		return copedID.update(false);
	}
	
	private void addRefAccID(ArrayList<String> lsRefAccID, String cellInfo) {
		if (cellInfo.equals("---")) {
			return;
		}
		else {
			String[] info = cellInfo.split("///");
			for (String string : info) {
				lsRefAccID.add(string);
			}
		}
	}
}