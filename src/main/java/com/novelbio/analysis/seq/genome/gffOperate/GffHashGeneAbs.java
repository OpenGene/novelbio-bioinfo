package com.novelbio.analysis.seq.genome.gffOperate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListHashSearch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.other.downloadpicture.pixiv.compDon2Chan;

public abstract class GffHashGeneAbs extends ListHashSearch<GffDetailGene, GffCodGene, GffCodGeneDU, ListGff> implements GffHashGeneInf {
	public static void main(String[] args) {
		Species species = new Species(9606);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO("AT1G16860");
		System.out.println("stop");
	}
	private static Logger logger = Logger.getLogger(GffHashGeneAbs.class);
	int taxID = 0;
	String acc2GeneIDfile = "";
	String gfffile = "";
	private HashMap<String, String> mapGeneID2AccID = null;
	HashMap<String, GffGeneIsoInfo> mapName2Iso = new HashMap<String, GffGeneIsoInfo>();
	
	public GffHashGeneAbs() {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		mapGeneID2AccID = new HashMap<String, String>();
	}
	/**
	 * �ڶ�ȡ�ļ��������ʲô��Ҫ���õģ�����д��setOther();��������
	 * 	 * ���ԣ�1.��ȡgff�ļ�
	 * 2. ��gff�ļ����ص��Ļ���ϲ�Ϊ1����ReadGffarrayExcep����ʵ�֣���
	 * ������Щ���ֲ�ͬ�Ļ���ʵ����һģһ����Ϊ�˷�ֹ���������������ڱ������в�ɾ�����ֲ�ͬʵ����ͬ��iso
	 * 3.����super.ReadGffarray�����mapName2DetailNum�� mapName2Detail
	 * 4. ɾ����ͬ��iso
	 * @param gfffilename
	 */
	public void ReadGffarray(String gfffilename) {
		this.acc2GeneIDfile = FileOperate.changeFileSuffix(gfffilename, "_accID2geneID", "list");
		super.ReadGffarray(gfffilename);
		
		//ɾ���ظ���iso
		for (ListGff listGff : mapChrID2ListGff.values()) {
			listGff.sort();
			for (int i = 0; i < listGff.size(); i++) {
				GffDetailGene gffDetailGene = listGff.get(i);
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					gffGeneIsoInfo.sort();
					try {
						gffGeneIsoInfo.setATGUAGncRNA();
					} catch (Exception e) {
						gffGeneIsoInfo.setATGUAGncRNA();
					}
				}
				gffDetailGene.removeDupliIso();
			}
		}
	}
	/**
	 * �ڶ�ȡ�ļ��������ʲô��Ҫ���õģ�����д��setOther();��������
	 * @param gfffilename
	 */
	public void ReadGffarrayExcep(String gfffilename) {
		try {
			ReadGffarrayExcepTmp(gfffilename);
			for (Entry<String, ListGff> entry : mapChrID2ListGff.entrySet()) {
				String chrID = entry.getKey();
				ListGff listGff = entry.getValue();
				ListGff listGffNew = listGff.combineOverlapGene();
				//װ��hash��
				for (GffDetailGene gffDetailGene : listGff) {
					for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
						mapName2Iso.put(GeneID.removeDot(gffGeneIsoInfo.getName().toLowerCase()), gffGeneIsoInfo);
						mapName2Iso.put(gffGeneIsoInfo.getName().toLowerCase(), gffGeneIsoInfo);
					}
				}
				mapChrID2ListGff.put(chrID, listGffNew);
				listGff = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	protected abstract void ReadGffarrayExcepTmp(String gfffilename) throws Exception;
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * ��������������ػ����������Ϣ��
	 * ��������accID
	 * @param accID
	 * @return
	 */
	public GffDetailGene searchLOC(String accID) {
		GffDetailGene gffDetailGene = super.searchLOC(accID);
		if (gffDetailGene == null) {
			GeneID copedID = new GeneID(accID, taxID, false);
			if (copedID.getIDtype().equals(GeneID.IDTYPE_ACCID)) {
				return null;
			}
			String locID = null;
			try {
				locID = getMapGeneID2Acc(acc2GeneIDfile).get(copedID.getGenUniID()).split("//")[0];
			} catch (Exception e) {
				logger.error("û�и�accID��"+accID);
				return null;
			}
			gffDetailGene = super.searchLOC(locID);
		}
		return gffDetailGene;
	}

	
	/**
	 * ��������������ػ���ľ���ת¼������Ҫ����UCSC��
	 * û�ҵ������ת¼�����֣���ô�ͷ����ת¼��
	 * ��������accID
	 * @param accID
	 * @return
	 */
	public GffGeneIsoInfo searchISO(String accID) {
		GffGeneIsoInfo gffGeneIsoInfo = mapName2Iso.get(accID.toLowerCase());
		if (gffGeneIsoInfo != null) {
			return gffGeneIsoInfo;
		}
		
		GffDetailGene gffdetail = searchLOC(accID);
		if (gffdetail == null) {
    		logger.info("cannotFind the ID: "+ accID);
			return null;
		}
		GffGeneIsoInfo gffGeneIsoInfoOut = gffdetail.getIsolist(accID);
		if (gffGeneIsoInfoOut == null) {
			gffGeneIsoInfoOut = gffdetail.getLongestSplitMrna();
		}
		return gffGeneIsoInfoOut;
	}
	/**
	 * ����ȫ���ں��ӣ����ȴ�С��������
	 * @return
	 */
	public ArrayList<Integer> getLsIntronSortedS2M() {
		ArrayList<Integer> lsIntronLen = new ArrayList<Integer>();
		for(Entry<String, ListGff> entry:mapChrID2ListGff.entrySet()) {
			String key = entry.getKey();
			ListGff value = entry.getValue();
			int chrLOCNum=value.size();
		    //һ��һ��Ⱦɫ���ȥ����ں��Ӻ������ӵĳ���
		    for (int i = 0; i < chrLOCNum; i++) {
				GffDetailGene tmpUCSCgene=value.get(i);
				GffGeneIsoInfo gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplitMrna();
				gffGeneIsoInfoLong.getLsIntron();
				for (ExonInfo intronInfo : gffGeneIsoInfoLong) {
					lsIntronLen.add(intronInfo.Length());
				}
			}
		}
		Collections.sort(lsIntronLen);
		return lsIntronLen;
	}
	/**
	 * ����CopedID�����ػ����������Ϣ��
	 * @param copedID 
	 * @return
	 * û�оͷ���null
	 */
	public GffDetailGene searchLOC(GeneID copedID) {
		GffDetailGene gffDetailGene = super.searchLOC(copedID.getAccID());
		if (gffDetailGene != null) {
			return gffDetailGene;
		}
		
		GffGeneIsoInfo gffGeneIsoInfo = searchISO(copedID.getAccID());
		if (gffGeneIsoInfo != null) {
			return gffGeneIsoInfo.getParentGffDetailGene();
		}
		
		String locID = getMapGeneID2Acc(acc2GeneIDfile).get(copedID.getGenUniID()).split("//")[0];
		return super.searchLOC(locID);
	}
	
	/**
	 * ����
	 * @param txtaccID2GeneID
	 * @return
	 * hashGeneID2Acc��һ��geneID��Ӧ���accID��ʱ��accID�á�//������
	 */
	private HashMap<String, String> getMapGeneID2Acc(String txtaccID2GeneID) {
		if (mapGeneID2AccID != null && mapGeneID2AccID.size() > 0) {
			return mapGeneID2AccID;
		}
		if (!FileOperate.isFileExistAndBigThanSize(txtaccID2GeneID, 3)) {
			writeAccID2GeneID(txtaccID2GeneID);
		}
		mapGeneID2AccID = new HashMap<String, String>();
		TxtReadandWrite txtAcc2GenID = new TxtReadandWrite(txtaccID2GeneID, false);
		ArrayList<String> lsAccID = txtAcc2GenID.readfileLs();
		for (String string : lsAccID) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (mapGeneID2AccID.containsKey(ss[1])) {
				mapGeneID2AccID.put(ss[1], mapGeneID2AccID.get(ss[1])+"//"+ss[0]);
			}
			else {
				mapGeneID2AccID.put(ss[1], ss[0]);
			}
		}
		return mapGeneID2AccID;
	}
	/**
	 * һ��Gff�ļ�ֻ��һ�ξͺ�
	 * ����ȡ��Gff�ļ��е�AccIDת��ΪGeneID���ұ������ı��У��´�ֱ�Ӷ�ȡ���ı����ɻ��AccID��GeneID�Ķ��ձ����ٲ���
	 * @param txtAccID2GeneID
	 */
	private void writeAccID2GeneID(String txtaccID2GeneID) {
		TxtReadandWrite txtAccID2GeneID = new TxtReadandWrite(txtaccID2GeneID, true);
		txtAccID2GeneID.ExcelWrite(getGene2ID());
	}
	/**
	 * ���Gene2GeneID�����ݿ��е���Ϣ������д���ı���һ�㲻��
	 */
	private ArrayList<String[]> getGene2ID() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		
		ArrayList<String> lsAccID = getLsNameAll();
		for (String accID : lsAccID) {
			GeneID copedID = new GeneID(accID, taxID, false);
			String[] tmpAccID = new String[2];
			tmpAccID[0] = copedID.getAccID();
			tmpAccID[1] = copedID.getGenUniID();
			lsResult.add(tmpAccID);
		}
		return lsResult;
	}
	
	/**
	 * ������װ��GffHash��
	 * @param chrID
	 * @param gffDetailGene
	 */
	public void addGffDetailGene(String chrID, GffDetailGene gffDetailGene) {
		chrID = chrID.toLowerCase();
		if (!mapChrID2ListGff.containsKey(chrID)) {
			ListGff lsGffDetailGenes = new ListGff();
			mapChrID2ListGff.put(chrID, lsGffDetailGenes);
		}
		ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID);
		lsGffDetailGenes.add(gffDetailGene);
	}
	/**
	 * 
	 * ���ļ�д��GTF��
	 * @param GTFfile
	 * @param title ����GTF�������
	 */
	@Override
	public void writeToGTF(String GTFfile,String title) {
		TxtReadandWrite txtGtf = new TxtReadandWrite(GTFfile, true);
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(mapChrID2ListGff);
		//�ѵõ���ChrID�Ÿ���
		TreeSet<String> treeSet = new TreeSet<String>();
		for (String string : lsChrID) {
			treeSet.add(string);
		}
		//��������ȥ�ظ�����Ϊһ������ֻ����һ������
		//�����������һ���Ļ�������������������.1��.2��
		HashSet<String> setGeneName = new HashSet<String>();
		HashSet<String> setTranscriptName = new HashSet<String>();
		for (String string : treeSet) {
			ArrayList<GffDetailGene> lsGffDetailGenes = mapChrID2ListGff.get(string);
			for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
				String geneID = gffDetailGene.getNameSingle();
				String geneIDinput = getNoReplicateName(setGeneName, geneID);
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					String gffIsoName = getNoReplicateName(setTranscriptName, gffGeneIsoInfo.getName());
					gffGeneIsoInfo.setName(gffIsoName);
					setTranscriptName.add(gffIsoName);
				}
				setGeneName.add(geneIDinput);
				
				gffDetailGene.removeDupliIso();
				String geneGTF = gffDetailGene.toGTFformate(geneIDinput, title);
				txtGtf.writefileln(geneGTF.trim());
			}
		}
		txtGtf.close();
	}
	
	private String getNoReplicateName(HashSet<String> setGeneName, String thisName) {
		String geneIDinput = thisName;
		int num = 1;
		while (setGeneName.contains(geneIDinput)) {
			geneIDinput = thisName + "." + num;
			num ++;
		}
		return geneIDinput;
	}
	/**
	 * ��һ��Ⱦɫ���е� ���в�ֹһ��ת¼���� ������Ϣд���ı�������GTF��ʽ
	 * Ҳ����˵��������һ��ת¼���Ļ���Ͳ�д���ı���
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 * @param title
	 */
	@Override
	public void writeToGFFIsoMoreThanOne(String GFFfile, String title) {
		TxtReadandWrite txtGtf = new TxtReadandWrite(GFFfile, true);
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(mapChrID2ListGff);
		//�ѵõ���ChrID�Ÿ���
		TreeSet<String> treeSet = new TreeSet<String>();
		for (String string : lsChrID) {
			treeSet.add(string);
		}
		for (String string : treeSet) {
			ArrayList<GffDetailGene> lsGffDetailGenes = mapChrID2ListGff.get(string);
			writeToGFFIsoMoreThanOne(txtGtf, lsGffDetailGenes, title);
		}
		txtGtf.close();
	}
	
	/**
	 * ��һ��Ⱦɫ���е� ���в�ֹһ��ת¼���� ������Ϣд���ı�������GTF��ʽ
	 * Ҳ����˵��������һ��ת¼���Ļ���Ͳ�д���ı���
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 * @param title
	 */
	private void writeToGFFIsoMoreThanOne(TxtReadandWrite txtWrite, ArrayList<GffDetailGene> lsGffDetailGenes, String title) {
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1) {
				continue;
			}
			String geneGFF = gffDetailGene.toGFFformate(title);
			txtWrite.writefileln(geneGFF.trim());
		}
	}
	@Override
	public void writeGene2Iso(String Gene2IsoFile) {
		TxtReadandWrite txtGtf = new TxtReadandWrite(Gene2IsoFile, true);
		HashSet<String> setRemoveRedundentID = new HashSet<String>();
		ArrayList<GffDetailGene> lsGffDetailGenes = getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			String symbol = getGeneSymbol(gffDetailGene);
			
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (isNotRedundent(setRemoveRedundentID, symbol, gffGeneIsoInfo.getName())) {
					txtGtf.writefileln(symbol + "\t" + gffGeneIsoInfo.getName());
				}
			}
		}
		txtGtf.close();
	}
	private String getGeneSymbol(GffDetailGene gffDetailGene) {
		GeneID copedID = gffDetailGene.getLongestSplitMrna().getGeneID();
		String symbol = null;
		if (copedID.getIDtype() != GeneID.IDTYPE_ACCID || copedID.getSymbol() == null || copedID.getSymbol().equals("")) {
			symbol = copedID.getSymbol();
			if (symbol.equals("")) {
				symbol = copedID.getAccID();
			}
		} else {
			symbol = gffDetailGene.getNameSingle();
		}
		symbol = GeneID.removeDot(symbol);
		return symbol;
	}
	private boolean isNotRedundent(HashSet<String> setRemoveRedundentID, String symbol, String geneID) {
		if (setRemoveRedundentID.contains(symbol + SepSign.SEP_ID + geneID)) {
			return false;
		}
		else {
			setRemoveRedundentID.add(symbol + SepSign.SEP_ID + geneID);
			return true;
		}
	}

}
