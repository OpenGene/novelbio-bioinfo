package com.novelbio.database.domain.geneanno;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 记录blast信息是否为
 * @author zong0jie
 *
 */
@Document(collection = "blastfileinfo")
public class BlastFileInfo {
	@Id
	String id;
	@Indexed
	String fileName;
	@Indexed
	boolean isTmp;
	String userName;
	@Indexed
	int queryTaxID;
	@Indexed
	int subjectTaxID;
	
	/** 文件的日期 */
	String dateBlastFile;
	/** 导入日期 */
	String dateImport = DateUtil.getDateDetail();
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
		dateBlastFile = FileOperate.getTimeLastModifyStr(fileName);
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	/** 上传人 */
	public void setUserID(String userName) {
		this.userName = userName;
	}
	public void setTmp(boolean isTmp) {
		this.isTmp = isTmp;
	}
	
	public String getDateBlastFile() {
		return dateBlastFile;
	}
	
	public String getDateImport() {
		return dateImport;
	}
	public void setQueryTaxID(int queryTaxID) {
		this.queryTaxID = queryTaxID;
	}
	public void setSubjectTaxID(int subjectTaxID) {
		this.subjectTaxID = subjectTaxID;
	}
	
	public String getFileName() {
		return fileName;
	}
	public boolean isTmp() {
		return isTmp;
	}
	public int getQueryTaxID() {
		return queryTaxID;
	}
	public int getSubjectTaxID() {
		return subjectTaxID;
	}
}
