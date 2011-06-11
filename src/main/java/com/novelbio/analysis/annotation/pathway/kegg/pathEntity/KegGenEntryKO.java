package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.database.DAO.FriceDAO.DaoFSBlastInfo;
import com.novelbio.database.DAO.KEGGDAO.DaoKCdetail;
import com.novelbio.database.DAO.KEGGDAO.DaoKEntry;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDKeg2Ko;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.kegg.KGCgen2Entry;
import com.novelbio.database.entity.kegg.KGCgen2Ko;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.KGentry;

/**
 * CopedID相关的Kegg信息
 * @author zong0jie
 *
 */
public class KegGenEntryKO {
	
	private String genUniID;
	public String getGenUniID() {
		return this.genUniID;
	}
	
	private String keggID;
	public String getKegID() {
		return this.keggID;
	}
	
	private int taxID;
	public int getTaxID() {
		return this.taxID;
	}
	
	private ArrayList<KegEntity> lsKegEntities = null;
	
	
	/**
	 * 如果本类是人类等注释全面物种的KG，那么这个存储blast的query物种信息，也就是mapping到query物种的entry
	 */
	private ArrayList<KegEntity> lsQKegEntities = null;
	/**
	 * 专门标记lsQKGentries的物种ID的，如果QtaxID == 0 说明lsQKGentries没有被初始化
	 */
	private int QtaxID = 0;
	/**
	 * 没有就返回null
	 * @return
	 */
	public ArrayList<KegEntity> getLsKGentries() {
		return this.lsKegEntities;
	}
	
	protected void setParam(KGCgen2Entry kgCgen2Entry) {
		this.genUniID = kgCgen2Entry.getGenID()+"";
		this.keggID = kgCgen2Entry.getKegID();
		ArrayList<KGentry> lsKGentries = kgCgen2Entry.getLsKGentries();
		if (lsKGentries != null && lsKGentries.size() > 0) {
			for (KGentry kGentry : lsKGentries) {
				KegEntity kegEntity = new KegEntity(kGentry);
				this.lsKegEntities.add(kegEntity);
			}
		}
		this.taxID = kgCgen2Entry.getTaxID();
	}
	
	/**
	 * <b>初始化本类的时候使用</b>
	 * 
	 * 用ncbiid去查找数据库，最后获得该基因的entry，主要用于做pathway的enrichment
	 * <b>如果ncbiid中geneID == 0</b>，说明该NCBIID没有值，那么返回null
	 * if test="geneID !=null and geneID !=0"
geneID=#{geneID}
/if
if test="taxID !=null and taxID !=0"
and taxID=#{taxID}
/if
	 * @param idType
	 * @param geneID
	 * @param taxID
	 * @return
	 */
	public KegGenEntryKO(String idType, String geneID,int taxID) 
	{
		if (idType.equals(CopedID.IDTYPE_GENEID)) {
			NCBIID ncbiid = new NCBIID();
			ncbiid.setGeneId(Integer.parseInt(geneID));
			ncbiid.setTaxID(taxID);
			KGCgen2Entry kgCgen2Entry = null;
			kgCgen2Entry = DaoKCdetail.queryGen2entry(ncbiid);
			if (kgCgen2Entry != null) {
				setParam(kgCgen2Entry);
			}
		}
	}

	ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = null;
	/**
	 * 获得该keggID所对应的KO
	 * 如果没有就返回null
	 */
	public ArrayList<String> getLsKo()
	{
		if (lsKgiDkeg2Kos == null) {
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();
			kgiDkeg2Ko.setKeggID(keggID); kgiDkeg2Ko.setTaxID(taxID);
			lsKgiDkeg2Kos = DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
			if (lsKgiDkeg2Kos == null) {
				lsKgiDkeg2Kos = new ArrayList<KGIDkeg2Ko>();
			}
		}
		if (lsKgiDkeg2Kos.size() == 0) {
			return null;
		}
		ArrayList<String> lsKO = new ArrayList<String>();
		for (KGIDkeg2Ko kgiDkeg2Ko : lsKgiDkeg2Kos) {
			lsKO.add(kgiDkeg2Ko.getKo());
		}
		return lsKO;
	}
	
	/**
	 * 如果本基因是通过blast获得的，那么将本基因所含有的pathway的entity mapping回Query 物种
	 * 如果没有就返回null
	 */
	public ArrayList<KegEntity> getBlastQInfo(int QtaxID)
	{
		///////////如果以前已经查找过了一次///////////
		if (this.QtaxID == QtaxID) {
			if (lsQKegEntities == null || lsQKegEntities.size() <1) {
				return null;
			}
			return lsQKegEntities;
		}
		///////////////////////////
		lsQKegEntities = new ArrayList<KegEntity>();
		this.QtaxID = QtaxID;
		
		ArrayList<String> lsKO = getLsKo();
		if (lsKO == null) {
			return null;
		}
		for (String ko : lsKO) {
			KGentry kGentry = new KGentry();
			KGIDkeg2Ko kgiDkeg2Ko = new KGIDkeg2Ko();  kgiDkeg2Ko.setKo(ko); kgiDkeg2Ko.setTaxID(QtaxID);
			ArrayList<KGIDkeg2Ko> lsKgiDkeg2Kos = DaoKIDKeg2Ko.queryLsKGIDkeg2Ko(kgiDkeg2Ko);
			ArrayList<KegEntity> lskGentries = new ArrayList<KegEntity>();
			////////////////如果geneBlast到了人类，并且得到了相应的KO，那么获得该KO所对应本物种的KeggID，并用KeggID直接mapping回本基因////////////////////////////////////////////////////////////////
			if (lsKgiDkeg2Kos != null && lsKgiDkeg2Kos.size()>0) 
			{
				//虽然一个ko对应多个keggID，但是对于pathway来说，一个ko就对应到一个pathway上，所以一个ko就够了
				String keggID = lsKgiDkeg2Kos.get(0).getKeggID();//这就是本物中的KeggID，用这个KeggID直接可以搜索相应的pathway
				kGentry.setEntryName(keggID);
				kGentry.setTaxID(QtaxID);
				//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
				lskGentries=KegEntity.getLsEntity(kGentry);
			}
			/////////////如果geneBlast到了人类，并且得到了相应的KO，那么获得该KO所对应本物种的KeggID，如果没有KeggID，则用KOmapping回本基因//////////////////////////////////////////////////////////////////
			else
			{
				kGentry.setEntryName(ko);
				kGentry.setTaxID(QtaxID);
				//在给定ko和taxID的情况下，一个ko可以参与多个pathway，和一个pathway里的多个entry
				lskGentries=KegEntity.getLsEntity(kGentry);
			}
			if (lskGentries == null || lskGentries.size() < 1 ) {
				continue;
			}
			lsQKegEntities.addAll(lskGentries);
		}
		if (lsQKegEntities.size() < 1 ) {
			return null;
		}
		return lsQKegEntities;
	}
	
}
