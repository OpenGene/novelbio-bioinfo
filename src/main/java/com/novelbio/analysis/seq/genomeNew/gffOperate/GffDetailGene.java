package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo.ExonCluster;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.generalConf.NovelBioConst;
/**
 * ���ǽ����е�isoװ��hash���У��Լӿ����Ч��
 * ��д��clone����û����дequals��hash
 * hashͬGffDetailAbs�����Ƚ�ChrID + "//" + locString + "//" + numberstart + "//" + numberstart;
 * ר�Ŵ洢UCSC��gene�����ļ�
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * GffDetailList���б���ÿ�����������յ��CDS������յ� 
 * @author zong0jie
 * @GffHashGene��ȡGff�ļ���ÿ��������Ի��������Ϣ
 * ������<br>
 * ��������㣬����UCSC konwn geneĳλ�����л�����ǰ��exon�����<br>
 * �������յ㣬����UCSC konwn geneĳλ�����л��������intron���յ�<br>
 * ����������Ⱦɫ����<br>
 * ������Ĳ�ͬת¼��<br>
 * ������ת¼����<br>
 * �����еļ�����������Gff�����й�<br>
 */
public class GffDetailGene extends ListDetailAbs {
	private final static Logger logger = Logger.getLogger(GffDetailGene.class);
	/** ����ת¼���Ľ����������0.6������һ������ */
	public final static double OVERLAP_RATIO = 0.6;
	/** ˳��洢ÿ��ת¼���ĵ�������� */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//�洢�ɱ���ӵ�mRNA
	ListGff listGff;
	int taxID = 0;
	
