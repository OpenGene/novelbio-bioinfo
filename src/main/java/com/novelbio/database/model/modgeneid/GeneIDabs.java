package com.novelbio.database.model.modgeneid;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.SepSign;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modgo.GOInfoGenID;
import com.novelbio.database.model.modgo.GOInfoUniID;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ManageBlastInfo;
import com.novelbio.database.service.servgeneanno.ManageGeneInfo;
import com.novelbio.database.service.servgeneanno.ManageNCBIUniID;
import com.novelbio.database.service.servgeneanno.ManageUniGene2Go;
import com.novelbio.database.service.servgeneanno.ManageUniGeneInfo;

public class GeneIDabs implements GeneIDInt {
	private static Logger logger = Logger.getLogger(GeneIDabs.class);
	static HashMap<Integer, String> hashDBtype = new HashMap<Integer, String>();
	
	AgeneUniID ageneUniID;
	/** 是否没有在数据库中查询到 */
	boolean isAccID = false;
	
	/** 譬方和多个物种进行blast，然后结合这些物种的信息，取并集 */
	ArrayList<BlastInfo> lsBlastInfos = null;
	AGeneInfo geneInfo = null;
	KeggInfo keggInfo;
	GOInfoAbs goInfoAbs = null;
	
	ArrayList<GeneID> lsBlastGeneID = null;
	
	boolean overrideUpdateDBinfo = false;
	// //////////////////// service 层
	ManageNCBIUniID servNCBIUniID = new ManageNCBIUniID();
	ManageBlastInfo servBlastInfo = new ManageBlastInfo();
	ManageGeneInfo servGeneInfo = new ManageGeneInfo();
	ManageUniGeneInfo servUniGeneInfo = new ManageUniGeneInfo();
	// ///////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 输入已经查询好的ageneUniID
	 * @param ageneUniID
	 */
	protected GeneIDabs(AgeneUniID ageneUniID) {
		this.ageneUniID = ageneUniID;
	}
	
	protected GeneIDabs(int idType, String genUniID, int taxID) {
		AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(idType);
		ageneUniID.setGenUniID(genUniID);
		ageneUniID.setTaxID(taxID);
		List<? extends AgeneUniID> lsTmp = servNCBIUniID.queryLsAgeneUniID(ageneUniID);
		if (lsTmp.size() == 0) {
			this.ageneUniID = ageneUniID;
			return;
		}
		String DBsource = getDatabaseType(lsTmp.get(0).getTaxID());
		this.ageneUniID = getGeneUniIDwithDB(lsTmp, DBsource);
	}
	
