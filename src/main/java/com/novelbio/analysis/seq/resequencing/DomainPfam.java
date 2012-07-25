package com.novelbio.analysis.seq.resequencing;

import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;

/**
 * pfam domain的数据库，指定每个蛋白都有哪些domain
 * @author zong0jie
 *
 */
public class DomainPfam extends ListAbsSearch<DomainDetail, ListCodAbs<DomainDetail>, ListCodAbsDu<DomainDetail,ListCodAbs<DomainDetail>>>{
	private static final long serialVersionUID = -4658442601484192818L;
	static HashMap<String, DomainPfam> hashDomain = new HashMap<String, DomainPfam>();
	String accID = "";
	/** 该蛋白上某个位点的坐标，第一位是1 */
	int aaLoc = 0;
	
	public DomainPfam(String accID) {
		this.accID = accID;
	}
	
	public String getAccID() {
		return accID;
	}
	
	public static HashMap<String, DomainPfam> readDomain(String pfamFileTxt) {
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
			domainPfam.add(new DomainDetail(string));
		}
		return hashDomain;
	}
	
	public static DomainPfam getDomainPfam(String geneID) {
		return hashDomain.get(geneID);
	}

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
		int domainNum = getNumCodInEle(aaLoc);
		if (domainNum > 0) {
			result = "inside domain: " + "\tdomain name:" + get(getNumCodInEle(domainNum - 1)).getName();
		}
		else if (domainNum < 0) {
			result = "outside domain: ";
			result = result + "\tbetween domain " + get(-domainNum - 1).getName() + 
			" and " + get(-domainNum - 1).getName();
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

	@Override
	protected ListCodAbs<DomainDetail> creatGffCod(String listName,
			int Coordinate) {
		return new ListCodAbs<DomainDetail>(listName, Coordinate);
	}

	@Override
	protected ListCodAbsDu<DomainDetail, ListCodAbs<DomainDetail>> creatGffCodDu(
			ListCodAbs<DomainDetail> gffCod1, ListCodAbs<DomainDetail> gffCod2) {
		return new ListCodAbsDu<DomainDetail, ListCodAbs<DomainDetail>>(gffCod1, gffCod2);
	}
	
}


class DomainDetail extends ListDetailAbs {
	String domainID = "";
	String Description = "";
	
	/**
	 * 输入pfam批量结果中的某一行，获得该domain的具体信息
	 * @param domainPfamLine
	 */
	public DomainDetail(String domainPfamLine) {
		super();
		String[] ss = domainPfamLine.split("\t");
		super.setParentName(ss[0]);
		super.setCis5to3(true);
		super.addItemName(ss[6]);
		super.setStartAbs(Integer.parseInt(ss[1]));
		super.setEndAbs(Integer.parseInt(ss[2]));
		this.domainID = ss[5];
	}
	
	public DomainDetail(String parentName, int startLoc, int endLoc, String name, String description) {
		super(parentName, name, true);
		super.setStartAbs(startLoc);
		super.setEndAbs(endLoc);
		this.Description = description;
	}

	public String getDescription() {
		return Description;
	}
	public String getDomainID() {
		return domainID;
	}
	
}
