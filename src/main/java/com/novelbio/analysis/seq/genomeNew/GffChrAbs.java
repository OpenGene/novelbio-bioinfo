package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.species.Species;
/**
 * GffHashGene��SeqHash����static��Ҳ����һ��ֻ�ܶ�һ�����ֽ��з���
 * MapReads����static��Ҳ���ǿ���ͬʱ������mapping�ļ�
 * @author zong0jie
 *
 */
public class GffChrAbs {
	private static final Logger logger = Logger.getLogger(GffChrAbs.class);
	private int distanceMapInfo = 3000;
	GffHashGene gffHashGene = null;
	SeqHash seqHash = null;
	MapReads mapReads = null;
	Species species;
	
	int[] tss = new int[]{-1500, 1500};
	int[] tes = null;
	boolean genebody = false;
	boolean UTR5 = false;
	boolean UTR3 = false;
	boolean exonFilter = false;
	boolean intronFilter = false;
	boolean filtertss = false;
	boolean filtertes = false;

	boolean HanYanFstrand = false;
	
	/** ��׼��������Ĭ��Ϊ����׼�� */
	int mapNormType = MapReads.NORMALIZATION_NO;
	/** ��Ҫ���ڻ�ͼ */
	int upBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	int downBp = 5000;//tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	int tssUpBp = 3000;
	int tssDownBp = 2000;
	int geneEnd3UTR = 100;
	
	String chrRegx = null;
	String equationsFile = "";

	public GffChrAbs() {}
	public GffChrAbs(Species species) {
		setSpecies(species);
	}	
	public GffChrAbs(int taxID) {
		setTaxID(taxID);
	}
	/**
	 * @param gffType
	 * @param gffFile
	 * @param chrFile �����ļ��������ļ���
	 * @param regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У�
	   ���ø�������ʽ��ȡ���и��ļ������ļ� ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param readsBed
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public GffChrAbs(String gffType, String gffFile, String chrFile, String regx, String readsBed, int binNum) {
		setGffFile(0, gffType, gffFile);
		setChrFile(chrFile, regx);
		this.setMapReads(readsBed, binNum);
	}
	/**
	 * @param gffType
	 * @param gffFile
	 * @param chrFile
	 * @param readsBed
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public GffChrAbs(String gffType, String gffFile, String chrFile,String readsBed ,int binNum) {
		setGffFile(0, gffType, gffFile);
		setChrFile(chrFile, null);
		this.setMapReads(readsBed, binNum);
	}
	public void setTaxID(int taxID) {
		this.species = new Species(taxID);
		setGffFile(species.getTaxID(), species.getGffFile()[0], species.getGffFile()[1]);
		setChrFile(species.getChrRegxAndPath()[1], species.getChrRegxAndPath()[0]);
	}
	public void setSpecies(Species species) {
		if (this.species != null && this.species.equals(species)) {
			return;
		}
		this.species = species;
		setGffFile(species.getTaxID(), species.getGffFile()[0], species.getGffFile()[1]);
		setChrFile(species.getChrRegxAndPath()[1], species.getChrRegxAndPath()[0]);
	}
	public void setGffHash(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	public void set(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	/** ���û���趨species���ͷ���һ��ȫ�µ�species��������taxID == 0 */
	public Species getSpecies() {
		if (species == null) {
			return new Species();
		}
		return species;
	}
	public int getTaxID() {
		if (species == null) {
			return 0;
		}
		return species.getTaxID();
	}
	public void setHanYanFstrand(boolean hanYanFstrand) {
		HanYanFstrand = hanYanFstrand;
	}
	public GffHashGene getGffHashGene() {
		return gffHashGene;
	}
	public SeqHash getSeqHash() {
		return seqHash;
	}
	public MapReads getMapReads() {
		return mapReads;
	}

