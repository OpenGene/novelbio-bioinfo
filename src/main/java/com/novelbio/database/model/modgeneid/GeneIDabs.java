package com.novelbio.database.model.modgeneid;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.model.modkegg.KeggInfoAbs;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ManageDBInfo;
import com.novelbio.database.service.servgeneanno.ManageGeneInfo;
import com.novelbio.database.service.servgeneanno.ManageNCBIUniID;

public class GeneIDabs implements GeneIDInt {
	private static final Logger logger = LoggerFactory.getLogger(GeneIDabs.class);
	static Map<Integer, String> hashDBtype = new HashMap<Integer, String>();
	// //////////////////// service 层
	static ManageNCBIUniID servNCBIUniID = ManageNCBIUniID.getInstance();
	static ManageGeneInfo servGeneInfo = new ManageGeneInfo();
	static ManageDBInfo manageDBInfo = ManageDBInfo.getInstance();
	
	/** 是否没有在数据库中查询到 */
	boolean isAccID = false;
	AgeneUniID ageneUniID;
	/** 是否需要升级genUniID */
	boolean updateGenUniID = false;

	/** 譬方和多个物种进行blast，然后结合这些物种的信息，取并集 */
	BlastList blastList;
	AGeneInfo geneInfo = null;
	KeggInfoAbs keggInfo;
	GOInfoAbs goInfoAbs = null;

	boolean overrideUpdateDBinfo = false;
	/**
	 * 记录可能用于升级数据库的ID 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	Set<String> lsRefAccID = new HashSet<String>();
	
	/** 是否删除尾巴上的.1等信息 */
	boolean removeDot = false;
	/**
	 * 输入已经查询好的ageneUniID
	 * 
	 * @param ageneUniID
	 */
	protected GeneIDabs(AgeneUniID ageneUniID) {
		this.ageneUniID = ageneUniID;
		if (ageneUniID.getId() == null || ageneUniID.getId().equals("")) {
			updateGenUniID = true;
		}
	}

	protected GeneIDabs(int idType, String geneUniID, int taxID) {
		if (idType == GeneID.IDTYPE_ACCID) {
			addAccID(geneUniID, taxID);
			return;
		}
		
		AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(idType);
		ageneUniID.setGenUniID(geneUniID);
		ageneUniID.setTaxID(taxID);
		List<AgeneUniID> lsTmp = new ArrayList<AgeneUniID>();
		if (taxID <= 0) {
			lsTmp = servNCBIUniID.findByGeneUniID(idType, geneUniID, taxID);
		}
		if (lsTmp.size() == 0) {
			this.ageneUniID = ageneUniID;
			updateGenUniID = true;
			return;
		}
		String DBsource = getDatabaseType(lsTmp.get(0).getTaxID());
		this.ageneUniID = getGeneUniIDwithDB(lsTmp, DBsource);
	}

	/**
	 * 如果没搜到，就把geneUniID设定为accID
	 * 
	 * @param accID
	 * @param taxID
	 */
	protected GeneIDabs(String accID, int taxID) {
		addAccID(accID, taxID);
	}

