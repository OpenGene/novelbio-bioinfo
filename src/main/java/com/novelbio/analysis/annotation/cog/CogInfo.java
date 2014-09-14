package com.novelbio.analysis.annotation.cog;

import java.util.ArrayList;
import java.util.List;

public class CogInfo {
	String cogId;
	String cogSeqName;
	double evalue;
	/** cog的单字母缩写 */
	String cogAbbr;
	/** cog具体的注释，跟最泛泛的注释还不一样 */
	String cogAnnoDetail;
	public CogInfo() {}
	public CogInfo(String cogLine) {
		String[] ss = cogLine.split("\t");
		setCogSeqName(ss[1]);
		setEvalue(Double.parseDouble(ss[2]));
		setCogId(ss[3]);
		setCogAbbr(ss[4]);
		setCogAnnoDetail(ss[5]);
	}
	public void setCogSeqName(String cogSeqName) {
		this.cogSeqName = cogSeqName;
	}
	public void setCogAbbr(String cogAbbr) {
		this.cogAbbr = cogAbbr;
	}
	public void setCogAnnoDetail(String cogAnnoDetail) {
		this.cogAnnoDetail = cogAnnoDetail;
	}
	public void setCogId(String cogId) {
		this.cogId = cogId;
	}
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/** Cog的单字母缩写 */
	public String getCogAbbr() {
		return cogAbbr;
	}
	/** cogId */
	public String getCogId() {
		return cogId;
	}
	/** cog的具体注释，一个cogid有一个注释 */
	public String getCogAnnoDetail() {
		return cogAnnoDetail;
	}
	/** 比对到该cog的evalue */
	public double getEvalue() {
		return evalue;
	}
	/** 获得该cogId所对应的蛋白 */
	public String getCogSeqName() {
		return cogSeqName;
	}
	
	public List<String> toLsArray() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(cogSeqName);
		lsResult.add(evalue + "");
		lsResult.add(cogId);
		lsResult.add(cogAbbr);
		lsResult.add(cogAnnoDetail);
		return lsResult;
	}
}