	/**
	 * 如果没搜到，就把geneUniID设定为accID
	 * @param accID
	 * @param taxID
	 */
	protected GeneIDabs(String accID, int taxID) {
		List<AgeneUniID> lsAgeneUniID = getNCBIUniTax(accID, taxID);
		if (lsAgeneUniID.size() > 0) {
			ageneUniID = lsAgeneUniID.get(0);
			isAccID = false;
		} else {
			ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_UNIID);
			ageneUniID.setAccID(accID);
			ageneUniID.setTaxID(taxID);
			isAccID = true;
		}
	}
	
	/**
	 * 根据输入的物种，返回该物种特定的那个ID，譬如水稻就返回LOCID
	 * 如果没有，则再搜索RefSeqRNA
	 * 如果再没有，则随便返回一个ID
	 * @return
	 */
	private AgeneUniID getGeneUniIDwithDB(List<? extends AgeneUniID> lsAgenUniID, String dbSource) {
		AgeneUniID ageneUniID = null;
		if (lsAgenUniID.size() > 0) {
			for (AgeneUniID ageneUniIDSub : lsAgenUniID) {
				if (ageneUniIDSub.getDataBaseInfo().getDbName().equals(dbSource)) {
					ageneUniID = ageneUniIDSub;
					break;
				} else if (ageneUniIDSub.getDataBaseInfo().getDbName().equals(DBAccIDSource.RefSeqRNA.toString())) {
					ageneUniID = ageneUniIDSub;
				}
			}
			isAccID = false;
			if (ageneUniID != null) {
				return ageneUniID;
			} else {
				return lsAgenUniID.get(0);
			}
		} else {
			isAccID = true;
			return ageneUniID;
		}
	}
	
	/**
	 * 某个物种的ID有其最特异的数据库来源
	 * @return
	 */
	private String getDatabaseType(int taxID) {
		if (hashDBtype.size() == 0) {
			hashDBtype.put(39947, DBAccIDSource.TIGR_rice.toString());
			hashDBtype.put(10090, DBAccIDSource.RefSeqRNA.toString());
			hashDBtype.put(3702, DBAccIDSource.TAIR_ATH.toString());
			hashDBtype.put(3847, DBAccIDSource.SOYBASE.toString());
			hashDBtype.put(4102, DBAccIDSource.PlantGDB.toString());
		}
		String result = hashDBtype.get(taxID);
		if (result == null) {
			return DBAccIDSource.RefSeqRNA.toString();
		}
		return result;
	}
	
	@Override
	public AgeneUniID getAgeneUniID() {
		return ageneUniID;
	}
	
	@Override
	public DBInfo getDBinfo() {
		return ageneUniID.getDataBaseInfo();
	}
	
	/**
	 * 有geneUniID就用geneUniID去query
	 * 否则就用accID去query
	 */
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		String geneUniID = ageneUniID.getGenUniID();
		if (!ageneUniID.isValidGenUniID()) {
			geneUniID = ageneUniID.getAccID();
		}
		lsBlastInfos = new ArrayList<BlastInfo>();
		for (int i : StaxID) {
			if (i <= 0) {
				continue;
			}
			BlastInfo blastInfo = servBlastInfo.queryBlastInfo(geneUniID, ageneUniID.getTaxID(), i,evalue);
			if (blastInfo != null) {
				lsBlastInfos.add(blastInfo);
			}
		}
		// 重置blastGeneID的内容
		lsBlastGeneID = null;
	}
	
	/**
	 * 单个物种的blast 获得本copedID blast到对应物种的第一个copedID，没有就返回null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	@Override
	public GeneID getGeneIDBlast() {
		ArrayList<GeneID> lsGeneIDs = getLsBlastGeneID();
		if (lsGeneIDs.size() != 0) {
			return lsGeneIDs.get(0);
		}
		return null;
	}

	/**
	 * blast多个物种 首先要设定blast的目标 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 给定一系列的目标物种的taxID，获得CopedIDlist 如果没有结果，返回一个空的lsResult
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	@Override
	public ArrayList<GeneID> getLsBlastGeneID() {
		if (lsBlastGeneID != null) {
			return lsBlastGeneID;
		}
		lsBlastGeneID = new ArrayList<GeneID>();
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return lsBlastGeneID;
		}
		for (BlastInfo blastInfo : lsBlastInfos) {
			GeneID geneID = getBlastGeneID(blastInfo);
			if (geneID != null) {
				lsBlastGeneID.add(geneID);
			}
		}
		return lsBlastGeneID;
	}

	/**
	 * 获得设定的第一个blast的对象，首先要设定blast的目标
	 * @param blastInfo
	 * @return
	 */
	private GeneID getBlastGeneID(BlastInfo blastInfo) {
		if (blastInfo == null) return null;
		
		int idType = blastInfo.getSubTab();
		GeneID geneID = new GeneID(idType, blastInfo.getSubjectID(), blastInfo.getSubjectTax());
		return geneID;
	}
	/**
	 * blast多个物种 首先要设定blast的目标 用方法： setBlastInfo(double evalue, int... StaxID)
	 * @return 返回blast的信息，包括evalue等，该list和{@link #getLsBlastGeneID()}得到的list是一一对应的
	 */
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return lsBlastInfos;
	}
	
	/**
	 * idType，必须是IDTYPE中的一种
	 * 不过在设定了lsRefAccID后，可以根据具体的lsRefAccID去查找数据库并确定idtype
	 */
	public int getIDtype() {
		//TODO
		if (isAccID && lsRefAccID != null && lsRefAccID.size() > 0) {
			setUpdateGenUniID();
		}
		if (isAccID || ageneUniID == null) {
			return GeneID.IDTYPE_ACCID;
		}
		return ageneUniID.getGeneIDtype();
	}
	
	/**
	 * 具体的accID，如果没有则根据物种随机抓一个出来
	 */
	public String getAccID() {
		return ageneUniID.getAccID();
	}
	
	/**
	 * 具体的accID，根据其默认数据库情况抓一个出来
	 * 如果没有该数据库，则随便返回一个<br>
	 * 如果没有导入数据库，则返回自身
	 */
	public AgeneUniID getAccID_With_DefaultDB() {
		return getGenUniID(ageneUniID, getDatabaseType(ageneUniID.getTaxID()));
	}
	
	/**
	 * * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * @param dbInfo 为null表示不指定dbinfo
	 * @return
	 */
	public AgeneUniID getAccIDDBinfo(String dbInfo) {
		return getGenUniID(ageneUniID, dbInfo);
	}
	
	/**
	 * 指定一个dbInfo，返回该dbInfo所对应的AgeneUniID，并且该AgeneUniID也仅对应一个geneUniID。
	 * 如果该dbinfo没有，则随便返回一个。
	 * @param dbInfo
	 * @return
	 */
	protected AgeneUniID getGenUniID(AgeneUniID genUniID, String dbInfo) {
		if (getIDtype() == GeneID.IDTYPE_ACCID || genUniID.getDataBaseInfo().getDbName().equals(dbInfo)) {
			return genUniID;
		}
		genUniID.setDataBaseInfo("");
		ArrayList<? extends AgeneUniID> lsSubject = servNCBIUniID.queryLsAgeneUniID(genUniID);
		return getGeneUniIDwithDB(lsSubject, dbInfo);
	}
	
	public int getTaxID() {
		return ageneUniID.getTaxID();
	}
	/**
	 * 返回geneinfo信息
	 * @return
	 */
	public AGeneInfo getGeneInfo() {
		setGenInfo();
		return geneInfo;
	}
	
	/**
	 * 获得该基因的description
	 * @return
	 */
	public String getDescription() {
		getGeneInfo();
		if (geneInfo == null) {
			return "";
		}
		return geneInfo.getDescrp();
	}
	
	/**
	 * 获得该基因的symbol
	 * @return
	 */
	public String getSymbol() {
		String symbol = "";
		setGenInfo();
		if (geneInfo != null) {
			symbol = geneInfo.getSymb();
		}
		
		if (symbol == null || symbol.equals("")) {
			symbol = getGenUniID(ageneUniID, getDatabaseType(ageneUniID.getTaxID())).getAccID();
		}
		//TODO
//		symbol = symbol.replace("@", "");
		return symbol;
	}

	/**
	 * 设定geneInfo信息
	 */
	protected void setGenInfo() {
		if (getIDtype() == GeneID.IDTYPE_GENEID) {
			GeneInfo geneInfoq = new GeneInfo();
			long geneID = Long.parseLong(ageneUniID.getGenUniID());
			geneInfoq.setGeneID(geneID);
			geneInfoq.setTaxID(getTaxID());
			geneInfo = servGeneInfo.queryGeneInfo(geneInfoq);
		}
		else if (getIDtype() == GeneID.IDTYPE_UNIID) {
			UniGeneInfo uniGeneInfo = new UniGeneInfo();
			uniGeneInfo.setUniProtID(ageneUniID.getGenUniID());
			uniGeneInfo.setTaxID(getTaxID());
			geneInfo = servUniGeneInfo.queryUniGeneInfo(uniGeneInfo);
		}
	}
	
	/**
	 * 先设定blast的情况 如果blast * 0:symbol 1:description  2:subjectSpecies 3:evalue
	 * 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno(boolean blast) {
		String[] tmpAnno = null;
		if (blast) {
			tmpAnno = new String[6];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbol();
			tmpAnno[1] = getDescription();
			if (getLsBlastGeneID() != null && getLsBlastInfos() != null
					&& getLsBlastInfos().size() > 0) {
				for (int i = 0; i < getLsBlastInfos().size(); i++) {
					if (tmpAnno[2] == null || tmpAnno[2].trim().equals("")) {
						tmpAnno[2] = Species.getSpeciesTaxIDName().get(getLsBlastInfos().get(i).getSubjectTax());
						tmpAnno[3] = getLsBlastInfos().get(i).getEvalue() + "";
						tmpAnno[4] = getLsBlastGeneID().get(i).getSymbol();
						tmpAnno[5] = getLsBlastGeneID().get(i).getDescription();
					} else {
						tmpAnno[2] = tmpAnno[2] + "//" + Species.getSpeciesTaxIDName().get(getLsBlastInfos().get(i).getSubjectTax());
						tmpAnno[3] = tmpAnno[3] + "//" + getLsBlastInfos().get(i).getEvalue();
						tmpAnno[4] = tmpAnno[4] + "//" + getLsBlastGeneID().get(i).getSymbol();
						tmpAnno[5] = tmpAnno[5] + "//" + getLsBlastGeneID().get(i).getDescription();
					}
				}
			}
		} else {
			tmpAnno = new String[2];
			for (int i = 0; i < tmpAnno.length; i++) {
				tmpAnno[i] = "";
			}
			tmpAnno[0] = getSymbol();
			tmpAnno[1] = getDescription();
		}
		return tmpAnno;
	}

	/**
	 * 返回该基因所对应的GOInfo信息，不包含Blast
	 * @return
	 */
	public GOInfoAbs getGOInfo() {
		if (goInfoAbs == null) {
			goInfoAbs = GOInfoAbs.createGOInfoAbs(ageneUniID.getGeneIDtype(), ageneUniID.getGenUniID(), ageneUniID.getTaxID());
		}
		return goInfoAbs;
	}
	/**
	 * 返回该CopedID所对应的Gene2GOInfo <br>
	 * GO_BP<br>
	 * GO_CC<br>
	 * GO_MF<br>
	 * GO_ALL<br>
	 * @param GOType
	 * @return
	 */
	public ArrayList<AGene2Go> getGene2GO(GOtype GOType) {
		return getGOInfo().getLsGene2Go(GOType);
	}
	/**
	 * blast多个物种 首先设定blast的物种 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的GoInfo
	 */
	public List<AGene2Go> getGene2GOBlast(GOtype GOType) {
		List<GOInfoAbs> lsGoInfo = new ArrayList<GOInfoAbs>();

		ArrayList<GeneID> lsBlastGeneIDs = getLsBlastGeneID();
		for (GeneID geneID : lsBlastGeneIDs) {
			lsGoInfo.add(geneID.getGOInfo());
		}
		lsGoInfo.add(getGOInfo());
		return GOInfoAbs.getLsGen2Go(lsGoInfo, GOType);
	}
	
	ArrayList<KGentry> lsKGentries = null;
	// ////////////////KEGG //////////////////////////////////////////////
	/**
	 * 获得该CopeID的List-KGentry,如果没有或为空，则返回null
	 * 
	 * @param blast
	 *            是否blast到相应物种查看
	 * @param StaxID
	 *            如果blast为true，那么设定StaxID
	 * @return 如果没有就返回一个空的list
	 */
	public ArrayList<KGentry> getKegEntity(boolean blast) {
		getKeggInfo();
		if (!blast) {
			return keggInfo.getLsKgGentries(null);
		} else {
			ArrayList<GeneID> lsGeneIDsBlast = getLsBlastGeneID();
			ArrayList<KeggInfo> lsKeggInfos = new ArrayList<KeggInfo>();
			for (GeneID copedID : lsGeneIDsBlast) {
				lsKeggInfos.add(copedID.getKeggInfo());
			}
			return keggInfo.getLsKgGentries(lsKeggInfos);
		}
	}
	/**
	 * 获得相关的Kegg信息
	 * @return
	 */
	public KeggInfo getKeggInfo() {
		if (keggInfo != null) {
			return keggInfo;
		}
		keggInfo = new KeggInfo(getIDtype(), ageneUniID.getGenUniID(), ageneUniID.getTaxID());
		return keggInfo;
	}
	
	/**
	 * blast多个物种 首先设定blast的物种 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath
	 * 没有就返回空的list
	 */
	@Override
	public ArrayList<KGpathway> getKegPath(boolean blast) {
		getKeggInfo();
		if (blast) {
			ArrayList<KeggInfo> lskeggInfo = new ArrayList<KeggInfo>();
			ArrayList<GeneID> lsBlastCopedIDs = getLsBlastGeneID();
			for (GeneID copedID : lsBlastCopedIDs) {
				lskeggInfo.add(copedID.getKeggInfo());
			}
			return keggInfo.getLsKegPath(lskeggInfo);
		} else {
			return keggInfo.getLsKegPath();
		}
	}
	// ///////////////// update method
	/**
	 * 记录可能用于升级数据库的ID 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	ArrayList<String> lsRefAccID = new ArrayList<String>();

	/**
	 * 添加可能用于升级数据库的ID 
	 * 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	@Override
	public void addUpdateRefAccID(String... refAccID) {
		for (String string : refAccID) {
			String tmpRefID = GeneID.removeDot(string);
			if (tmpRefID == null) {
				continue;
			}
			lsRefAccID.add(tmpRefID);
		}
	}
	/**
	 * 设置可能用于升级数据库的ID 
	 * 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	@Override
	public void setUpdateRefAccID(String... refAccID) {
		lsRefAccID.clear();
		addUpdateRefAccID(refAccID);
	}
	/**
	 * 记录可能用于升级数据库的ID 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	@Override
	public void setUpdateRefAccID(ArrayList<String> lsRefAccID) {
		this.lsRefAccID.clear();
		for (String string : lsRefAccID) {
			String tmpRefID = GeneID.removeDot(string);
			if (tmpRefID == null) {
				continue;
			}
			this.lsRefAccID.add(tmpRefID);
		}
	}

	/**
	 * 记录该ID的物种ID和数据库信息，用于修正以前的数据库
	 * 
	 * @param taxID
	 * @param DBInfo
	 * @param 是否用本DBInfo修正以前的DBInfo
	 * 不管是true还是false，geneinfo都会用其进行修正
	 */
	@Override
	public void setUpdateDBinfo(DBAccIDSource DBInfo, boolean overlapDBinfo) {
		if (DBInfo != null) {
			ageneUniID.setDataBaseInfo(DBInfo.toString());
		}
		this.overrideUpdateDBinfo = overlapDBinfo;
	}
	
	/**
	 * 记录该ID的物种ID和数据库信息，用于修正以前的数据库
	 * 
	 * @param taxID
	 * @param DBInfo
	 * @param 是否用本DBInfo修正以前的DBInfo
	 * 不管是true还是false，geneinfo都会用其进行修正
	 */
	public void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo) {
		if (DBInfo != null && !DBInfo.equals("")) {
			ageneUniID.setDataBaseInfo(DBInfo);
		}
		this.overrideUpdateDBinfo = overlapDBinfo;
	}
	
	/**
	 * 输入已知的geneUniID和IDtype
	 * @param geneUniID
	 * @param idType
	 *            必须是CopedID.IDTYPE_GENEID等，必须输入
	 */
	@Override
	public void setUpdateGeneID(String geneUniID, int idType) {
		if (geneUniID != null && !geneUniID.trim().equals("")
				&& !geneUniID.trim().equals("0")) {
			AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(idType);
			ageneUniID.setDataBaseInfo(this.ageneUniID.getDataBaseInfo());
			ageneUniID.setAccID(this.ageneUniID.getAccID());
			ageneUniID.setGenUniID(geneUniID);
			ageneUniID.setTaxID(this.getTaxID());
			this.ageneUniID = ageneUniID;
			this.isAccID = false;
		}
	}
	
	/** 设定该ID的accID */
	@Override
	public void setUpdateAccID(String accID) {
		ageneUniID.setAccID(GeneID.removeDot(accID));

	}
	/** 设定该ID的accID，不经过处理的ID */
	@Override
	public void setUpdateAccIDNoCoped(String accID) {
		ageneUniID.setAccID(accID);
	}

	/**
	 * 依次输入需要升级的GO信息，最后升级 这里只是先获取GO的信息，最后调用升级method的时候再升级
	 * 可以连续不断的添加
	 * @param GOID 必填
	 * @param GOdatabase 没有就设置为 null 
	 * @param GOevidence 没有就设置为 null 
	 * @param GORef 没有就设置为 null 
	 * @param gOQualifiy 没有就设置为 null 
	 */
	@Override
	public void addUpdateGO(String GOID, DBAccIDSource GOdatabase, String GOevidence,
			String GORef, String gOQualifiy) {
		goInfoAbs.addGOid(getTaxID(), GOID, GOdatabase, GOevidence, GORef, gOQualifiy);
	}

	/**
	 * 输入需要update的geneInfo，注意不需要设定geneUniID
	 * 但是需要设定
	 * @param geneInfo
	 */
	@Override
	public void setUpdateGeneInfo(AGeneInfo geneInfo) {
		this.geneInfo = geneInfo;
	}

	// 专门用于升级
	ArrayList<BlastInfo> lsBlastInfosUpdate = null;

	/**
	 * 如果没有QueryID, SubjectID, taxID中的任何一项，就不升级 如果evalue>50 或 evalue<0，就不升级
	 * 可以连续不断的添加
	 * @param blastInfo
	 */
	@Override
	public void addUpdateBlastInfo(BlastInfo blastInfo) {
		if (!ageneUniID.isValidGenUniID() || blastInfo.getSubTab() == GeneID.IDTYPE_ACCID) {
			return;
		}
		if (lsBlastInfosUpdate == null) {
			lsBlastInfosUpdate = new ArrayList<BlastInfo>();
		}
		lsBlastInfosUpdate.add(blastInfo);
	}
	
	/**
	 * 如果新的ID不加入UniID，那么就写入指定的文件中 文件需要最开始用set指定
	 * 只要有一个错误就会返回
	 * @param updateUniID
	 */
	@Override
	public boolean update(boolean updateUniID) {
		setUpdateGenUniID();
		if (!updateUniID && getIDtype() == GeneID.IDTYPE_ACCID) {
			return false;
		}
		if (!ageneUniID.isValidGenUniID()) {
			logger.error("geneID为0，请check: " + ageneUniID.getAccID());
			return false;
		}

		if(!updateGeneID(updateUniID)) {
			return false;
		}
		if(!updateGeneInfo()) {
			return false;
		}
		if (!updateGene2Go()) {
			return false;
		}
		if (!updateBlastInfo()) {
			return false;
		}
		return true;
	}

	/** 升级GO数据库 */
	private boolean updateGene2Go() {
		try {
			goInfoAbs.update();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 根据输入的geneUniID--中的geneID，升级AccID和DBinfo
	 * 升级geneID数据库，并且将geneUniID按照数据库进行重置 <b>只升级第一个获得的geneID</b>
	 * 如果accID没有，则不升级
	 * @param 如果在数据库中没有找到对应的ID
	 *            ，是否将ID导入UniID库
	 *            true,导入uniID库，并且重置idtype
	 * @throws EOFException
	 */
	private boolean updateGeneID(boolean updateUniID) {
		if (!ageneUniID.isValidGenUniID() && ageneUniID.getAccID() == null) {
			return false;
		}
		
		if (ageneUniID.getDataBaseInfo() == null || ageneUniID.getDataBaseInfo().getDbName().equals("")) {
			logger.error("升级geneID时没有设置该gene的数据库来源，自动设置为NCBIID");
			ageneUniID.setDataBaseInfo(DBAccIDSource.NCBI.toString());
		}
		// 只升级第一个获得的geneID
		if (getIDtype() != GeneID.IDTYPE_ACCID) {
			ageneUniID.update(overrideUpdateDBinfo);
		} else if (updateUniID) {
			AgeneUniID uniProtID =  AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_UNIID);
			uniProtID.setAccID(ageneUniID.getAccID());
			uniProtID.setDataBaseInfo(ageneUniID.getDataBaseInfo());
			uniProtID.setGenUniID(ageneUniID.getGenUniID());
			uniProtID.setTaxID(ageneUniID.getTaxID());
			uniProtID.update(overrideUpdateDBinfo);
			//TODO 防止下一个导入的时候出错
//			try { hread.sleep(10); } catch (InterruptedException e) { }
		} else {
			return false;
		}
		return true;
	}
	/**
	 * 根据geneID和idType升级相关的geneInfo
	 * 注意，geneInfo只能是单个，不能是合并过的geneInfo
	 * 如果没有genUniID，或者没有搜索到对应的genID，则返回false；
	 * 如果没有geneInfo信息，则认为不需要升级，返回true
	 */
	private boolean updateGeneInfo() {
		if (!ageneUniID.isValidGenUniID()) {
			return false;
		}
		if (geneInfo == null) {
			return true;
		}
		geneInfo.setTaxID(getTaxID());
		if (getIDtype() == GeneID.IDTYPE_UNIID) {
			servUniGeneInfo.updateUniGenInfo(ageneUniID.getGenUniID(), getTaxID(), geneInfo);
		} else if (getIDtype() == GeneID.IDTYPE_GENEID) {
			servGeneInfo.updateGenInfo(ageneUniID.getGenUniID(), getTaxID(), geneInfo);
		} else {
			return false;
		}
		updateGeneInfoSymbolAndSynonyms(getIDtype(), geneInfo);
		geneInfo = null;
		return true;
	}
	
	private void updateGeneInfoSymbolAndSynonyms(int idType, AGeneInfo geneInfoUpdate) {
		AgeneUniID ageneUniID = null;
		String sep = SepSign.SEP_ID;
		if (geneInfoUpdate.getSep() != null && !geneInfoUpdate.getSep().equals("")) {
			sep = geneInfoUpdate.getSep();
		}
		if (geneInfoUpdate.getSymb() != null) {
			String[] ssymb = geneInfoUpdate.getSymb().split(sep);
			for (String string : ssymb) {
				ageneUniID = AgeneUniID.creatAgeneUniID(idType);
				ageneUniID.setAccID(string.trim());
				ageneUniID.setDataBaseInfo(DBAccIDSource.Symbol.toString());
				ageneUniID.setGenUniID(this.ageneUniID.getGenUniID());
				ageneUniID.setTaxID(getTaxID());
				ageneUniID.update(true);
			}
		}
		if (geneInfoUpdate.getSynonym() != null) {
			String[] ssynonym = geneInfoUpdate.getSynonym().split(sep);
			for (String string : ssynonym) {
				ageneUniID = AgeneUniID.creatAgeneUniID(idType);
				ageneUniID.setAccID(string.trim());
				ageneUniID.setDataBaseInfo(DBAccIDSource.Synonyms.toString());
				ageneUniID.setGenUniID(this.ageneUniID.getGenUniID());
				ageneUniID.setTaxID(getTaxID());
				ageneUniID.update(true);
			}
		}
	}
	
	// /////////////////////// 升级 Blast 的信息
	private boolean updateBlastInfo() {
		boolean blastCorrect = true;
		if (!ageneUniID.isValidGenUniID()) {
			return false;
		}
		if (lsBlastInfosUpdate == null) {
			return true;
		}
		for (BlastInfo blastInfo : lsBlastInfosUpdate) {
			if (blastInfo.getSubTab() == GeneID.IDTYPE_ACCID) {
				blastCorrect = false;
				continue;
			}
			servBlastInfo.updateBlast(blastInfo);
		}
		return blastCorrect;
	}

	// /////////////////////// 升级 uniGene 的信息
	/**
	 * 根据lsRefAccID的信息设定geneUniID和IDtype 获得所对应的geneUniID<br>
	 * 如果没搜到则不动<br>
	 * 内部已经将genUniID和IDType和TaxID都改过来了
	 */
	private void setUpdateGenUniID() {
		if (getIDtype() != GeneID.IDTYPE_ACCID) {
			return;
		}
		
		// 保存所有refID--也就是用于查找数据库的refxDB的信息ID，他们所对应的geneUniID
		ArrayList<ArrayList<AgeneUniID>> lsgeneID = new ArrayList<ArrayList<AgeneUniID>>();
		ArrayList<ArrayList<AgeneUniID>> lsgeneIDUniProt = new ArrayList<ArrayList<AgeneUniID>>();
		for (String refAccID : lsRefAccID) {
			ArrayList<AgeneUniID> lsTmpGenUniID = getNCBIUniTax(refAccID, this.ageneUniID.getTaxID());
			if (lsTmpGenUniID.size() == 0) {
				continue;
			} else if (lsTmpGenUniID.size() == 1 && lsTmpGenUniID.get(0).getGeneIDtype().equals(GeneID.IDTYPE_GENEID)) {
				AgeneUniID ageneUniID = lsTmpGenUniID.get(0);
				ageneUniID.setAccID(this.ageneUniID.getAccID());
				ageneUniID.setDataBaseInfo(this.ageneUniID.getDataBaseInfo());
				this.ageneUniID = ageneUniID;
				this.isAccID = false;
				return;
			} else if (lsTmpGenUniID.get(0).getDataBaseInfo().getDbOrg().equals("UniProt")) {
				lsgeneIDUniProt.add(lsTmpGenUniID);
			} else {
				lsgeneID.add(lsTmpGenUniID);
			}
		}
		
		//如果ref所对应的NCBI的geneID超过两个，就需要check了
		if (lsgeneID.size() == 0 && lsgeneIDUniProt.size() > 0) {
			logger.error("出现奇怪的ID，请check：" + getAccID());
			lsgeneID.addAll(lsgeneIDUniProt);
		}
		
		// 挑选出含有geneUniID的geneID
		Collections.sort(lsgeneID, new Comparator<ArrayList<AgeneUniID>>() {
			public int compare(ArrayList<AgeneUniID> o1, ArrayList<AgeneUniID> o2) {
				Integer o1Info = o1.get(0).getGeneIDtype();
				Integer o2Info = o2.get(0).getGeneIDtype();
				int flag = o1Info.compareTo(o2Info);
				if (flag != 0) {
					return flag;
				} else {
					Integer o1Len = o1.size();
					Integer o2Len = o2.size();
					return o1Len.compareTo(o2Len);
				}
			}
		});
		if (lsgeneID.size() == 0) {
			if (this.ageneUniID.getAccID() != null && !this.ageneUniID.getAccID().equals("")) {
				if (!this.ageneUniID.isValidGenUniID()) {
					this.ageneUniID.setGenUniID(this.ageneUniID.getAccID());
				}
				this.isAccID = true;
				return;
			}
		}
		AgeneUniID ageneUniID = lsgeneID.get(0).get(0);
		ageneUniID.setAccID(this.ageneUniID.getAccID());
		ageneUniID.setDataBaseInfo(this.ageneUniID.getDataBaseInfo());
		this.ageneUniID = ageneUniID;
		this.isAccID = false;
	}

	// ///////////////// static method
	/**
	 * 给定一个accessID，搜索数据库
	 * 返回list-AgeneUniID
	 * 如果同一个物种得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目
	 * 如果没搜到，则返回空的list
	 * @param accID  输入的accID,没有内置去空格去点
	 * @param taxID 物种ID，如果不知道就设置为0，只要不是symbol都可以为0
	 * @return list-string[] 0: IDtype 1: geneUniID 2: taxID
	 */
	protected static List<AgeneUniID> getNCBIUniTax(String accID, int taxID) {
		ArrayList<AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (accID == null || accID.equals("")) {
			return lsResult;
		}
		
		ManageNCBIUniID servGeneAnno = new ManageNCBIUniID();
		AgeneUniID ncbiid = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
		ncbiid.setAccID(accID);
		ncbiid.setTaxID(taxID);
		List<? extends AgeneUniID> lsNcbiids = null;
		// 先查ncbiid
		try {
			lsNcbiids = servGeneAnno.queryLsAgeneUniID(ncbiid);
		} catch (Exception e) {
			return null;
		}
		if (lsNcbiids != null && lsNcbiids.size() > 0) {
			return getLsGeneIDinfo(lsNcbiids);
		}
		// 查不到查uniprotID
		else {
			AgeneUniID uniProtID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_UNIID);
			uniProtID.setAccID(accID);
			uniProtID.setTaxID(taxID);
			List<? extends AgeneUniID> lsUniProtIDs = servGeneAnno.queryLsAgeneUniID(uniProtID);
			return getLsGeneIDinfo(lsUniProtIDs);
		}
	}
	
	/** 指定一系列的NCBIID或者UniID，进行过滤，
	 * 主要是过滤
	 * 如果同一个物种得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目 */
	private static List<AgeneUniID> getLsGeneIDinfo(List<? extends AgeneUniID> lsNcbiids) {
		ArrayList<AgeneUniID> lsGeneIDinfo = new ArrayList<AgeneUniID>();
		for (AgeneUniID geneUniID : lsNcbiids) {
			if (geneUniID.getDataBaseInfo().getDbName().equals(DBAccIDSource.Synonyms.toString())) {
				continue;
			}
			lsGeneIDinfo.add(geneUniID);
		}
		if (lsGeneIDinfo.size() == 0 && lsNcbiids.size() > 0) {
			lsGeneIDinfo.addAll(lsNcbiids);
		}
		return lsGeneIDinfo;
	}
	// /////////////////////////// 重写equals等
	/**
	 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
	 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
	 * 
	 * @Override
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		GeneID otherObj = (GeneID) obj;
		
		return ageneUniID.equals(otherObj.getAgeneUniID());
	}

	/**
	 * 重写hashcode
	 */
	public int hashCode() {
		String hash = "";
		if (ageneUniID.isValidGenUniID()) {
			hash = ageneUniID.getGenUniID().trim() + "sep_@@_genUni_" + getIDtype() + "@@" + getTaxID();
		} else if (!ageneUniID.isValidGenUniID() && ageneUniID.getAccID() != null && !ageneUniID.getAccID().equals("")) {
			hash = ageneUniID.getAccID().trim() + "@@accID" + getIDtype() + "@@" + getTaxID();
		} else if (!ageneUniID.isValidGenUniID() && (ageneUniID.getAccID() == null || ageneUniID.getAccID().equals(""))) {
			hash = "";
		}
		return hash.hashCode();
	}

}
