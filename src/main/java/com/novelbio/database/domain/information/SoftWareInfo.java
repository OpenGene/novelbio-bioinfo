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
 * 生物信息的软件以及执行路径
 * @author zong0jie
 */
public class SoftWareInfo {
	public static void main(String[] args) {
//		SoftWareInfo.updateInfo("/home/zong0jie/桌面/SoftwareInfo_english.txt");
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName("picard");
		System.out.println(softWareInfo.getDescription());
	}
	private String softName;
	private String descrip;
	private String web = "";
	/** 运行路径，如果是系统路径的话就为 "" */
	private String installPath;
	/** 所在文件夹 */
	private String locationPath;
	private String use;
	private String ver;
	/** 是否处于环境变量中:1表示true */
	int isPath = 1;
	/** 是否已经查找过 */
	boolean searched = false;
	/** 查找用的 */
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
	 * 使用类别，如mapping，miRNA预测等等
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
	/** 运行路径，如果是系统路径的话就为 "" */
	public void setInstallPath(String installPath) {
		this.installPath = installPath;
	}
	/** 所在文件夹 */
	public void setPath(String path) {
		this.locationPath = path;
	}
	/** 所在文件夹 */
	public String getPath() {
		querySoftWareInfo();
		return locationPath;
	}
	/** 是否处于环境变量中 */
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
	////////////////////////////////////////////////////充血模型 ///////////////////////////////////////////////////////////
	/**
	 * 升级本信息，没有就插入，有就升级
	 */
	public void update() {
		servSoftWareInfo.update(this);
	}
	public ArrayList<SoftWareInfo> queryLsSoftWareInfo() {
		ArrayList<SoftWareInfo> lsSoftWareInfos = servSoftWareInfo.queryLsSoftWareInfo(this);
		return lsSoftWareInfos;
	}
	/**
	 * 必须要有软件名，最好有版本号
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
			//根据版本号进行排序
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
	
	/** 将数据库搜索到的信息全部拷贝至本类 */
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
	 * 深度比较相似
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
	 * 将配置信息导入数据库
	 * @param txtFile 	 配置信息：第一行，item名称
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
			//升级
			softWareInfo.update();
		}
	}
	public static enum SoftMapping {
		bwa, bowtie, bowtie2, tophat, rsem
	}

}
