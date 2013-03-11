package com.novelbio.database.model.modgeneid;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ServBlastInfo;
import com.novelbio.database.service.servgeneanno.ServGene2Go;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServUniGene2Go;
import com.novelbio.database.service.servgeneanno.ServUniGeneInfo;
import com.novelbio.database.service.servgeneanno.ServUniProtID;

public abstract class GeneIDabs implements GeneIDInt {
	private static Logger logger = Logger.getLogger(GeneIDabs.class);
	static HashMap<Integer, DBAccIDSource> hashDBtype = new HashMap<Integer, DBAccIDSource>();
	
	/**  物种id  */
	int taxID = 0;
	/**  idType，必须是IDTYPE中的一种 */
	String idType = GeneID.IDTYPE_ACCID;
	/** 具体的accID */
	String accID = null;
	String genUniID = "";
	String symbol = null;
	/** 譬方和多个物种进行blast，然后结合这些物种的信息，取并集 */
	ArrayList<BlastInfo> lsBlastInfos = null;
	double evalue = 10;
	AGeneInfo geneInfo = null;
	KeggInfo keggInfo;
	GOInfoAbs goInfoAbs = null;
	/** 设定是否将blast的结果进行了查找 */
	boolean isBlastedFlag = false;
	ArrayList<GeneID> lsBlastGeneID = new ArrayList<GeneID>();
	boolean overrideUpdateDBinfo = false;
	String geneIDDBinfo;
	// //////////////////// service 层
	ServBlastInfo servBlastInfo = new ServBlastInfo();
	ServNCBIID servNCBIID = new ServNCBIID();
	ServUniProtID servUniProtID = new ServUniProtID();
	ServGeneInfo servGeneInfo = new ServGeneInfo();
	ServUniGeneInfo servUniGeneInfo = new ServUniGeneInfo();
	ServGene2Go servGene2Go = new ServGene2Go();
	ServUniGene2Go servUniGene2Go = new ServUniGene2Go();
	// ///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 如果输入的是accID，那么返回该accID对应的数据库
	 * 如果没有则返回null
	 * @return
	 */
	@Override
	public String getDBinfo() {
		return this.geneIDDBinfo;
	}
	/**
	 * 该物种的symbol应该是属于哪个数据库
	 * @return
	 */
	private DBAccIDSource getDatabaseTyep() {
		if (hashDBtype.size() == 0) {
			hashDBtype.put(39947, DBAccIDSource.TIGR_rice);
			hashDBtype.put(10090, DBAccIDSource.RefSeqRNA);
			hashDBtype.put(3702, DBAccIDSource.TAIR_ATH);
			hashDBtype.put(3847, DBAccIDSource.SOYBASE);
			hashDBtype.put(4102, DBAccIDSource.PlantGDB);
		}
		DBAccIDSource result = hashDBtype.get(taxID);
		if (result == null) {
			return DBAccIDSource.RefSeqRNA;
		}
		return result;
	}
	/**
	 * 设定多个物种进行blast 每次设定后都会刷新
	 * @param evalue
	 * @param StaxID
	 */
	@Override
	public abstract void setBlastInfo(double evalue, int... StaxID);
	
