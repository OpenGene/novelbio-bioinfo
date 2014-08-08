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
	/** cog注释的常规，就是 [A] 是 RNA processing and modification这种 */ 
	String cogAnno;
	/** cog的大类 */
	String cogAnnoBig;
	public CogInfo() {}
	public CogInfo(String cogLine) {
		String[] ss = cogLine.split("\t");
		setCogSeqName(ss[1]);
		setEvalue(Double.parseDouble(ss[2]));
		setCogId(ss[3]);
		setCogAbbr(ss[4]);
		setCogAnnoDetail(ss[5]);
		setCogAnno(ss[6]);
		setCogAnnoBig(ss[7]);
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
	public void setCogAnno(String cogAnno) {
		this.cogAnno = cogAnno;
	}
	public void setCogAnnoBig(String cogAnnoBig) {
		this.cogAnnoBig = cogAnnoBig;
	}
	/** Cog的单字母缩写 */
	public String getCogAbbr() {
		return cogAbbr;
	}
	/** cogId */
	public String getCogId() {
		return cogId;
	}
	/** cog的单字母所对应的注释 */
	public String getCogAnno() {
		return cogAnno;
	}
	/** cog的大类注释，好几个字母有一个大类注释 */
	public String getCogAnnoBig() {
		return cogAnnoBig;
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
		lsResult.add(cogAnno);
		lsResult.add(cogAnnoBig);
		return lsResult;
	}
}