	/**
	 * ����Tss��GeneEnd�Ķ���
	 * @param filterTss
	 * @param filterGenEnd
	 */
	public void setFilterTssTes(int[] filterTss, int[] filterGenEnd) {
		if (filterTss != null)
			this.filtertss = true;
		else
			this.filtertss = false;
		
		if (filterGenEnd != null)
			this.filtertes = true;
		else
			this.filtertes = false;
		
		this.tss = filterTss;
		this.tes = filterGenEnd;
	}

	public void setFilterGeneBody(boolean filterGeneBody, boolean filterExon, boolean filterIntron) {
		this.genebody = filterGeneBody;
		this.exonFilter = filterExon;
		this.intronFilter = filterIntron;
	}
	public void setFilterUTR(boolean filter5UTR, boolean filter3UTR) {
		this.UTR5 = filter5UTR;
		this.UTR3 = filter3UTR;
	}
	/**
	 * �趨mapreads�ı�׼������
	 * @param mapNormType �� MapReads.NORMALIZATION_ALL_READS ��ѡ��Ĭ��MapReads.NORMALIZATION_ALL_READS
	 */
	public void setMapNormType(int mapNormType) {
		this.mapNormType = mapNormType;
		if (mapReads != null) {
			mapReads.setNormalType(mapNormType);
		}
	}
	/**
	 * ר�����ڻ�ͼʱ�Ĳ���
	 * @param upBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 * @param downBp tss��tes�Լ�����λ������γ��ȣ�Ĭ��5000
	 */
	public void setPlotRegion(int upBp, int downBp) {
		this.upBp = Math.abs(upBp);
		this.downBp = downBp;
	}
	public void setGffFile(int taxID, String gffType, String gffFile) {
		if (FileOperate.isFileExist(gffFile)) {
			gffHashGene = new GffHashGene(gffType, gffFile);
			gffHashGene.setTaxID(taxID);
		}
	}
	/**
	 chrFile �����ļ��������ļ���
    regx ����������ļ������ø�������ʽ��ȡÿ�����е����֣�����������ļ��У�
    ���ø�������ʽ��ȡ���и��ļ������ļ� ���ļ�Ĭ��Ϊ"";�ļ���Ĭ��Ϊ"\\bchr\\w*"��
	 * @param chrFile
	 * @param regx
	 */
	public void setChrFile(String chrFile, String regx) {
		if (FileOperate.isFileExist(chrFile) || FileOperate.isFileDirectory(chrFile)) {
			 seqHash = new SeqHash(chrFile, chrRegx);
		}
	}
	/** �����Ѿ����úõ�mapReads���󣬵��Ǳ�׼����У������GffChrAbs�ṩ */
	public void setMapReads(MapReads mapReads) {
		this.mapReads = mapReads;
		setMapCorrect();
		mapReads.setNormalType(mapNormType);
		mapReads.setMapChrID2Len(species.getMapChromInfo());
	}
	/**
	 * @param readsFile mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			if (HanYanFstrand) {
				mapReads = new MapReadsHanyanChrom();
			} else {
				mapReads = new MapReads();
			}
			mapReads.setBedSeq(readsFile);
			mapReads.setInvNum(binNum);
			mapReads.setMapChrID2Len(species.getMapChromInfo());
			mapReads.setNormalType(mapNormType);

			setMapCorrect();
		}
	}
	/**
	 * ����һ���ı�������  û���ļ���ֱ�ӷ���
	 * @param correctFile x��һ�У�y�ڶ����У��ӵ�һ�п�ʼ��ȡ
	 */
	public void setMapCorrect(String correctFile) {
		this.equationsFile = correctFile;
		setMapCorrect();
	}
	/** �趨��qpcr�Ȳ���У��mapping��� */
	protected void setMapCorrect() {
		Equations equations = new Equations();
		equations.setXYFile(equationsFile);
		if (mapReads != null) {
			mapReads.setFormulatToCorrectReads(equations);
		}
	}
	public void loadMapReads() {
		try { mapReads.running(); }
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ���ָ���ļ��ڵ�������Ϣ
	 * �������λ���յ�ļ����distanceMapInfo���ڣ��ͻ�ɾ���Ǹ�Ȩ�ص͵�
	 * @param txtExcel
	 * @param colChrID
	 * @param colStartLoc
	 * @param colEndLoc
	 * @param colScore ��֣�Ҳ����Ȩ�أ�û�и��еĻ���������Ϊ <= 0
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileRegionMapInfo(String txtExcel, int colChrID, int colStartLoc, int colEndLoc, int colScore,int rowStart) {
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID,colStartLoc,colEndLoc};
		}
		else {
			columnID = new int[]{colChrID,colStartLoc,colEndLoc, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			try {
				mapInfo.setStartEndLoc(Integer.parseInt(strings[1]),Integer.parseInt(strings[2]));
			} catch (Exception e) {
				logger.error("�����������⣺"+mapInfo.getRefID());
				continue;
			}
			if (colScore > 0) {
				mapInfo.setScore(Double.parseDouble(strings[3]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}
	/**
	 * ����reads���MapInfo
	 * ���summit���˸�region�������ܹ�����region*2+1������
	 * �������λ���յ�ļ����distanceMapInfo���ڣ��ͻ�ɾ���Ǹ�Ȩ�ص͵�
	 * @param txtExcel
	 * @param region
	 * @param colChrID
	 * @param colSummit
	 * @param rowStart
	 */
	public ArrayList<MapInfo> readFileSiteMapInfo(String txtExcel,int region ,int colChrID, int colSummit, int colScore, int rowStart) {
		int[] columnID = null;
		if (colScore <= 0 ) {
			 columnID = new int[]{colChrID, colSummit, colScore};
		}
		else {
			columnID = new int[]{colChrID, colSummit, colScore};
		}
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (String[] strings : lstmp) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			try {
				mapInfo.setFlagLoc(Integer.parseInt(strings[1]));
			} catch (Exception e) {
				logger.error("�����������⣺"+mapInfo.getRefID());
				continue;
			}
			mapInfo.setStartEndLoc(mapInfo.getFlagSite() - region, mapInfo.getFlagSite() + region);
			if (colScore > 0) {
				mapInfo.setScore(Double.parseDouble(strings[2]));
			}
			lsMapInfos.add(mapInfo);
		}
		MapInfo.sortLsMapInfo(lsMapInfos, distanceMapInfo);
		return lsMapInfos;
	}
	
	/**
	 * ���������Զ���û���
	 * ����ǰ���趨upBp��downBp
	 * @param lsMapInfos
	 * @param structure GffDetailGene.TSS��
	 * @param binNum �ֳɼ���
	 * @return
	 */
	public ArrayList<MapInfo> getPeakCoveredGeneMapInfo(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure, int binNum) {
		HashMap<GffDetailGene,Double>  hashGffDetailGenes = getPeakGeneStructure( lsMapInfos, structure);
		 ArrayList<MapInfo> lsResult = getMapInfoFromGffGene(hashGffDetailGenes, structure);
		 mapReads.getRegionLs(binNum, lsResult, 0);
		 return lsResult;
	}

	/**
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * ���û��Ȩ�أ��Ͱ���reads���ܶȽ�������
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ 
	 * @param binNum ������ֳɼ���
	 */
	public ArrayList<MapInfo> readFileGeneMapInfo(String txtExcel,int colGeneID, int colScore, int rowStart, GeneStructure Structure, int binNum)
	{
		////////////////////     �� �� ��   ////////////////////////////////////////////
		int[] columnID = null;
		if (colScore <= 0 || colScore == colGeneID) {
			 columnID = new int[]{colGeneID};
		}
		else {
			columnID = new int[]{colGeneID, colScore};
		}	
		ArrayList<String[]> lstmp = ExcelTxtRead.readLsExcelTxt(txtExcel, columnID, rowStart, 0);
		return getLsGeneMapInfo(lstmp, Structure, binNum);
	}
	/**
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * ���û��Ȩ�أ��Ͱ���reads���ܶȽ�������
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param txtExcel
	 * @param colGeneID
	 * @param colScore
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ 
	 * @param binNum ������ֳɼ���
	 */
	public ArrayList<MapInfo> readGeneMapInfoAll(GeneStructure Structure, int binNum) {
		ArrayList<String> lsGeneID = gffHashGene.getLsNameAll();
		ArrayList<String[]> lstmp = new ArrayList<String[]>();
		for (String string : lsGeneID) {
			lstmp.add(new String[]{string.split(SepSign.SEP_ID)[0]});
		}
		return getLsGeneMapInfo(lstmp, Structure, binNum);
	}
	/**
	 * ���geneID�Լ���ӦȨ�أ��ڲ��Զ�ȥ���࣬����Ȩ�ظߵ��Ǹ������������Ӧ��reads
	 * һ�����ڸ���gene express ��heapmapͼ
	 * @param lsGeneValue string[2] 0:geneID 1:value ����1 ����û�У���ô����string[1] 0:geneID
	 * @param rowStart
	 * @param Structure ������ĸ����ֵĽṹ
	 * @param binNum ������ֳɼ���
	 * @return
	 */
	public ArrayList<MapInfo> getLsGeneMapInfo(ArrayList<String[]> lsGeneValue, GeneStructure Structure, int binNum) {
		//��Ȩ�صľ�ʹ�����hash
 		HashMap<GffDetailGene, Double> hashGene2Value = new HashMap<GffDetailGene, Double>();

		for (String[] strings : lsGeneValue) {
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(strings[0]);
			if (gffDetailGene == null) {
				continue;
			}
			//have gene score, using the score as value, when the gene is same, add the score bigger one
			if (strings.length > 1) {
				if (hashGene2Value.containsKey(gffDetailGene)) {
					double score = Double.parseDouble(strings[1]);
					if (MapInfo.isMin2max()) {
						if (hashGene2Value.get(gffDetailGene) < score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					}
					else {
						if (hashGene2Value.get(gffDetailGene) > score) {
							hashGene2Value.put(gffDetailGene, score);
						}
					}
				} else {
					hashGene2Value.put(gffDetailGene, Double.parseDouble(strings[1]));
				}
			}
			//didn't have score
			else {
				hashGene2Value.put(gffDetailGene, 0.0);
			}
		}
		ArrayList<MapInfo> lsMapInfoGene = getMapInfoFromGffGene(hashGene2Value, Structure);
		mapReads.getRegionLs(binNum, lsMapInfoGene, 0);
		if (lsGeneValue.get(0).length <= 1) {
			for (MapInfo mapInfo : lsMapInfoGene) {
				mapInfo.setScore(MathComput.mean(mapInfo.getDouble()));
			}
		}
		return lsMapInfoGene;
	}
	
	/**
	 * ����ǰ���趨upBp��downBp
	 * ����һϵ��gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����LsMapInfo
	 * <b>ע������û�����reads��double[] value</b>
	 * @param setgffDetailGenes
	 * @param structure
	 * @return
	 */
	private ArrayList<MapInfo> getMapInfoFromGffGene(HashMap<GffDetailGene,Double> setgffDetailGenes, GeneStructure structure) {
		ArrayList<MapInfo> lsMapInfos = new ArrayList<MapInfo>();
		for (Entry<GffDetailGene, Double> gffDetailValue : setgffDetailGenes.entrySet()) {
			lsMapInfos.add(getStructureLoc(gffDetailValue.getKey(),gffDetailValue.getValue(), structure));
		}
		return lsMapInfos;
	}

	
	/**
	 * ����peak����Ϣ��chrID������յ㣬���ر�peak���ǵ�Tss�Ļ������͸��������������Tssͼ
	 * �Զ�ȥ�������
	 * @param lsPeakInfo mapInfo������ chrID �� startLoc �� endLoc ���� 
	 * @param structure GffDetailGene.TSS��
	 * @return
	 * �����Ȩ�ص�hash��
	 */
	private HashMap<GffDetailGene,Double> getPeakGeneStructure(ArrayList<? extends MapInfo> lsMapInfos, GeneStructure structure) {
		//�洢���Ļ����Ȩ��
		HashMap<GffDetailGene,Double> hashGffDetailGenes = new HashMap<GffDetailGene,Double>();
		for (MapInfo mapInfo : lsMapInfos) {
			Set<GffDetailGene> setGffDetailGene = getPeakStructureGene( mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), structure );
			for (GffDetailGene gffDetailGene : setGffDetailGene) {
				if (hashGffDetailGenes.containsKey(gffDetailGene)) {
					if (MapInfo.isMin2max()) {
						if (mapInfo.getScore() < hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					}
					else {
						if (mapInfo.getScore() > hashGffDetailGenes.get(gffDetailGene)) {
							hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
						}
					}
				}
				else
					hashGffDetailGenes.put(gffDetailGene, mapInfo.getScore());
			}
		}
		return hashGffDetailGenes;
	}
	/**
	 * �����������򣬷��ظ�peak�����ǵ�GffDetailGene
	 * @param tsstesRange ���Ƕȣ�tss��tes�ķ�Χ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param structure GffDetailGene.TSS��
	 * @return
	 */
	private Set<GffDetailGene> getPeakStructureGene(String chrID, int startLoc, int endLoc, GeneStructure structure) {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(chrID, startLoc, endLoc);
		if (gffCodGeneDU == null) {
			return new HashSet<GffDetailGene>();
		}
		if (structure.equals(GeneStructure.TSS)) {
			return gffCodGeneDU.getTSSGene(tss);
		}
		else if (structure.equals(GeneStructure.TES)) {
			return gffCodGeneDU.getTESGene(tes);
		}
		else {
			logger.error("��ʱû�г�Tss��Tes֮��Ļ���ṹ");
			return null;
		}
	}
	/**
	 * ǰ���趨upBp��downBp
	 * ����gffDetailGene���Լ���Ҫ�Ĳ��֣����ض�Ӧ�����MapInfo
	 * <b>ע������û�����reads��double[] value</b>
	 * @param gffDetailGene
	 * @param value �û�������Ӧ��Ȩ��
	 * @param structure GffDetailGene.TSS��
	 * @return
	 */
	private MapInfo getStructureLoc(GffDetailGene gffDetailGene, Double value,GeneStructure structure)
	{
		if (structure.equals(GeneStructure.TSS)) {
			int tss = gffDetailGene.getLongestSplit().getTSSsite();
			MapInfo mapInfo = null;
			if (gffDetailGene.isCis5to3())
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tss - Math.abs(upBp), tss + Math.abs(downBp), tss,0, gffDetailGene.getLongestSplit().getName());
			else 
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tss - Math.abs(downBp), tss + Math.abs(upBp), tss, 0, gffDetailGene.getLongestSplit().getName());
			mapInfo.setCis5to3(gffDetailGene.isCis5to3());
			mapInfo.setScore(value);
			return mapInfo;
		}
		else if (structure.equals(GeneStructure.TES)) {
			int tes = gffDetailGene.getLongestSplit().getTESsite();
			MapInfo mapInfo = null;
			if (gffDetailGene.isCis5to3())
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tes - Math.abs(upBp), tes + Math.abs(downBp), tes, 0, gffDetailGene.getLongestSplit().getName());
			else 
				mapInfo = new MapInfo(gffDetailGene.getParentName(), tes - Math.abs(downBp), tes + Math.abs(upBp), tes, 0, gffDetailGene.getLongestSplit().getName());
			mapInfo.setCis5to3(gffDetailGene.isCis5to3());
			mapInfo.setScore(value);
			return mapInfo;
		}
		else {
			logger.error("��û��Ӹ������͵�structure");
			return null;
		}
	}

	
}
