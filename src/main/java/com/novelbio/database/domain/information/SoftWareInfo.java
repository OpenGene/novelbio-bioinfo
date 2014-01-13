package com.novelbio.database.domain.information;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.service.servinformation.ManageSoftWareInfo;

/**
 * 生物信息的软件以及执行路径
 * @author zong0jie
 */
@Document(collection = "software")
public class SoftWareInfo {
	@Id
	String id;
	@Indexed
	private String softName;
	private String descrip;
	private String web = "";
	/** 运行路径，如果是系统路径的话就为 "" */
	private String installPath;
	/** 所在文件夹 */
	private String locationPath;
	private String usage;
	private String ver;
	/** 是否处于环境变量中:1表示true */
	int isPath = 1;
	
	/** 是否已经查找过 */
	@Transient
	boolean searched = false;
	@Transient
	static ManageSoftWareInfo manageSoftWareInfo = ManageSoftWareInfo.getInstance();
	
	public SoftWareInfo() { }
	public SoftWareInfo(SoftWare softName) { 
		setName(softName);
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	
	public void setName(String softName) {
		this.softName = softName.toLowerCase();
		searched = false;
	}
	public void setName(SoftWare softName) {
		this.softName = softName.toString().toLowerCase();
		searched = false;
	}
	public String getName() {
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
		this.usage = usage;
	}
	public String getUsage() {
		querySoftWareInfo();
		return usage;
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
		if (isPath) {
			this.isPath = 1;
		} else {
			this.isPath = 0;
		}
	}
	public boolean isInPath() {
		querySoftWareInfo();
		if (isPath == 1) {
			return true;
		}
		else
			return false;
	}
	/**
	 * 根据是否在系统路径，返回""或者locationPath，最后加上/
	 * @return
	 */
	public String getExePath() {
		querySoftWareInfo();
		if (isPath==1) {
			return "";
		}
		return locationPath;
	}
	public String getInstallPath() {
		querySoftWareInfo();
		return installPath;
	}
	////////////////////////////////////////////////////充血模型 ///////////////////////////////////////////////////////////
	/**
	 * 升级本信息，没有就插入，有就升级
	 */
	public void update() {
		manageSoftWareInfo.update(this);
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
		SoftWareInfo softWareInfos = manageSoftWareInfo.findSoftwareByName(softName);
		copyInfo(softWareInfos);
		searched = true;
	}
	
	/** 将数据库搜索到的信息全部拷贝至本类 */
	private void copyInfo(SoftWareInfo softWareInfo) {
		if (softWareInfo == null) {
			return;
		}
		this.softName = softWareInfo.softName;
		this.descrip = softWareInfo.descrip;
		this.installPath = softWareInfo.installPath;
		this.locationPath = softWareInfo.locationPath;
		this.usage = softWareInfo.usage;
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
				&& ArrayOperate.compareString(this.usage, otherObj.usage)
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
		manageSoftWareInfo.updateInfo(txtFile);
	}
	public static enum SoftWare {
		blast,
		bwa, bowtie, bowtie2, 
		tophat, rsem, mapsplice,
		miranada, RNAhybrid, mirDeep, miReap,
		samtools, picard, GATK, cufflinks,
		macs, sicer,
		emboss;
		
		static HashMap<String, SoftWare> mapStr2MapSoftware = new LinkedHashMap<String, SoftWareInfo.SoftWare>();
		public static HashMap<String, SoftWare> getMapStr2MappingSoftware() {
			if (mapStr2MapSoftware.size() == 0) {
				mapStr2MapSoftware.put("bwa", bwa);
				mapStr2MapSoftware.put("bowtie2", bowtie2);
			}
			return mapStr2MapSoftware;
		}
		
		
	}
}