	boolean removeDuplicateIso = false;
	/**
	 * @param chrID �ڲ�Сд
	 * @param locString
	 * @param cis5to3
	 */
	public GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}
	public GffDetailGene(ListGff listGff, String locString, boolean cis5to3) {
		super(listGff, locString, cis5to3);
	}
	
	protected void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	/**
	 * ɾ��ת¼��,��0��ʼ����
	 */
	public void removeIso(int id) {
		lsGffGeneIsoInfos.remove(id);
	}
	/**
	 * ����ת¼�������֣�ɾ��ת¼��
	 */
	public void removeIso(String isoName) {
		int id = getIsoID(isoName);
		removeIso(id);
	}
	/**
	 * �������е�ת¼����Ϣ
	 * @param coord
	 */
	public ArrayList<GffGeneIsoInfo> getLsCodSplit() {
		return lsGffGeneIsoInfos;
	}
	/**
	 * ��0��ʼ����
	 * ����-1��ʾû�и�ת¼�� 
	 * @param isoName
	 * @return
	 */
	private int getIsoID(String isoName) {
		for (int i = 0; i < lsGffGeneIsoInfos.size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(i);
			String[] tmpName = gffGeneIsoInfo.getName().split(SepSign.SEP_ID);
			for (String string : tmpName) {
				string = GeneID.removeDot(string);
				if(string.equalsIgnoreCase( GeneID.removeDot(isoName) )) {
					return i;
				}
			}
		}
		return -1;
	}
	/**
	 * ����Tss��Χ����Ϊ����������Ϊ����
	 * ͬʱ�趨���浱ʱ���е�ȫ��GffGeneIsoInfo
	 * @param upTss
	 * @param downTss
	 */
	@Override
	public void setTssRegion(int upTss, int downTss) {
		super.upTss = upTss;
		super.downTss = downTss;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			gffGeneIsoInfo.setTssRegion(upTss, downTss);
		}
	}
	/**
	 * ����Tes��Χ����Ϊ����������Ϊ����
	 * ͬʱ�趨���浱ʱ���е�ȫ��GffGeneIsoInfo
	 * @param upTes
	 * @param downTes
	 */
	@Override
	public void setTesRegion(int upTes, int downTes) {
		this.upGeneEnd3UTR = upTes;
		this.downGeneEnd3UTR = downTes;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			gffGeneIsoInfo.setTesRegion(upTes, downTes);
		}
	}
	/**
	 * �����һ��ת¼�����exon���꣬<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ�
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 * <b>�������һ��û��ת¼���ģ��������һ��gene��������Ϊpseudo</b>
	 */
	protected void addExon(int locStart,int locEnd) {
		if (lsGffGeneIsoInfos.size() == 0) {//�������һ��û��ת¼���ģ��������һ��gene��������Ϊpseudo
			addsplitlist(getName().get(0), GeneType.PSEU_TRANSCRIPT);
		}
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExon(locStart, locEnd);
	}
	/**
	 * ���ˮ�����Ͻ��GFF�ļ�
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ��
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�
	 * �����exon��ʱ�������CDS��UTR֮�������ŵģ���ô�ͽ���CDS��UTR����һ�𣬷���һ��exon�� ����������Ͱ�ԭ������
	 */
	protected void addExonGFFCDSUTR(int locStart,int locEnd) {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFFCDSUTR(locStart, locEnd);
	}
	/**
	 * �����һ��ת¼�����ATG��UAG���꣬<br>
	 * ������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 * ���Զ��ж����������Ƿ�С�����е�atg���յ��Ƿ�������е�uag
	 * �ǵĻ����Ż��趨������Ͳ��趨
	 */
	protected void setATGUAG(int atg, int uag) {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.setATGUAG(atg, uag);
	}
	/**
	 * ����ǷǱ���RNA����atg��uag����Ϊ���һλ
	 */
	protected void setATGUAGncRNA() {
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.sort();
		gffGeneIsoInfo.setATGUAGncRNA();
	}
	/**
	 * ֱ�����ת¼��������genedetail����Ϣ����cis5to3��֮����addcds()��������ת¼�����exon
	 */
	protected void addsplitlist(String splitName, GeneType geneTpye) {
		removeDuplicateIso = false;
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(splitName, this, geneTpye, cis5to3);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
	}
	/**
	 * ֱ�����ת¼����֮����addcds()��������ת¼�����exon
	 */
	protected void addsplitlist(String splitName, GeneType geneTpye, boolean cis5to3) {
		removeDuplicateIso = false;
		
		GffGeneIsoInfo gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(splitName, this, geneTpye, cis5to3);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
	}
	/**
	 * @return ����ת¼������Ŀ
	 */
	public int getSplitlistNumber() {
		return lsGffGeneIsoInfos.size();
    }
	/**
	 * ת¼���򣬼���ͬһ���򲻹ܶ���ת¼����ͬһת¼����
	 * ���Ϊnull���򷵻��ת¼���ķ���
	 */
	public Boolean isCis5to3() {
		if (cis5to3 == null) {
			return getLongestSplit().isCis5to3();
		}
		return this.cis5to3;
	}
    /**
     * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
     * ����ĳ��ת¼���ľ�����Ϣ
     */
    public GffGeneIsoInfo getIsolist(int splitnum) {
    	return lsGffGeneIsoInfos.get(splitnum);//include one special loc start number to end number	
    }
    /**
     * ����ת¼����(UCSC��ʵ�����ǻ�����)<br>
     * û���򷵻�null
     */
    public GffGeneIsoInfo getIsolist(String splitID) {
    	int index = getIsoID(splitID);
    	if (index == -1) {
			return null;
		}
    	return lsGffGeneIsoInfos.get(index);//include one special loc start number to end number	
    }
	/** ��øû��������һ��ת¼������Ϣ */
	public GffGeneIsoInfo getLongestSplit() {
		int id = getLongestSplitID();
		return lsGffGeneIsoInfos.get(id);
	}
    private int getLongestSplitID() {
    	if (lsGffGeneIsoInfos.size() == 1) {
			return 0;
		}
		ArrayList<Integer> lslength = new ArrayList<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.size() == 0)
				lslength.add(0);
			else
				lslength.add(gffGeneIsoInfo.getLen());
		}
		int max = lslength.get(0);
		int id = 0;
		
		for (int i = 0; i < lslength.size(); i++) {
			if (lslength.get(i) > max) {
				max = lslength.get(i);
				id = i;
			}
		}
		return id;
	}
    /**
     * ��øû��������һ��ת¼���Ĳ����������Ϣ���Ѿ����ǹ�������������
     * @param type ָ��ΪINTRON,EXON,UTR5,UTR3
     * @param num ���typeΪ"Intron"��"Exon"��ָ���ڼ���������������򷵻�0
     * num Ϊʵ�ʸ�����
     * ���5UTRֱ�ӷ���ȫ��5UTR
     * 3UTRҲֱ�ӷ���ȫ��3UTR
     * @return 
     */
	public int getTypeLength(GeneStructure geneStructure,int num) {
		GffGeneIsoInfo gffGeneIsoInfo = getLongestSplit();
		if (num > gffGeneIsoInfo.size()) {
			return 0;
		}
		if (geneStructure.equals(GeneStructure.INTRON)) {
			return gffGeneIsoInfo.getLenIntron(num);
		}
		if (geneStructure.equals(GeneStructure.EXON)) {
			return gffGeneIsoInfo.getLenExon(num);
		}
		if (geneStructure.equals(GeneStructure.UTR5)) {
			return gffGeneIsoInfo.getLenUTR5();
		}
		if (geneStructure.equals(GeneStructure.UTR3)) {
			return gffGeneIsoInfo.getLenUTR3();
		}
		return -1000000;
	}
	public void clearIso() {
		lsGffGeneIsoInfos.clear();
	}
	/**
	 * �Ƿ��ڸû����ڣ��������
	 * @return
	 * ����anno[4]
	 * 0��accID
	 * 1��symbol
	 * 2��description
	 * 3��location
	 * û�оͷ��ء���
	 */
	public String[] getInfo(int coord) {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		if (isCodInGeneExtend(coord)) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
					hashCopedID.add(gffGeneIsoInfo.getGeneID());
				}
			}
			for (GeneID copedID : hashCopedID) {
				if (anno.equals("")) {
					anno[0] = copedID.getAccID();
					anno[1] = copedID.getSymbol();
					anno[2] = copedID.getDescription();
				}
				else {
					anno[0] = anno[0]+"//"+copedID.getAccID();
					anno[1] = anno[1]+"//"+copedID.getSymbol();
					anno[2] = anno[2]+"//"+copedID.getDescription();
				}
			}
			if (getLongestSplit().isCodInIsoExtend(coord)) {
				anno[4] = getLongestSplit().toStringCodLocStr(coord);
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
						anno[4] = gffGeneIsoInfo.toStringCodLocStr(coord);
						break;
					}
				}
			}
		}
		return anno;
	}
	/**
	 * ��gffDetailGene�к����µ����ֵ�iso����뱾��
	 * @param gffDetailGene
	 */
	public void addIsoSimple(GffDetailGene gffDetailGene) {
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			addIso(gffGeneIsoInfo);
		}
	}
	
	/**
	 * ȥ���ظ�Isoform
	 */
	public void removeDupliIso() {
		if (removeDuplicateIso) {
			return;
		}
		removeDuplicateIso = true;
		HashMap<GffGeneIsoInfo, Integer> hashIso = new HashMap<GffGeneIsoInfo, Integer>();
		ArrayList<GffGeneIsoInfo> lsResult = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (hashIso.containsKey(gffGeneIsoInfo)) {
				continue;
			}
			lsResult.add(gffGeneIsoInfo);
		}
		this.lsGffGeneIsoInfos = lsResult;
	}
	
	/**
	 * Ч�ʵ��£��ȴ��Ż�
	 * ����µ�ת¼��
	 * ͬʱ�����趨�û����numberstart��numberend
	 * @param gffDetailGeneParent
	 */
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo) {
		if (gffGeneIsoInfo == null || gffGeneIsoInfo.size() == 0)
			return;
	
		gffGeneIsoInfo.setGffDetailGeneParent(this);
		removeDuplicateIso = false;

		if (cis5to3 != null && gffGeneIsoInfo.isCis5to3() != cis5to3) {
			cis5to3 = null;
		}
		for (GffGeneIsoInfo gffGeneIsoInfoOld : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfoOld.equalsIso(gffGeneIsoInfo)) {//�Ƚ�����list�Ƿ�һ�£�exon��equalsֻ�Ƚ�����յ�
				return;
			}
		}

		String IsoName = gffGeneIsoInfo.getName();
		int i = lsGffGeneIsoInfos.size();
		//�޸�����
		while (isContainsIso(IsoName)) {
			IsoName = FileOperate.changeFileSuffix(IsoName, "", ""+i).replace("/", "");
			i++;
		}
		gffGeneIsoInfo.setName(IsoName);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		
		if (numberstart < 0 || numberstart > gffGeneIsoInfo.getStartAbs()) {
			numberstart = gffGeneIsoInfo.getStartAbs();
		}
		if (numberend < 0 || numberend < gffGeneIsoInfo.getEndAbs()) {
			numberend = gffGeneIsoInfo.getEndAbs();
		}
	}
	/**
	 * �����в����exonϵ��
	 * @return
	 */
	public ArrayList<ExonCluster> getDifExonCluster() {
		if (getNameSingle().contains("NM_001080977")) {
			logger.error("stop");
		}
		ArrayList<GffGeneIsoInfo> lsSameGroupIso = getLsGffGeneIsoSameGroup();
		/**
		 * һ����������в�ֹһ����ת¼������ô��Щת¼����ͬһ�����exon�Ϳ�����ȡ�������������list
		 * Ҳ����ÿ��exoncluster����һ��exon�࣬��ʾ 
		 */
		ArrayList<ExonCluster> lsExonClusters = null;
		if (lsSameGroupIso.size() <= 1) {
			return new ArrayList<ExonInfo.ExonCluster>();
		}
		boolean cis5to3 = lsSameGroupIso.get(0).isCis5to3();
		lsExonClusters = GffGeneIsoInfo.getExonCluster(cis5to3, lsSameGroupIso);
		
		ArrayList<ExonCluster> lsDifExon = new ArrayList<ExonCluster>();
		for (ExonCluster exonClusters : lsExonClusters) {
			if (exonClusters.isSameExon()) {
				continue;
			}
			lsDifExon.add(exonClusters);
		}
		return lsDifExon;
	}
	/** ����iso�����ӽ���һ�����ɱ���ӷ���
	 * ֻ�е�����iso��ֻ����������exon�Ĳ�࣬�������ɱ���ӵķ���
	 *  */
	private ArrayList<GffGeneIsoInfo> getLsGffGeneIsoSameGroup() {
		//���lsiso�飬ÿ�������iso�����ڲ�����ӽ����飬Ȼ��Ž�ȥ
		ArrayList<ArrayList<GffGeneIsoInfo>> ls_lsIso = new ArrayList<ArrayList<GffGeneIsoInfo>>();
		boolean flagGetNexIso = false;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			flagGetNexIso = false;
			for (ArrayList<GffGeneIsoInfo> lsIso : ls_lsIso) {
				if (flagGetNexIso)
					break;

				for (GffGeneIsoInfo gffGeneIsoInfoExist : lsIso) {
					if (GffGeneIsoInfo.compareIsoRatio(gffGeneIsoInfo, gffGeneIsoInfoExist) >= 0.6) {
						lsIso.add(gffGeneIsoInfo);
						flagGetNexIso = true;
						break;
					}
				}
				
			}
			if (!flagGetNexIso) {
				ArrayList<GffGeneIsoInfo> lsIsoNew = new ArrayList<GffGeneIsoInfo>();
				lsIsoNew.add(gffGeneIsoInfo);
				ls_lsIso.add(lsIsoNew);
			}
		}
		//�ҳ�����iso������
		int maxIsoIndex = 0; int maxNum = 0;
		for (int i = 0; i < ls_lsIso.size(); i++) {
			ArrayList<GffGeneIsoInfo> lsIso = ls_lsIso.get(i);
			if (lsIso.size() > maxNum) {
				maxIsoIndex = i;
			}
		}
		
		return ls_lsIso.get(maxIsoIndex);
	}
	
	
	/**
	 * ����һ��ת¼����������֮��ӽ���ת¼�������ƶȱ�����ָ����Χ��
	 * û��
	 * @param gffGeneIsoInfo
	 * @param likelyhood ���ƶ� 0-1֮��
	 * @return û���򷵻�null
	 */
	public GffGeneIsoInfo getSimilarIso(GffGeneIsoInfo gffGeneIsoInfo, double likelyhood) {
		HashMap<int[], GffGeneIsoInfo> mapCompInfo2GeneIso = new HashMap<int[], GffGeneIsoInfo>();
		ArrayList<int[]> lsCompInfo = new ArrayList<int[]>();
		for (GffGeneIsoInfo gffGeneIsoInfoRef : lsGffGeneIsoInfos) {
			int[] compareInfo = GffGeneIsoInfo.compareIso(gffGeneIsoInfoRef, gffGeneIsoInfo);
			mapCompInfo2GeneIso.put(compareInfo, gffGeneIsoInfoRef);
			lsCompInfo.add(compareInfo);
		}
		//������ѡ�������Ƶ�ת¼��
		Collections.sort(lsCompInfo, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				Double int1 = (double)o1[0]/o1[1];
				Double int2 = (double)o2[0]/o2[1];
				return -int1.compareTo(int2);
			}
		});
		int[] compareInfo = lsCompInfo.get(0);
		double ratio = (double)compareInfo[0]/Math.min(compareInfo[2], compareInfo[3]);
		if (ratio < likelyhood) {
			return null;
		}
		return mapCompInfo2GeneIso.get(lsCompInfo.get(0));
	}	
	
	public String getGTFformate(String title) {
		String geneGTF = "";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			gffGeneIsoInfo.sort();
			geneGTF = geneGTF + gffGeneIsoInfo.getGTFformat(getNameSingle(), title);
		}
		return geneGTF;
	}
	/**
	 * ����gff��ʽ����Ϣ
	 * @param title ��˾������Ϣ
	 * @return
	 */
	public String getGFFformate(String title) {
		if (title == null || title.trim().equals("")) {
			title = NovelBioConst.COMPANY_NAME_ABBR;
		}
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String geneGFF = getParentName() + "\t" +title + "\tgene\t" + getStartAbs()+ "\t" + getEndAbs()
        + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=" + getNameSingle()
        +";Name=" + getNameSingle() + ";Name=" + getNameSingle() + " " + TxtReadandWrite.ENTER_LINUX;
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			String strandmRNA = "+";
			if (!gffGeneIsoInfo.isCis5to3()) {
				strandmRNA = "-";
			}
			geneGFF = geneGFF + getParentName() + "\t" +title + "\tmRNA\t" +gffGeneIsoInfo.getStartAbs()+ "\t" + gffGeneIsoInfo.getEndAbs()
	        + "\t"+"."+"\t" +strandmRNA+"\t.\t"+ "ID=" + gffGeneIsoInfo.getName() 
	        +";Name="+gffGeneIsoInfo.getName()+ ";Parent="+ getNameSingle() + " " + TxtReadandWrite.ENTER_LINUX;
			gffGeneIsoInfo.sort();
			geneGFF = geneGFF + gffGeneIsoInfo.getGFFformat(getNameSingle(), title);
		}
		return geneGFF;
	}
	/**
	 * ������굽��ItemEnd�ľ���
	 * �����������������������෴�Ļ�����ô�жϳ�������
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * ���û����� >--------5start>--------->3end------->������
	 * ���������end��5������Ϊ����
	 * ���������end��3������Ϊ����
	 * @return
	 */
	public Integer getCod2End(int coord) {
		if (cis5to3 != null) {
			return super.getCod2End(coord);
		}
		return getLongestSplit().getCod2Tes(coord);
	}
	
	/**
	 * ������굽��ItemStart�ľ���,���coordС��0˵�������⣬�򷵻�null
	 * ��֮ǰ���趨coord
	 * ����item������
	 * ���굽��Ŀ�յ��λ�ã�����������<br/>
	 * ���û����� >--------5start>--------->3end------->������
	 * ���������start��5������Ϊ����
	 * ���������start��3������Ϊ����
	 * @return
	 */
	public Integer getCod2Start(int coord) {
		if (cis5to3 != null) {
			return super.getCod2Start(coord);
		}
		return getLongestSplit().getCod2Tss(coord);
	}
	/**
	 * �ж��Ƿ���ڸ����ֵ�ת¼��
	 * @param IsoName
	 */
	private boolean isContainsIso(String IsoName) {
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.getName().toLowerCase().contains(IsoName.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * ǳ��clone��lsGffGeneIsoInfos ��¡�ˡ�
	 * ����ÿ��isoû�б�clone
	 */
	public GffDetailGene clone() {
		GffDetailGene result = null;
		result = (GffDetailGene) super.clone();
		result.taxID = taxID;
		result.lsGffGeneIsoInfos = (ArrayList<GffGeneIsoInfo>) lsGffGeneIsoInfos.clone();
		return result;
	}
	
	public static enum GeneStructure {
		ALLLENGTH,INTRON, CDS, EXON, UTR5, UTR3, TSS, TES;
		/**
		 * �������ֶ�Ӧ��GeneStructure
		 * @return
		 */
		public static HashMap<String, GeneStructure> getMapInfo2GeneStr() {
			HashMap<String, GeneStructure> mapStr2GeneStructure = new HashMap<String, GffDetailGene.GeneStructure>();
			mapStr2GeneStructure.put("Full Length", ALLLENGTH);
			mapStr2GeneStructure.put("Intron", INTRON);
			mapStr2GeneStructure.put("CDS", CDS);
			mapStr2GeneStructure.put("Exon", EXON);
			mapStr2GeneStructure.put("5-UTR", UTR5);
			mapStr2GeneStructure.put("3-UTR", UTR3);
			mapStr2GeneStructure.put("Tss", TSS);
			mapStr2GeneStructure.put("Tes", TES);
			return mapStr2GeneStructure;
		}
		/**
		 * ����GeneStructure��Ӧ������
		 * @return
		 */
		public static HashMap<GeneStructure, String> getMapGene2Str() {
			HashMap<GeneStructure, String> mapGeneStructure2Str = new HashMap<GffDetailGene.GeneStructure, String>();
			HashMap<String, GeneStructure> mapStr2GeneStructure = getMapInfo2GeneStr();
			for (Entry<String, GeneStructure> entry : mapStr2GeneStructure.entrySet()) {
				mapGeneStructure2Str.put(entry.getValue(), entry.getKey());
			}
			return mapGeneStructure2Str;
		}
	}
}