	private void addAccID(String accID, int taxID) {
		List<AgeneUniID> lsAgeneUniID = getNCBIUniTax(accID, taxID);
		if (!lsAgeneUniID.isEmpty()) {			
			ageneUniID = lsAgeneUniID.get(0);
			ageneUniID.setAccIdRaw(accID);
			isAccID = false;
			if (lsAgeneUniID.size() > 1) {
				removeDot = false;
			}
		} else {
			ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_UNIID);
			ageneUniID.setAccID(accID);
			ageneUniID.setTaxID(taxID);
			updateGenUniID = true;
			isAccID = true;
		}
	}

	/**
	 * 根据输入的物种，返回该物种特定的那个ID，譬如水稻就返回LOCID 如果没有，则再搜索RefSeqRNA 如果再没有，则随便返回一个ID
	 * 
	 * @param lsAgenUniID
	 *            输入的list不能为空
	 * @param dbSource
	 * @return
	 */
	private AgeneUniID getGeneUniIDwithDB(List<AgeneUniID> lsAgenUniID, String dbSource) {
		if (lsAgenUniID == null || lsAgenUniID.size() == 0) {
			return null;
		}

		AgeneUniID ageneUniID = null;
		//如果是要找RefSeqRNA，那么注意要找NM的号
		if (dbSource.toLowerCase().equals(DBAccIDSource.RefSeqRNA.toString().toLowerCase())) {
			for (AgeneUniID ageneUniIDSub : lsAgenUniID) {
				if (ageneUniIDSub.getDataBaseInfo().getDbName().equals(DBAccIDSource.RefSeqRNA.toString())) {
					ageneUniID = ageneUniIDSub;
					if (ageneUniIDSub.getAccID().toLowerCase().startsWith("nm") || ageneUniIDSub.getAccID().toLowerCase().startsWith("xm")) {
						break;
					}
				}
			}
		} else {
			for (AgeneUniID ageneUniIDSub : lsAgenUniID) {
				if (ageneUniIDSub.getDataBaseInfo().getDbName().equals(dbSource)) {
					ageneUniID = ageneUniIDSub;
					break;
				} else if (ageneUniIDSub.getDataBaseInfo().getDbName().equals(DBAccIDSource.RefSeqRNA.toString())) {
					if (ageneUniID != null && (ageneUniIDSub.getAccID().toLowerCase().startsWith("nm") || ageneUniIDSub.getAccID().toLowerCase().startsWith("xm"))) {
						continue;
					}
					ageneUniID = ageneUniIDSub;
				}
			}
		}
		isAccID = false;
		if (ageneUniID != null) {
			return ageneUniID;
		} else {
			return lsAgenUniID.get(0);
		}
	}

	/**
	 * 某个物种的ID有其最特异的数据库来源
	 * 
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
	public String getGeneUniID() {
		if (isAccID) {
			return ageneUniID.getAccID();
		} else {
			return ageneUniID.getGenUniID();
		}
	}

	@Override
	public DBInfo getDBinfo() {
		return ageneUniID.getDataBaseInfo();
	}

	/**
	 * 有geneUniID就用geneUniID去query 否则就用accID去query
	 * 
	 * @param evalue
	 *            小于0就走默认 为1e-10
	 */
	@Override
	public void setBlastInfo(double evalue, int... StaxID) {
		if (blastList == null) {
			blastList = new BlastList(getIDtype(), getGeneUniID(), getTaxID());
		}
		blastList.setTaxIDBlastTo(StaxID);
		blastList.setEvalue_And_GetOneSeqPerTaxID(evalue, true);
	}

	/**
	 * 有geneUniID就用geneUniID去query 否则就用accID去query
	 * 
	 * @param evalue
	 *            小于0就走默认 为1e-10
	 */
	@Override
	public void setBlastInfo(double evalue, List<Integer> lsStaxID) {
		if (blastList == null) {
			blastList = new BlastList(getIDtype(), getGeneUniID(), getTaxID());
		}
		blastList.setTaxIDBlastTo(lsStaxID);
		blastList.setEvalue_And_GetOneSeqPerTaxID(evalue, true);
	}

	/**
	 * 单个物种的blast 获得本copedID blast到对应物种的第一个copedID，没有就返回null
	 * 
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	@Override
	public GeneID getGeneIDBlast() {
		List<GeneID> lsGeneIDs = getLsBlastGeneID();
		if (lsGeneIDs.size() != 0) {
			return lsGeneIDs.get(0);
		}
		return null;
	}

	/**
	 * blast多个物种 首先要设定blast的目标 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 给定一系列的目标物种的taxID，获得CopedIDlist 如果没有结果，返回一个空的lsResult
	 * 
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	@Override
	public List<GeneID> getLsBlastGeneID() {
		if (blastList == null) {
			blastList = new BlastList(getIDtype(), getGeneUniID(), getTaxID());
		}
		return blastList.getLsBlastGeneID();
	}

	/**
	 * blast多个物种 首先要设定blast的目标 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 
	 * @return 返回blast的信息，包括evalue等，该list和{@link #getLsBlastGeneID()}
	 *         得到的list是一一对应的
	 */
	public List<BlastInfo> getLsBlastInfos() {
		if (blastList == null) {
			blastList = new BlastList(getIDtype(), getGeneUniID(), getTaxID());
		}
		return blastList.getBlastInfo();
	}

	/**
	 * idType，必须是IDTYPE中的一种 不过在设定了lsRefAccID后，可以根据具体的lsRefAccID去查找数据库并确定idtype
	 */
	public int getIDtype() {
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
		String accID = ageneUniID.getAccID();
		if (removeDot) {
			String dbName = ageneUniID.getDataBaseInfo().getDbName().toLowerCase();
			if (!dbName.equals("symbol") && !dbName.equals("synonyms")) {
				accID = GeneID.removeDot(accID);
			}
		}
		return accID;
	}

	/**
	 * 具体的accID，根据其默认数据库情况抓一个出来 如果没有该数据库，则随便返回一个<br>
	 * 如果没有导入数据库，则返回自身
	 */
	public AgeneUniID getAccID_With_DefaultDB() {
		return getGenUniID(ageneUniID, getDatabaseType(ageneUniID.getTaxID()));
	}

	/**
	 * * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * 
	 * @param dbInfo
	 *            为null表示不指定dbinfo
	 * @return
	 */
	public AgeneUniID getAccIDDBinfo(String dbInfo) {
		return getGenUniID(ageneUniID, dbInfo);
	}

	/**
	 * 指定一个dbInfo，返回该dbInfo所对应的AgeneUniID，并且该AgeneUniID也仅对应一个geneUniID。
	 * 如果该dbinfo没有，则随便返回一个。
	 * 
	 * @param dbInfo
	 * @return
	 */
	protected AgeneUniID getGenUniID(AgeneUniID genUniID, String dbName) {
		if (getIDtype() == GeneID.IDTYPE_ACCID || (genUniID.getDataBaseInfo() != null && genUniID.getDataBaseInfo().getDbName().equals(dbName))) {
			return genUniID;
		}
		List<AgeneUniID> lsSubject = servNCBIUniID.findByGeneUniID(genUniID.getGeneIDtype(), genUniID.getGenUniID(), genUniID.getTaxID());
		return getGeneUniIDwithDB(lsSubject, dbName);
	}

	public int getTaxID() {
		return ageneUniID.getTaxID();
	}

	/**
	 * 返回geneinfo信息
	 * 
	 * @return
	 */
	public AGeneInfo getGeneInfo() {
		setGenInfo();
		return geneInfo;
	}

	/**
	 * 获得该基因的description
	 * 
	 * @return
	 */
	public String getDescription() {
		getGeneInfo();
		if (geneInfo == null) {
			return "";
		}
		geneInfo.setDBinfo(getDatabaseType(getTaxID()));
		return geneInfo.getDescrp();
	}

	/**
	 * 获得该基因的symbol
	 * 
	 * @return
	 */
	public String getSymbol() {
		String symbol = "";
		setGenInfo();
		if (geneInfo != null) {
			symbol = geneInfo.getSymb();
		}

		if (symbol == null || symbol.equals("")) {
			AgeneUniID geneUniId = getGenUniID(ageneUniID, getDatabaseType(ageneUniID.getTaxID()));
			if (geneUniId != null) {
				symbol = geneUniId.getAccID();
			} else {
				logger.error(" cannot find gene symbol of {}", getAccID());
				symbol = getAccID();
			}
		}
		return symbol;
	}

	/** 设定geneInfo信息 */
	protected void setGenInfo() {
		if (geneInfo == null && !isAccID) {
			try {
				geneInfo = servGeneInfo.queryGeneInfo(getIDtype(), ageneUniID.getGenUniID(), getTaxID());
			} catch (Exception e) {
				return;
			}
			
		}
	}

	/**
	 * 先设定blast的情况 如果blast * 0:symbol 1:description 2:subjectSpecies 3:evalue
	 * 4:symbol 5:description 如果不blast 0:symbol 1:description
	 * 
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
			if (getLsBlastGeneID() != null && getLsBlastInfos() != null && getLsBlastInfos().size() > 0) {

				for (int i = 0; i < getLsBlastInfos().size(); i++) {
					int subTaxID = getLsBlastInfos().get(i).getSubjectTax();
					Species species = new Species(subTaxID);
					if (tmpAnno[2] == null || tmpAnno[2].trim().equals("")) {
						tmpAnno[2] = species.getCommonName();
						tmpAnno[3] = getLsBlastInfos().get(i).getEvalue() + "";
						tmpAnno[4] = getLsBlastGeneID().get(i).getSymbol();
						tmpAnno[5] = getLsBlastGeneID().get(i).getDescription();
					} else {
						tmpAnno[2] = tmpAnno[2] + "//" + species.getCommonName();
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
	 * 
	 * @return
	 */
	public GOInfoAbs getGOInfo() {
		if (goInfoAbs == null) {
			initGoInfoAbs();
		}
		return goInfoAbs;
	}

	private void initGoInfoAbs() {
		int idtype;
		String genUniID;
		if (isAccID) {
			idtype = GeneID.IDTYPE_ACCID;
			genUniID = ageneUniID.getAccID();
		} else {
			idtype = ageneUniID.getGeneIDtype();
			genUniID = ageneUniID.getGenUniID();
		}		
		goInfoAbs = GOInfoAbs.createGOInfoAbs(idtype, genUniID, ageneUniID.getTaxID());
	}

	/**
	 * 返回该CopedID所对应的Gene2GOInfo <br>
	 * GO_BP<br>
	 * GO_CC<br>
	 * GO_MF<br>
	 * GO_ALL<br>
	 * 
	 * @param GOType
	 * @return
	 */
	public List<AGene2Go> getGene2GO(GOtype GOType) {
		return getGOInfo().getLsGene2Go(GOType);

		
	}

	/**
	 * blast多个物种 首先设定blast的物种 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的GoInfo
	 */
	public List<AGene2Go> getGene2GOBlast(GOtype GOType) {
		List<GOInfoAbs> lsGoInfo = new ArrayList<GOInfoAbs>();

		List<GeneID> lsBlastGeneIDs = getLsBlastGeneID();
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
	public List<KGentry> getKegEntity(boolean blast) {
		getKeggInfo();
		if (!blast) {
			return keggInfo.getKgGentries();
		} else {
			List<GeneID> lsGeneIDsBlast = getLsBlastGeneID();
			ArrayList<KeggInfoAbs> lsKeggInfos = new ArrayList<>();
			lsKeggInfos.add(getKeggInfo());
			for (GeneID copedID : lsGeneIDsBlast) {
				lsKeggInfos.add(copedID.getKeggInfo());
			}
			return KeggInfo.getLsKgGentries(lsKeggInfos);
		}
	}

	/**
	 * 获得相关的Kegg信息
	 * 
	 * @return
	 */
	public KeggInfoAbs getKeggInfo() {
		if (keggInfo != null) {
			return keggInfo;
		}
		int idType = getIDtype();
		String geneUniAccId = ageneUniID.getGenUniID();
		if (idType == GeneID.IDTYPE_ACCID) {
			geneUniAccId = ageneUniID.getAccID();
		}
		keggInfo = KeggInfoAbs.createInstance(idType, geneUniAccId, ageneUniID.getTaxID());
		return keggInfo;
	}

	/**
	 * blast多个物种 首先设定blast的物种 用方法： setBlastInfo(double evalue, int... StaxID)
	 * 获得经过blast的KegPath 没有就返回空的list
	 */
	@Override
	public ArrayList<KGpathway> getKegPath(boolean blast) {
		getKeggInfo();
		if (blast) {
			ArrayList<KeggInfoAbs> lskeggInfo = new ArrayList<>();
			lskeggInfo.add(keggInfo);
			List<GeneID> lsBlastCopedIDs = getLsBlastGeneID();
			for (GeneID copedID : lsBlastCopedIDs) {
				lskeggInfo.add(copedID.getKeggInfo());
			}
			return KeggInfo.getLsKegPath(lskeggInfo, getTaxID());
		} else {
			return keggInfo.getLsKegPath();
		}
	}

	/**
	 * 添加可能用于升级数据库的ID 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
	 */
	@Override
	public void addUpdateRefAccID(String... refAccID) {
		for (String tmpRefID : refAccID) {
			if (tmpRefID == null) {
				continue;
			}
			lsRefAccID.add(tmpRefID);
		}
	}

	/**
	 * 设置可能用于升级数据库的ID 譬如获得一个ID与NCBI的别的ID有关联，就用别的ID来查找数据库，以便获得该accID所对应的genUniID
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
	public void setUpdateRefAccID(List<String> lsRefAccID) {
		this.lsRefAccID.clear();
		for (String tmpRefID : lsRefAccID) {
			if (tmpRefID == null) {
				continue;
			}
			this.lsRefAccID.add(tmpRefID);
		}
	}

	/**
	 * 记录该ID的物种ID和数据库信息，用于修正以前的数据库
	 * 
	 * @param taxIDfile
	 * @param DBInfo
	 * @param 是否用本DBInfo修正以前的DBInfo
	 *            不管是true还是false，geneinfo都会用其进行修正
	 */
	@Override
	public void setUpdateDBinfo(DBAccIDSource DBInfo, boolean overlapDBinfo) {
		if (DBInfo != null) {
			if (ageneUniID.setDataBaseInfo(DBInfo.toString()) && overlapDBinfo) {
				updateGenUniID = true;
			}
		}
		this.overrideUpdateDBinfo = overlapDBinfo;
	}

	/**
	 * 记录该ID的物种ID和数据库信息，用于修正以前的数据库
	 * 
	 * @param taxIDfile
	 * @param DBInfo
	 * @param 是否用本DBInfo修正以前的DBInfo
	 *            不管是true还是false，geneinfo都会用其进行修正
	 */
	public void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo) {
		if (DBInfo != null && !DBInfo.equals("")) {
			if (ageneUniID.setDataBaseInfo(DBInfo.toString()) && overlapDBinfo) {
				updateGenUniID = true;
			}
		}
		this.overrideUpdateDBinfo = overlapDBinfo;
	}

	/**
	 * 输入已知的geneUniID和IDtype
	 * 
	 * @param geneUniID
	 * @param idType
	 *            必须是CopedID.IDTYPE_GENEID等，必须输入
	 */
	@Override
	public void setUpdateGeneID(String geneUniID, int idType) {
		if (!isAccID && ageneUniID.getGeneIDtype() == idType && ageneUniID.getGenUniID() != null && ageneUniID.getGenUniID().equals(geneUniID)) {
			return;
		}
		if (geneUniID != null && !geneUniID.trim().equals("") && !geneUniID.trim().equals("0")) {
			AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(idType);
			ageneUniID.setDataBaseInfo(this.ageneUniID.getDataBaseInfo());
			ageneUniID.setAccID(this.ageneUniID.getAccID());
			ageneUniID.setGenUniID(geneUniID);
			ageneUniID.setTaxID(this.getTaxID());

			this.ageneUniID = ageneUniID;
			this.isAccID = false;
			updateGenUniID = true;
		}
	}

	/** 设定该ID的accID */
	@Override
	public void setUpdateAccID(String accID) {
		setUpdateAccID(accID, true);
	}

	/** 设定该ID的accID，不经过处理的ID */
	@Override
	public void setUpdateAccIDNoCoped(String accID) {
		setUpdateAccID(accID, false);
	}

	/**
	 * 设定该ID的accID
	 * 
	 * @param accID
	 * @param coped
	 *            是否经过处理
	 */
	private void setUpdateAccID(String accID, boolean coped) {
		if (accID == null || accID.equals("")) return;
		if (ageneUniID.getAccID() != null && ageneUniID.getAccID().equalsIgnoreCase(accID)) {
			return;
		}

		ageneUniID.setAccID(accID, coped);
		ageneUniID.setId(null);
		updateGenUniID = true;
	}

	/**
	 * 依次输入需要升级的GO信息，最后升级 这里只是先获取GO的信息，最后调用升级method的时候再升级 可以连续不断的添加
	 * 
	 * @param GOID
	 *            必填
	 * @param GOdatabase
	 *            没有就设置为 null
	 * @param GOevidence
	 *            没有就设置为 null
	 * @param GORef
	 *            没有就设置为 null
	 * @param gOQualifiy
	 *            没有就设置为 null
	 */
	@Override
	public void addUpdateGO(String GOID, DBAccIDSource GOdatabase, String GOevidence, List<String> GORef, String gOQualifiy) {
		getGOInfo().addGOid(getTaxID(), GOID, GOdatabase, GOevidence, GORef, gOQualifiy);
	}

	/**
	 * 依次输入需要升级的GO信息，最后升级 这里只是先获取GO的信息，最后调用升级method的时候再升级 可以连续不断的添加
	 */
	@Override
	public void addUpdateGO(AGene2Go aGene2Go) {
		getGOInfo().addGene2GO(aGene2Go);
	}

	/**
	 * 输入需要update的geneInfo，注意不需要设定geneUniID 但是需要设定
	 * 
	 * @param geneInfo
	 */
	@Override
	public void setUpdateGeneInfo(AGeneInfo geneInfo) {
		this.geneInfo = geneInfo;
	}

	/**
	 * 如果Subject没有在数据库中记录，就不升级 可以连续不断的添加
	 * 
	 * @param blastInfo
	 */
	@Override
	public void addUpdateBlastInfo(BlastInfo blastInfo) {
		if (blastInfo.getSubjectIDtype() == GeneID.IDTYPE_ACCID) {
			return;
		}
		if (blastList == null) {
			// blastList = new BlastList(getIDtype(), getGeneUniID(),
			// getTaxID());
			if (isAccID) {
				blastList = new BlastList(GeneID.IDTYPE_ACCID, ageneUniID.getAccID(), getTaxID());
			} else {
				blastList = new BlastList(ageneUniID.getGeneIDtype(), ageneUniID.getGenUniID(), getTaxID());
			}
		}
		blastList.addBlastInfoNew(blastInfo);
	}

	/**
	 * 如果新的ID不加入UniID，那么就写入指定的文件中 文件需要最开始用set指定 只要有一个错误就会返回
	 * 
	 * @param updateUniID
	 */
	@Override
	public boolean update(boolean updateUniID) {
		setUpdateGenUniID();
		lsRefAccID.clear();
		if (!updateUniID && getIDtype() == GeneID.IDTYPE_ACCID) {
			return false;
		}
		if (!ageneUniID.isValidGenUniID()) {
			logger.error("geneID为0，请check: " + ageneUniID.getAccID());
			return false;
		}

		if (!updateGeneID(updateUniID)) {
			return false;
		}
		if (!updateGeneInfo()) {
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
			if (goInfoAbs == null) {
				return true;
			}
			GOInfoAbs goInfoAbsNew = GOInfoAbs.createGOInfoAbs(getIDtype(), getAgeneUniID().getGenUniID(), getTaxID());
			goInfoAbsNew.addGOinfo(goInfoAbs);
			goInfoAbs = goInfoAbsNew;
			goInfoAbs.update();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 根据输入的geneUniID--中的geneID，升级AccID和DBinfo 升级geneID数据库，并且将geneUniID按照数据库进行重置
	 * <b>只升级第一个获得的geneID</b> 如果accID没有，则不升级
	 * 
	 * @param updateUniID
	 *            如果在数据库中没有找到对应的ID ，是否将ID导入UniID库 true,导入uniID库，并且重置idtype
	 * @throws EOFException
	 */
	private boolean updateGeneID(boolean updateUniID) {
		if (!updateGenUniID || ageneUniID.getAccID() == null || ageneUniID.getAccID().equals("")) {
			return true;
		}

		if (!ageneUniID.isValidGenUniID()) {
			return false;
		}
		boolean update = false;
		if (ageneUniID.getDataBaseInfo() == null || ageneUniID.getDataBaseInfo().getDbName().equals("")) {
			logger.info("升级geneID时没有设置该gene的数据库来源，自动设置为NCBIID");
			if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
				ageneUniID.setDataBaseInfo(DBAccIDSource.NCBI.toString());
			} else {
				ageneUniID.setDataBaseInfo(DBAccIDSource.Uniprot.toString());
			}
		}
		if (getIDtype() != GeneID.IDTYPE_ACCID) {
			update = ageneUniID.update(overrideUpdateDBinfo);
		} else if (updateUniID) {
			AgeneUniID uniProtID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_UNIID);
			uniProtID.setAccID(ageneUniID.getAccID());
			uniProtID.setDataBaseInfo(ageneUniID.getDataBaseInfo());
			uniProtID.setGenUniID(ageneUniID.getGenUniID());
			uniProtID.setTaxID(ageneUniID.getTaxID());
			update = uniProtID.update(overrideUpdateDBinfo);
			if (ageneUniID instanceof NCBIID) {
				logger.error("出错，升级的是uniprot数据库，但是该ID是NCBIID：" + ageneUniID.getGenUniID() + "\t" + ageneUniID.getAccID() + "\t"
						+ ageneUniID.getTaxID());
			}
			isAccID = false;
		} else {
			return false;
		}
		return update;
	}

	/**
	 * 根据geneID和idType升级相关的geneInfo 注意，geneInfo只能是单个，不能是合并过的geneInfo
	 * 如果没有genUniID，或者没有搜索到对应的genID，则返回false； 如果没有geneInfo信息，则认为不需要升级，返回true
	 */
	private boolean updateGeneInfo() {
		if (!ageneUniID.isValidGenUniID()) {
			return false;
		}
		if (geneInfo == null) {
			return true;
		}
		geneInfo.setTaxID(getTaxID());
		if (geneInfo.getDbInfo() == null) {
			if (getDBinfo() == null) {
				if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
					geneInfo.setDBinfo(manageDBInfo.findByDBname(DBAccIDSource.NCBI.toString()));
				} else {
					geneInfo.setDBinfo(manageDBInfo.findByDBname(DBAccIDSource.Uniprot.toString()));
				}
			} else {
				geneInfo.setDBinfo(getDBinfo());
			}
		}

		try {
			if (!servGeneInfo.updateGenInfo(getIDtype(), ageneUniID.getGenUniID(), getTaxID(), geneInfo)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		boolean update = updateGeneInfoSymbolAndSynonyms(getIDtype(), geneInfo);
		geneInfo = null;
		return update;
	}

	private boolean updateGeneInfoSymbolAndSynonyms(int idType, AGeneInfo geneInfoUpdate) {
		boolean update = true;
		AgeneUniID ageneUniID = null;
		String symbol = geneInfoUpdate.getSymb();
		if (!symbol.equals("")) {
			String ssymb = geneInfoUpdate.getSymb();
			ageneUniID = AgeneUniID.creatAgeneUniID(idType);
			ageneUniID.setAccID(ssymb.trim());
			ageneUniID.setDataBaseInfo(DBAccIDSource.Symbol.toString());
			ageneUniID.setGenUniID(this.ageneUniID.getGenUniID());
			ageneUniID.setTaxID(getTaxID());
			update = ageneUniID.update(true);
		}
		if (geneInfoUpdate.getSynonym() != null && geneInfoUpdate.getSynonym().size() > 0) {
			for (String string : geneInfoUpdate.getSynonym()) {
				if (string == null || string.equals("")) {
					continue;
				}
				ageneUniID = AgeneUniID.creatAgeneUniID(idType);
				ageneUniID.setAccID(string.trim());
				ageneUniID.setDataBaseInfo(DBAccIDSource.Synonyms.toString());
				ageneUniID.setGenUniID(this.ageneUniID.getGenUniID());
				ageneUniID.setTaxID(getTaxID());
				update = update && ageneUniID.update(true);
			}
		}
		return update;
	}

	private boolean updateBlastInfo() {
		if (blastList != null) {
			if (blastList.genUniID != getGeneUniID() || blastList.getIdType() != getIDtype()) {
				blastList.setGeneInfo(getIDtype(), getGeneUniID(), getTaxID());
			}
			try {
				blastList.update();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	// /////////////////////// 升级 uniGene 的信息
	/**
	 * 根据lsRefAccID的信息设定geneUniID和IDtype 获得所对应的geneUniID<br>
	 * 如果没搜到则不动<br>
	 * 内部已经将genUniID和IDType和TaxID都改过来了
	 */
	private void setUpdateGenUniID() {
		if (!isAccID) {
			return;
		}
		// 保存所有refID--也就是用于查找数据库的refxDB的信息ID，他们所对应的geneUniID
		ArrayList<List<AgeneUniID>> lsgeneID = new ArrayList<List<AgeneUniID>>();
		ArrayList<List<AgeneUniID>> lsgeneIDUniProt = new ArrayList<List<AgeneUniID>>();
		for (String refAccID : lsRefAccID) {
			List<AgeneUniID> lsTmpGenUniID = getNCBIUniTax(refAccID, this.ageneUniID.getTaxID());
			if (lsTmpGenUniID.size() == 0) {
				continue;
			} else if (lsTmpGenUniID.size() == 1 && lsTmpGenUniID.get(0).getGeneIDtype().equals(GeneID.IDTYPE_GENEID)) {
				AgeneUniID ageneUniID = lsTmpGenUniID.get(0);
				ageneUniID.setAccID(this.ageneUniID.getAccID());
				ageneUniID.setDataBaseInfo(this.ageneUniID.getDataBaseInfo());
				ageneUniID.setId(null);
				this.ageneUniID = ageneUniID;
				this.isAccID = false;
				return;
			} else if (lsTmpGenUniID.get(0).getDataBaseInfo().getDbOrg().equals("UniProt")) {
				lsgeneIDUniProt.add(lsTmpGenUniID);
			} else {
				lsgeneID.add(lsTmpGenUniID);
			}
		}

		// 如果ref所对应的NCBI的geneID超过两个，就需要check了
		if (lsgeneID.size() == 0 && lsgeneIDUniProt.size() > 0) {
			logger.error("出现奇怪的ID，请check：" + getAccID());
			lsgeneID.addAll(lsgeneIDUniProt);
		}

		// 挑选出含有geneUniID的geneID
		Collections.sort(lsgeneID, new Comparator<List<AgeneUniID>>() {
			public int compare(List<AgeneUniID> o1, List<AgeneUniID> o2) {
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
			}
			this.isAccID = true;
			return;
		}
		AgeneUniID ageneUniID = lsgeneID.get(0).get(0);
		if (this.ageneUniID.getAccID() != null) {
			ageneUniID.setAccID(this.ageneUniID.getAccID());
			ageneUniID.setDataBaseInfo(this.ageneUniID.getDataBaseInfo());
			ageneUniID.setId(null);
		}
		this.ageneUniID = ageneUniID;
		this.isAccID = false;
	}

	// ///////////////// static method ///////////////////////
	/**
	 * 给定一个accessID，搜索数据库 返回list-AgeneUniID
	 * 如果同一个物种得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目 如果没搜到，则返回空的list
	 * 
	 * @param accID
	 *            输入的accID,没有内置去空格去点
	 * @param taxID
	 *            物种ID，如果不知道就设置为0，只要不是symbol都可以为0
	 * @return list-string[] 0: IDtype 1: geneUniID 2: taxID
	 */
	protected static List<AgeneUniID> getNCBIUniTax(String accID, int taxID) {
		ArrayList<AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (accID == null || accID.equals("") || taxID <= 0) {
			return lsResult;
		}

		List<AgeneUniID> lsAgeneUniIDs = new ArrayList<AgeneUniID>();
		for (int i = 0; i < 2; i++) {
			try {
				lsAgeneUniIDs = servNCBIUniID.findByAccID(GeneID.IDTYPE_GENEID, accID, taxID);
				break;
			} catch (Exception e) {
				if (i == 0) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
					}
					continue;
				} else if (i == 1) {
					e.printStackTrace();
				}
			}
		}

		if (lsAgeneUniIDs.size() == 0) {
			lsAgeneUniIDs = servNCBIUniID.findByAccID(GeneID.IDTYPE_UNIID, accID, taxID);
		}
		return getLsGeneIDinfo(lsAgeneUniIDs);
	}

	/**
	 * 指定一系列的NCBIID或者UniID，进行过滤， 主要是过滤
	 * 如果同一个物种得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目 如果含有RefSeqRNA的ID，优先返回那一个
	 */
	private static List<AgeneUniID> getLsGeneIDinfo(List<AgeneUniID> lsNcbiids) {
		if (lsNcbiids.size() == 0) {
			return new ArrayList<AgeneUniID>();
		}
		if (lsNcbiids.size() == 1) {
			return lsNcbiids;
		}
		List<AgeneUniID> lsGeneIDinfo = new ArrayList<AgeneUniID>();
		List<AgeneUniID> lsGeneIDinfoNCBI = new ArrayList<AgeneUniID>();
		for (AgeneUniID geneUniID : lsNcbiids) {
			if (geneUniID.getDataBaseInfo().getDbName().equalsIgnoreCase(DBAccIDSource.Synonyms.toString())) {
				continue;
			}
			if (geneUniID.getDataBaseInfo().getDbName().equalsIgnoreCase(DBAccIDSource.RefSeqRNA.toString())) {
				lsGeneIDinfoNCBI.add(geneUniID);
			}
			lsGeneIDinfo.add(geneUniID);
		}

		if (lsGeneIDinfoNCBI.size() > 0) {
			return lsGeneIDinfoNCBI;
		}

		if (lsGeneIDinfo.size() == 0) {
			lsGeneIDinfo.addAll(lsNcbiids);
		}
		return lsGeneIDinfo;
	}

	/**
	 * 只要两个ncbiid的geneID相同，就认为这两个NCBIID相同
	 * 但是如果geneID为0，也就是NCBIID根本没有初始化，那么直接返回false
	 * 
	 * @Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		GeneIDabs otherObj = (GeneIDabs) obj;
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
