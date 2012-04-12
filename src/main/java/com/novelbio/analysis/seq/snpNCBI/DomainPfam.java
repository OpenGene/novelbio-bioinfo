package com.novelbio.analysis.seq.snpNCBI;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ElementAbs;
import com.novelbio.base.dataStructure.listOperate.ListAbs;

/**
 * pfam domain的数据库，指定每个蛋白都有哪些domain
 * @author zong0jie
 *
 */
public class DomainPfam extends ListAbs<DomainDetail>{
	static HashMap<String, DomainPfam> hashDomain = new HashMap<String, DomainPfam>();
	String accID = "";
	
	public DomainPfam(String accID) {
		this.accID = accID;
	}
	
	public String getAccID() {
		return accID;
	}
	
	public void addDomainDetail(DomainDetail domainDetail)
	{
		lsElement.add(domainDetail);
	}
	
	public static HashMap<String, DomainPfam> readDomain(String pfamFileTxt)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(pfamFileTxt, false);
		hashDomain = new HashMap<String, DomainPfam>();
		String tmpAccID = "";	DomainPfam domainPfam = null;
		for (String string : txtRead.readlines()) {
			string = string.trim();
			if (string.startsWith("#") || string.equals("")) {
				continue;
			}
			String[] tmp = string.split("\t");
			if (!tmp[0].equals(tmpAccID)) {
				tmpAccID = tmp[0];
				domainPfam = new DomainPfam(tmpAccID);
				hashDomain.put(tmpAccID, domainPfam);
			}
			domainPfam.addDomainDetail(new DomainDetail(string));
		}
		return hashDomain;
	}
	
	public static DomainPfam getDomainPfam(String geneID)
	{
		return hashDomain.get(geneID);
	}
	
	/**
	 * 该蛋白上某个位点的坐标，第一位是1
	 */
	int aaLoc = 0;
	
	public void setAALoc(int aaLoc) {
		this.aaLoc = aaLoc;
	}
	
	public int getLoc2End() {
		return Math.abs(super.getLoc2End(aaLoc));
	}
	
	public int getLoc2Start() {
		return super.getLoc2Start(aaLoc);
	}
	
	public String toString() {
		String result = "";
		int domainNum = getLocInEleNum(aaLoc);
		if (domainNum > 0) {
			result = "inside domain: " + "\tdomain name:" + lsElement.get(getLocInEleNum(domainNum - 1)).getName();
		}
		else if (domainNum < 0) {
			result = "outside domain: ";
			result = result + "\tbetween domain " + lsElement.get(-domainNum - 1).getName() + 
			" and " + lsElement.get(-domainNum - 1).getName();
		}
		else {
			result = "outside domain: ";
			if (!isLocDownStart(aaLoc)) {
				result = result + "\tbefore all domains";
			}
			else if (isLocDownEnd(aaLoc)) {
				result = result + "\tafter all domains";
			}
		}
		return result;
	}
	
}


class DomainDetail implements ElementAbs
{
	/**
	 * 输入pfam批量结果中的某一行，获得该domain的具体信息
	 * @param domainPfamLine
	 */
	public DomainDetail(String domainPfamLine) {
		String[] ss = domainPfamLine.split("\t");
		this.startLoc = Integer.parseInt(ss[1]);
		this.endLoc = Integer.parseInt(ss[2]);
		this.domainID = ss[5];
		this.Name = ss[6];
	}
	
	
	
	public DomainDetail(int startLoc, int endLoc, String name, String description) {
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.Name = name;
		this.Description = description;
	}
	
	int startLoc = 0;
	int endLoc = 0;
	String domainID = "";
	String Name = "";
	String Description = "";
	public String getName() {
		return Name;
	}
	public String getDescription() {
		return Description;
	}
	public String getDomainID() {
		return domainID;
	}
	@Override
	public int getStart() {
		return this.startLoc;
	}
	
	@Override
	public int getEnd() {
		return this.endLoc;
	}
	
	@Override
	public void setStart(int startLoc) {
		this.startLoc = startLoc;
	}

	@Override
	public void setEnd(int endLoc) {
		this.endLoc = endLoc;
	}

	@Override
	public int getLen() {
		return Math.abs(startLoc - endLoc) + 1;
	}
}