	protected void addLsBlastInfo(BlastInfo blastInfo) {
		if (blastInfo != null)
			lsBlastInfos.add(blastInfo);
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
	 * 获得设定的第一个blast的对象，首先要设定blast的目标
	 * @param blastInfo
	 * @return
	 */
	private GeneID getBlastGeneID(BlastInfo blastInfo) {
		if (blastInfo == null) return null;
		
		String idType = blastInfo.getSubjectTab();
		GeneID copedID = new GeneID(idType, blastInfo.getSubjectID(), blastInfo.getSubjectTax());
		return copedID;
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
		if (isBlastedFlag) {
			return lsBlastGeneID;
		}
		isBlastedFlag = true;
		if (lsBlastInfos == null || lsBlastInfos.size() == 0) {
			return new ArrayList<GeneID>();
		}
		for (BlastInfo blastInfo : lsBlastInfos) {
			GeneID copedID = getBlastGeneID(blastInfo);
			if (copedID != null) {
				lsBlastGeneID.add(copedID);
			}
		}
		return lsBlastGeneID;
	}

	/**
	 * blast多个物种 首先要设定blast的目标 用方法： setBlastInfo(double evalue, int... StaxID)
	 * @return 返回blast的信息，包括evalue等，该list和getCopedIDLsBlast()得到的list是一一对应的
	 */
	public ArrayList<BlastInfo> getLsBlastInfos() {
		return lsBlastInfos;
	}
	// ////////////////// normal setting
	/**
	 * idType，必须是IDTYPE中的一种
	 * 不过在设定了lsRefAccID后，可以根据具体的lsRefAccID去查找数据库并确定idtype
	 */
	public String getIDtype() {
		if (lsRefAccID == null || lsRefAccID.size() == 0) {
			return this.idType;
		}
		AgeneUniID ageneUniID = getUpdateGenUniID();
		if (ageneUniID == null) {
			return GeneID.IDTYPE_ACCID;
		}
		return ageneUniID.getGeneIDtype();
	}
	/**
	 * 具体的accID，如果没有则根据物种随机抓一个出来
	 */
	public String getAccID() {
		if (accID == null) {
			accID = getAccIDDBinfo(getDatabaseTyep());
		}
		return this.accID;
	}
	
	/**
	 * 具体的accID，根据数据库情况抓一个出来
	 */
	public String getAccIDDBinfo() {
		 String accID = getAccIDDBinfo(getDatabaseTyep());
		 if (accID == null) {
			 accID = getAccID();
		 }
		 return accID;
	}
	
	/**
	 * * 指定一个dbInfo，返回该dbInfo所对应的accID，没有则返回null
	 * @param dbInfo 为null表示不指定dbinfo
	 * @return
	 */
	public String getAccIDDBinfo(DBAccIDSource dbInfo) {
		AgeneUniID genuniID = getGenUniID(getGenUniID(), dbInfo);
		if (genuniID != null) {
			return genuniID.getAccID();
		}
		return null;
	}
	public String getGenUniID() {
		return this.genUniID;
	}
	
	public int getTaxID() {
		return taxID;
	}
	/**
	 * 获得该基因的description
	 * @return
	 */
	public String getDescription() {
		setSymbolDescrip();
		if (geneInfo == null || geneInfo.getDescrp() == null) {
			return "";
		}
		return geneInfo.getDescrp().replaceAll("\"", "");
	}
	/**
	 * 获得该基因的symbol
	 * @return
	 */
	public String getSymbol() {
		setSymbolDescrip();
		return symbol;
	}

	/**
	 * 设定geneInfo信息
	 */
	protected abstract void setGenInfo();
	protected void setSymbolDescrip() {
		if (geneInfo != null || symbol != null) {
			return;
		}
		setGenInfo();
		if (geneInfo == null) {
			symbol = getGenName(getGenUniID(), getDatabaseTyep());
		} else {
			geneInfo.setTaxID(taxID);
			symbol = geneInfo.getSymb();
		}
		if (symbol == null || symbol.equals("")) {
			symbol = getGenName(getGenUniID(), getDatabaseTyep());
		}
		symbol = symbol.replace("@", "");
	}

	/**
	 * 给定基因的NCBIgeneID，和databaseType，获得 accID. 如果databaseType == null 或 “”
	 * 那就随便选一个accID 如果geneID是0，返回""
	 * 
	 * @return
	 */
	protected String getGenName(String genUniID,DBAccIDSource databaseType) {
		AgeneUniID ageneUniID = getGenUniID(genUniID, databaseType);
		if (ageneUniID == null) {
			return "";
		} else {
			return ageneUniID.getAccID();
		}
	}

	/**
	 * * 指定一个dbInfo，返回该dbInfo所对应的AgeneUniID，并且该AgeneUniID也仅对应一个geneUniID。
	 * 如果该dbinfo没有，则随便返回一个。
	 * @param dbInfo
	 * @return
	 */
	protected abstract AgeneUniID getGenUniID(String genUniID, DBAccIDSource dbInfo);
	
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
	// ////////////////////////////////GOInfo
	/**
	 * 设定 goInfoAbs的信息
	 */
	protected abstract void setGoInfo();
	/**
	 * 返回geneinfo信息
	 * @return
	 */
	public AGeneInfo getGeneInfo() {
		setGenInfo();
		return geneInfo;
	}
	/**
	 * 返回该基因所对应的GOInfo信息，不包含Blast
	 * @return
	 */
	public GOInfoAbs getGOInfo() {
		if (goInfoAbs == null) {
			setGoInfo();
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
	public ArrayList<AGene2Go> getGene2GOBlast(GOtype GOType) {
		setGoInfo();
		ArrayList<GOInfoAbs> lsGoInfo = new ArrayList<GOInfoAbs>();

		ArrayList<GeneID> lsBlastGeneIDs = getLsBlastGeneID();
		for (GeneID geneID : lsBlastGeneIDs) {
			lsGoInfo.add(geneID.getGOInfo());
		}
		return getGOInfo().getLsGen2Go(lsGoInfo, GOType);
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
		keggInfo = new KeggInfo(idType, genUniID, taxID);
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
	Boolean uniqID = null;
	/**
	 * 在采用refaccID作为参照进行升级ID的时候，是否必须是uniqID
	 * @param uniqID	用给定的参考ID能找到数据库中的唯一基因
	 * true：只有当uniqID时才升级
	 * null：默认参数--非uniqID也升级，不过只升级第一个基因
	 * false：非uniqID也升级，升级搜索到的全部ID，该功能尚未实现
	 */
	@Override
	public void setUpdateRefAccIDClear(Boolean uniqID) {
		this.uniqID = uniqID;
	}
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
			this.geneIDDBinfo = DBInfo.toString();
		}
		this.overrideUpdateDBinfo = overlapDBinfo;
	}
	/**
	 * 输入已知的geneUniID和IDtype
	 * 
	 * @param geneUniID
	 * @param idType
	 *            必须是CopedID.IDTYPE_GENEID等，可以不输入
	 */
	@Override
	public void setUpdateGeneID(String geneUniID, String idType) {
		if (geneUniID != null && !genUniID.trim().equals("")
				&& !genUniID.trim().equals("0")) {
			this.genUniID = geneUniID;
		}
		if (idType == null || idType.trim().equals("")) {
			return;
		}
		this.idType = idType;
	}
	/**
	 * 设定该ID的accID
	 */
	@Override
	public void setUpdateAccID(String accID) {
		this.accID = GeneID.removeDot(accID);
	}
	/**
	 * 设定该ID的accID，不经过处理的ID
	 */
	@Override
	public void setUpdateAccIDNoCoped(String accID) {
		this.accID = accID;
	}
	
	/** 记录升级的GO信息的，每次升级完毕后都清空 */
	ArrayList<Gene2Go> lsGOInfoUpdate = new ArrayList<Gene2Go>();

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
	public void setUpdateGO(String GOID, DBAccIDSource GOdatabase, String GOevidence,
			String GORef, String gOQualifiy) {
		if (GOID == null) {
			return;
		}
		GOID = GOID.trim();
		if (GOID.equals("")) {
			return;	
		}
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGOID(GOID);
		gene2Go.setTaxID(taxID);
		gene2Go.setEvidence(GOevidence);
		gene2Go.setDataBase(GOdatabase.toString());
		gene2Go.setQualifier(gOQualifiy);
		gene2Go.setReference(GORef);
		lsGOInfoUpdate.add(gene2Go);
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
	 * 
	 * 如果没有QueryID, SubjectID, taxID中的任何一项，就不升级 如果evalue>50 或 evalue<0，就不升级
	 * 可以连续不断的添加
	 * @param blastInfo
	 */
	@Override
	public void setUpdateBlastInfo(String SubAccID, String subDBInfo, int SubTaxID, double evalue, double identities) {
		if (genUniID == null || genUniID.equals("")) {
			return;
		}
		BlastInfo blastInfo = new BlastInfo(null, 0, SubAccID, SubTaxID);
		if (blastInfo.getSubjectTab().equals(GeneID.IDTYPE_ACCID)) {
			logger.error("没有该blast的accID："+SubAccID);
			return;
		}
		blastInfo.setQueryID(genUniID);
		blastInfo.setQueryTax(getTaxID());
		blastInfo.setEvalue_Identity(evalue, identities);
		blastInfo.setQueryDB_SubDB(this.geneIDDBinfo, subDBInfo);
		if (lsBlastInfosUpdate == null) {
			lsBlastInfosUpdate = new ArrayList<BlastInfo>();
		}
		lsBlastInfosUpdate.add(blastInfo);
	}
	/**
	 * 
	 * 如果没有QueryID, SubjectID, taxID中的任何一项，就不升级 如果evalue>50 或 evalue<0，就不升级
	 * 可以连续不断的添加
	 * @param blastInfo
	 */
	@Override
	public void setUpdateBlastInfo(String SubGenUniID, String subIDtype, String subDBInfo, int SubTaxID, double evalue, double identities) {
		if (genUniID == null || genUniID.equals("")) {
			return;
		}
		BlastInfo blastInfo = new BlastInfo(null, 0, SubGenUniID, subIDtype, SubTaxID);
		if (blastInfo.getSubjectTab().equals(GeneID.IDTYPE_ACCID)) {
			logger.error("没有该blast的geneUniID："+SubGenUniID);
		}
		blastInfo.setQueryID(genUniID);
		blastInfo.setQueryTax(getTaxID());
		blastInfo.setEvalue_Identity(evalue, identities);
		blastInfo.setQueryDB_SubDB(this.geneIDDBinfo, subDBInfo);
		if (lsBlastInfosUpdate == null) {
			lsBlastInfosUpdate = new ArrayList<BlastInfo>();
		}
		lsBlastInfosUpdate.add(blastInfo);
	}
	/**
	 * 如果新的ID不加入UniID，那么就写入指定的文件中 文件需要最开始用set指定
	 * 
	 * @param updateUniID
	 */
	@Override
	public boolean update(boolean updateUniID) {
		AgeneUniID geneUniID = getUpdateGenUniID();
		if (!updateUniID && geneUniID == null) {
			return false;
		}
		if (geneUniID != null && geneUniID.getGeneIDtype().equals(GeneID.IDTYPE_GENEID) && geneUniID.getGenUniID().equals("0")) {
			logger.error("geneID为0，请check");
			return false;
		}
		boolean flag1 = false;
		try {
			flag1 = updateGeneID(geneUniID, updateUniID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		boolean flag2 = updateGeneInfo();
		boolean flag3 = false;
		try {
			flag3 = updateGene2Go();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean flag4 = updateBlastInfo();
		return flag1&&flag2&&flag3&&flag4;
	}

	/** 升级GO数据库 */
	private boolean updateGene2Go() {
		boolean flag = true;
		if (genUniID == null || genUniID.equals("")) {
			return false;
		}
		for (Gene2Go gene2Go : lsGOInfoUpdate) {
			if (idType.equals(GeneID.IDTYPE_GENEID)) {
				if (!servGene2Go.updateGene2Go(genUniID, taxID, gene2Go))
					flag = false;
			} 
			else if (idType.equals(GeneID.IDTYPE_UNIID)) {
				if (!servUniGene2Go.updateUniGene2Go(genUniID, taxID, gene2Go))
					flag = false;
			}
			else {
				return false;
			}
		}
		return flag;
	}

	/**
	 * 升级失败的ID写入本表
	 */
	static String txtUpdateFailFile = "";

	/**
	 * 升级geneID数据库，并且将geneUniID按照数据库进行重置 <b>只升级第一个获得的geneID</b>
	 * 如果accID没有，则不升级
	 * @param 如果在数据库中没有找到对应的ID
	 *            ，是否将ID导入UniID库
	 *            true,导入uniID库，并且重置idtype
	 * @throws EOFException
	 */
	private boolean updateGeneID(AgeneUniID geneUniID, boolean updateUniID) {
		if (genUniID == null && accID == null) {
			return false;
		}
		//说明不需要升级accID
		if (accID == null || accID.equals("")) {
			return true;
		}
		if (geneIDDBinfo == null || geneIDDBinfo.equals("")) {
			logger.error("升级geneID时没有设置该gene的数据库来源，自动设置为NCBIID");
			geneIDDBinfo = DBAccIDSource.NCBI.toString();
		}
		// 只升级第一个获得的geneID
		if (geneUniID != null) {
			this.idType = geneUniID.getGeneIDtype();
			
			if (geneUniID.getGeneIDtype().equals(GeneID.IDTYPE_GENEID)) {
				NCBIID ncbiid = new NCBIID();
				ncbiid.setAccID(accID);
				ncbiid.setDBInfo(this.geneIDDBinfo);
				ncbiid.setGenUniID(geneUniID.getGenUniID());
				ncbiid.setTaxID(geneUniID.getTaxID());
				servNCBIID.updateNCBIID(ncbiid, overrideUpdateDBinfo);
			} else if (geneUniID.getGeneIDtype().equals(GeneID.IDTYPE_UNIID)) {
				UniProtID uniProtID = new UniProtID();
				uniProtID.setAccID(accID);
				uniProtID.setDBInfo(this.geneIDDBinfo);
				uniProtID.setGenUniID(geneUniID.getGenUniID());
				uniProtID.setTaxID(geneUniID.getTaxID());
				servUniProtID.updateUniProtID(uniProtID, overrideUpdateDBinfo);
			}
		} else if (updateUniID) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setDBInfo(this.geneIDDBinfo);
			uniProtID.setGenUniID(accID);
			uniProtID.setTaxID(taxID);
			servUniProtID.updateUniProtID(uniProtID, overrideUpdateDBinfo);
			// 重置geneUniID
			idType = GeneID.IDTYPE_UNIID;
			genUniID = accID;
			//防止下一个导入的时候出错
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
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
		if (genUniID == null || genUniID.equals("")) {
			return false;
		}
		if (geneInfo == null) {
			return true;
		}
		geneInfo.setTaxID(taxID);
		if (idType.equals(GeneID.IDTYPE_UNIID)) {
			servUniGeneInfo.updateUniGenInfo(genUniID, taxID, geneInfo);
			updateUniGeneInfoSymbolAndSynonyms(geneInfo);
		} else if (idType.equals(GeneID.IDTYPE_GENEID)) {
			try {
				servGeneInfo.updateGenInfo(genUniID, taxID, geneInfo);
			} catch (Exception e) {
				servGeneInfo.updateGenInfo(genUniID, taxID, geneInfo);
			}
			updateGeneInfoSymbolAndSynonyms(geneInfo);
		}
		else {
			return false;
		}
		geneInfo = null;
		return true;
	}
	
	private void updateGeneInfoSymbolAndSynonyms(AGeneInfo geneInfo2) {
		NCBIID ncbiid = null;
		if (geneInfo2.getSep() != null && !geneInfo2.equals("")) {
			if (geneInfo2.getSymb() != null) {
				String[] ssymb = geneInfo2.getSymb().split(geneInfo2.getSep());
				for (String string : ssymb) {
					ncbiid = new NCBIID();
					ncbiid.setAccID(string.trim());
					ncbiid.setDBInfo(DBAccIDSource.Symbol.toString());
					ncbiid.setGenUniID(genUniID);
					ncbiid.setTaxID(taxID);
					servNCBIID.updateNCBIID(ncbiid, true);
				}
			}
			if (geneInfo2.getSynonym() != null) {
				String[] ssynonym = geneInfo2.getSynonym().split(geneInfo2.getSep());
				for (String string : ssynonym) {
					ncbiid = new NCBIID();
					ncbiid.setAccID(string.trim());
					ncbiid.setDBInfo(DBAccIDSource.Synonyms.toString());
					ncbiid.setGenUniID(genUniID);
					ncbiid.setTaxID(taxID);
					servNCBIID.updateNCBIID(ncbiid, true);
				}
			}
		}
		else {
			if (geneInfo2.getSymb() != null) {
				ncbiid = new NCBIID();
				ncbiid.setAccID(geneInfo2.getSymb());
				ncbiid.setDBInfo(DBAccIDSource.Symbol.toString());
				ncbiid.setGenUniID(genUniID);
				ncbiid.setTaxID(taxID);
				servNCBIID.updateNCBIID(ncbiid, false);
			}
			if (geneInfo2.getSynonym() != null) {
				ncbiid = new NCBIID();
				ncbiid.setAccID(geneInfo2.getSynonym());
				ncbiid.setDBInfo(DBAccIDSource.Synonyms.toString());
				ncbiid.setGenUniID(genUniID);
				ncbiid.setTaxID(taxID);
				servNCBIID.updateNCBIID(ncbiid, false);
			}
		}
	}
	private void updateUniGeneInfoSymbolAndSynonyms(AGeneInfo uniGeneInfo) {
		UniProtID uniprotID = null;
		if (uniGeneInfo.getSep() != null && !uniGeneInfo.equals("")) {
			if (uniGeneInfo.getSymb() != null) {
				String[] ssymb = uniGeneInfo.getSymb().split(uniGeneInfo.getSep());
				for (String string : ssymb) {
					uniprotID = new UniProtID();
					uniprotID.setAccID(string.trim());
					uniprotID.setDBInfo(DBAccIDSource.Symbol.toString());
					uniprotID.setGenUniID(genUniID);
					uniprotID.setTaxID(taxID);
					servUniProtID.updateUniProtID(uniprotID, true);
				}
			}
			if (uniGeneInfo.getSynonym() != null) {
				String[] ssynonym = uniGeneInfo.getSynonym().split(uniGeneInfo.getSep());
				for (String string : ssynonym) {
					uniprotID = new UniProtID();
					uniprotID.setAccID(string.trim());
					uniprotID.setDBInfo(DBAccIDSource.Synonyms.toString());
					uniprotID.setGenUniID(genUniID);
					uniprotID.setTaxID(taxID);
					servUniProtID.updateUniProtID(uniprotID, true);
				}
			}
		}
		else {
			if (uniGeneInfo.getSymb() != null) {
				uniprotID = new UniProtID();
				uniprotID.setAccID(uniGeneInfo.getSymb());
				uniprotID.setDBInfo(DBAccIDSource.Symbol.toString());
				uniprotID.setGenUniID(genUniID);
				uniprotID.setTaxID(taxID);
				servUniProtID.updateUniProtID(uniprotID, true);
			}
			if (uniGeneInfo.getSynonym() != null) {
				uniprotID = new UniProtID();
				uniprotID.setAccID(uniGeneInfo.getSynonym());
				uniprotID.setDBInfo(DBAccIDSource.Synonyms.toString());
				uniprotID.setGenUniID(genUniID);
				uniprotID.setTaxID(taxID);
				servUniProtID.updateUniProtID(uniprotID, true);
			}
		}
	}
	// /////////////////////// 升级 Blast 的信息
	// /////////////////////////////////////////////////////

	private boolean updateBlastInfo() {
		boolean blastCorrect = true;
		if (genUniID == null || genUniID.equals("")) {
			return false;
		}
		if (lsBlastInfosUpdate == null) {
			return true;
		}
		for (BlastInfo blastInfo : lsBlastInfosUpdate) {
			if (blastInfo.getSubjectTab().equals(GeneID.IDTYPE_ACCID)) {
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
	 * 如果没搜到则返回null
	 */
	private AgeneUniID getUpdateGenUniID() {
		// /// 如果已经有了IDtype，就直接返回 ////////////////////////////////////////
		if (!idType.equals(GeneID.IDTYPE_ACCID)) {
			AgeneUniID geneUniID = null;
			if (idType.equals(GeneID.IDTYPE_GENEID)) {
				geneUniID = new NCBIID();
			} else if (idType.equals(GeneID.IDTYPE_UNIID)) {
				geneUniID = new UniProtID();
			}
			geneUniID.setTaxID(taxID);
			geneUniID.setGenUniID(genUniID);
			geneUniID.setAccID(accID);
			geneUniID.setDBInfo(geneIDDBinfo);
			return geneUniID;
		}
		
		// 保存所有refID--也就是用于查找数据库的refxDB的信息ID，他们所对应的geneUniID
		ArrayList<ArrayList<AgeneUniID>> lsgeneID = new ArrayList<ArrayList<AgeneUniID>>();
		for (String refAccID : lsRefAccID) {
			ArrayList<AgeneUniID> lsTmpGenUniID = getNCBIUniTax(refAccID, taxID);
			if (lsTmpGenUniID.size() == 0) {
				continue;
			} else if (lsTmpGenUniID.size() == 1 && lsTmpGenUniID.get(0).getDBInfo().equals(GeneID.IDTYPE_GENEID)) {
				genUniID = lsTmpGenUniID.get(0).getGenUniID();
				idType = GeneID.IDTYPE_GENEID;
				return lsTmpGenUniID.get(0);
			}
			lsgeneID.add(lsTmpGenUniID);
		}
		// 挑选出含有geneUniID的geneID
		Collections.sort(lsgeneID, new Comparator<ArrayList<AgeneUniID>>() {
			public int compare(ArrayList<AgeneUniID> o1, ArrayList<AgeneUniID> o2) {
				Integer o1Info = GeneIDabs.getHashAccIDtype2Int().get(o1.get(0).getGeneIDtype());
				Integer o2Info = GeneIDabs.getHashAccIDtype2Int().get(o2.get(0).getGeneIDtype());
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
			if (accID != null && !accID.equals("")) {
				if (genUniID == null || genUniID.equals("") || genUniID.equals("0")) {
					genUniID = accID;
				}
				return null;
			}
		}
		AgeneUniID ageneUniID = lsgeneID.get(0).get(0);
		return ageneUniID;
	}

	private static HashMap<String, Integer> hashAccIDtype2Int = null;

	private static HashMap<String, Integer> getHashAccIDtype2Int() {
		if (hashAccIDtype2Int == null) {
			hashAccIDtype2Int = new HashMap<String, Integer>();
			hashAccIDtype2Int.put(GeneID.IDTYPE_ACCID, 300);
			hashAccIDtype2Int.put(GeneID.IDTYPE_UNIID, 200);
			hashAccIDtype2Int.put(GeneID.IDTYPE_GENEID, 100);
		}
		return hashAccIDtype2Int;
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
	protected static ArrayList<AgeneUniID> getNCBIUniTax(String accID, int taxID) {
		ArrayList<AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (accID == null || accID.equals("")) {
			return lsResult;
		}
		
		ServNCBIID servGeneAnno = new ServNCBIID();
		ServUniProtID servUniProtID = new ServUniProtID();

		NCBIID ncbiid = new NCBIID();
		ncbiid.setAccID(accID);
		ncbiid.setTaxID(taxID);
		
		// 先查ncbiid
		ArrayList<NCBIID> lsNcbiids = servGeneAnno.queryLsNCBIID(ncbiid);
		if (lsNcbiids != null && lsNcbiids.size() > 0) {
			return getLsGeneIDinfo(lsNcbiids);
		}
		// 查不到查uniprotID
		else {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setAccID(accID);
			uniProtID.setTaxID(taxID);
			ArrayList<UniProtID> lsUniProtIDs = servUniProtID.queryLsUniProtID(uniProtID);
			return getLsGeneIDinfo(lsUniProtIDs);
		}
	}
	/** 指定一系列的NCBIID或者UniID，进行过滤，
	 * 主要是过滤
	 * 如果同一个物种得到了两个以上的accID，那么跳过数据库为DBINFO_SYNONYMS的项目 */
	private static ArrayList<AgeneUniID> getLsGeneIDinfo(ArrayList<? extends AgeneUniID> lsNcbiids) {
		ArrayList<AgeneUniID> lsGeneIDinfo = new ArrayList<AgeneUniID>();
		for (AgeneUniID geneUniID : lsNcbiids) {
			if (geneUniID.getDBInfo().equals(DBAccIDSource.Synonyms.toString())) {
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

		if (
		// geneID相同且都不为“”，可以认为两个基因相同
		(!genUniID.trim().equals("")
				&& !otherObj.getGenUniID().trim().equals("")
				&& genUniID.trim().equals(otherObj.getGenUniID().trim())
				&& idType.equals(otherObj.getIDtype()) && taxID == otherObj
				.getTaxID())
				|| // geneID都为""，那么如果两个accID相同且不为""，也可认为两个基因相同
				(genUniID.trim().equals("")
						&& otherObj.getGenUniID().trim().equals("")
						&& (!accID.equals("") && !otherObj.getAccID()
								.equals(""))
						&& accID.equals(otherObj.getAccID())
						&& idType.equals(otherObj.getIDtype()) && taxID == otherObj
						.getTaxID())
				|| (genUniID.trim().equals("")
						&& otherObj.getGenUniID().trim().equals("")
						&& accID.equals("") && otherObj.getAccID().equals(""))) {
			return true;
		}
		return false;
	}

	/**
	 * 重写hashcode
	 */
	public int hashCode() {
		String hash = "";
		if (!genUniID.trim().equals("")) {
			hash = genUniID.trim() + "sep_@@_genUni_" + idType.trim() + "@@"
					+ taxID;
		} else if (genUniID.trim().equals("") && !accID.trim().equals("")) {
			hash = accID.trim() + "@@accID" + idType.trim() + "@@" + taxID;
		} else if (genUniID.trim().equals("") && accID.trim().equals("")) {
			hash = "";
		}
		return hash.hashCode();
	}

}
