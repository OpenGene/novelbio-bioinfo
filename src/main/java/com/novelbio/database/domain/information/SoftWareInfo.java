package com.novelbio.database.domain.information;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.database.service.servinformation.ServSoftWareInfo;

/**
 * ������Ϣ������Լ�ִ��·��
 * @author zong0jie
 */
public class SoftWareInfo {
	public static void main(String[] args) {
//		SoftWareInfo.updateInfo("/home/zong0jie/����/SoftwareInfo_english.txt");
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName("picard");
		System.out.println(softWareInfo.getDescription());
	}
	private String softName;
	private String descrip;
	private String web = "";
	/** ����·���������ϵͳ·���Ļ���Ϊ "" */
	private String installPath;
	/** �����ļ��� */
	private String locationPath;
	private String use;
	private String ver;
	/** �Ƿ��ڻ���������:1��ʾtrue */
	int isPath = 1;
	/** �Ƿ��Ѿ����ҹ� */
	boolean searched = false;
	/** �����õ� */
	ServSoftWareInfo servSoftWareInfo = new ServSoftWareInfo();


	public SoftWareInfo() { }
	public void setName(String softName) {
		this.softName = softName;
		searched = false;
	}
	public String getName() {
		querySoftWareInfo();
		return softName;
	}
	public void setDescription(String description) {
		this.descrip = description;
	}
	public String getDescription() {
		querySoftWareInfo();
		return descrip;
	}
	public void setWebsite(String website) {
		this.web = website;
	}
	public String getWebsite() {
		querySoftWareInfo();
		return web;
	}
	public void setVersion(String version) {
		searched = false;
		this.ver = version;
	}
	public String getVersion() {
		querySoftWareInfo();
		return ver;
	}
	/**
	 * ʹ�������mapping��miRNAԤ��ȵ�
	 * @param usage
	 */
	public void setUsage(String usage) {
		searched = false;
		this.use = usage;
	}
	public String getUsage() {
		querySoftWareInfo();
		return use;
	}
	/** ����·���������ϵͳ·���Ļ���Ϊ "" */
	public void setInstallPath(String installPath) {
		this.installPath = installPath;
	}
	/** �����ļ��� */
	public void setPath(String path) {
		this.locationPath = path;
	}
	/** �����ļ��� */
	public String getPath() {
		querySoftWareInfo();
		return locationPath;
	}
	/** �Ƿ��ڻ��������� */
	public void setInPath(boolean isPath) {
		if (isPath)
			this.isPath = 1;
		else
			this.isPath = 0;
	}
	public boolean isInPath() {
		querySoftWareInfo();
		if (isPath == 1) {
			return true;
		}
		else
			return false;
	}
	public String getExePath() {
		querySoftWareInfo();
		if (isPath==1) {
			return "";
		}
		return installPath;
	}
	////////////////////////////////////////////////////��Ѫģ�� ///////////////////////////////////////////////////////////
	/**
	 * ��������Ϣ��û�оͲ��룬�о�����
	 */
	public void update() {
		servSoftWareInfo.update(this);
	}
	public ArrayList<SoftWareInfo> queryLsSoftWareInfo() {
		ArrayList<SoftWareInfo> lsSoftWareInfos = servSoftWareInfo.queryLsSoftWareInfo(this);
		return lsSoftWareInfos;
	}
	/**
	 * ����Ҫ�������������а汾��
	 */
	private void querySoftWareInfo() {
		if (searched) {
			return;
		}
		if (softName == null || softName.trim().equals("")) {
			return;
		}
		ArrayList<SoftWareInfo> lsSoftWareInfos = servSoftWareInfo.queryLsSoftWareInfo(this);
		if (lsSoftWareInfos == null || lsSoftWareInfos.size() == 0) {
			return;
		}
		if (lsSoftWareInfos.size() > 1) {
			//���ݰ汾�Ž�������
			Collections.sort(lsSoftWareInfos, new Comparator<SoftWareInfo>() {
				@Override
				public int compare(SoftWareInfo o1, SoftWareInfo o2) {
					return o1.getVersion().compareTo(o2.getVersion());
				}
			});
		}
		copyInfo(lsSoftWareInfos.get(lsSoftWareInfos.size() - 1));
		searched = true;
	}
	
	/** �����ݿ�����������Ϣȫ������������ */
	private void copyInfo(SoftWareInfo softWareInfo) {
		this.softName = softWareInfo.softName;
		this.descrip = softWareInfo.descrip;
		this.installPath = softWareInfo.installPath;
		this.locationPath = softWareInfo.locationPath;
		this.use = softWareInfo.use;
		this.ver = softWareInfo.ver;
		this.web = softWareInfo.web;
		this.isPath = softWareInfo.isPath;
	}
	/**
	 * ��ȱȽ�����
	 * @param obj
	 */
	public boolean equalsDeep(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		SoftWareInfo otherObj = (SoftWareInfo)obj;
		
		if (ArrayOperate.compareString(this.descrip, otherObj.descrip)
				&& ArrayOperate.compareString(this.installPath,otherObj.installPath)
				&& this.isPath == otherObj.isPath
				&& ArrayOperate.compareString(this.locationPath, otherObj.locationPath)
				&& ArrayOperate.compareString(this.softName, otherObj.softName)
				&& ArrayOperate.compareString(this.use, otherObj.use)
				&& ArrayOperate.compareString(this.ver, otherObj.ver)
				&& ArrayOperate.compareString(this.web, otherObj.web)
				)
		{
			return true;
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ��������Ϣ�������ݿ�
	 * @param txtFile 	 ������Ϣ����һ�У�item����
	 */
	public static void updateInfo(String txtFile) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(txtFile, 0);
		String[] title = lsInfo.get(0);
		HashMap<String, Integer> hashName2ColNum = new HashMap<String, Integer>();
		for (int i = 0; i < title.length; i++) {
			hashName2ColNum.put(title[i].trim().toLowerCase(), i);
		}
		
		for (int i = 1; i < lsInfo.size()-1; i++) {
			SoftWareInfo softWareInfo = new SoftWareInfo();
			String[] info = lsInfo.get(i);
			int m = hashName2ColNum.get("softwarename");
			softWareInfo.setName(info[m]);
			
			m = hashName2ColNum.get("description");
			softWareInfo.setDescription(info[m]);
			
			m = hashName2ColNum.get("website");
			softWareInfo.setWebsite(info[m]);
			
			m = hashName2ColNum.get("installpath");
			softWareInfo.setInstallPath(info[m]);
			
			m = hashName2ColNum.get("path");
			softWareInfo.setPath(info[m]);
			
			m = hashName2ColNum.get("usage");
			softWareInfo.setUsage(info[m]);
			
			m = hashName2ColNum.get("version");
			softWareInfo.setVersion(info[m]);
			
			m = hashName2ColNum.get("ispath");
			softWareInfo.setInPath(info[m].trim().toLowerCase().equals("true"));
			//����
			softWareInfo.update();
		}
	}
	public static enum SoftMapping {
		bwa, bowtie, bowtie2, tophat, rsem
	}

}
