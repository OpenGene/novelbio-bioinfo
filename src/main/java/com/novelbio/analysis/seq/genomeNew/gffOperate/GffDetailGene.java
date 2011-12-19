package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.chipseq.regDensity.RegDensity;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
/**
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
public class GffDetailGene extends GffDetailAbs
{
	private final static Logger logger = Logger.getLogger(GffDetailGene.class);
	/**
	 * ����ת¼���Ľ����������0.6������һ������
	 */
	public final static double OVERLAP_RATIO = 0.6;
	public final static String INTRON = "intron";
	public final static String EXON_WITHOUT_UTR = "exon_without_utr";
	public final static String EXON = "exon";
	public final static String UTR5 = "5utr";
	public final static String UTR3 = "3utr";
	public final static String TSS = "tss";
	public final static String TES = "tes";
	/**
	 * ����ת¼����һ�������£���ô��������������ǽ�����������֣��ø÷��ŷָ�
	 */
	public final static String SEP_GENE_NAME = "/";
	
	int taxID = 0;
	/**
	 * �趨�����ת¼����յ�λ����Ϣ
	 * @param upStreamTSSbp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ3000bp
	 * @param downStreamTssbp �趨�����ת¼������γ��ȣ�Ĭ��Ϊ2000bp
	 * @param geneEnd3UTR �趨�����β��������ĳ��ȣ�Ĭ��Ϊ100bp
	 */
	public static void setCodLocation(int upStreamTSSbp, int downStreamTssbp, int geneEnd3UTR) {
		UpStreamTSSbp = upStreamTSSbp;
		DownStreamTssbp = downStreamTssbp;
		GeneEnd3UTR = geneEnd3UTR;
		GffGeneIsoInfo.setCodLocation(upStreamTSSbp, downStreamTssbp, geneEnd3UTR);
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
	 * ��0��ʼ����
	 * ����-1��ʾû�и�ת¼�� 
	 * @param isoName
	 * @return
	 */
	private int getIsoID(String isoName)
	{
		for (int i = 0; i < lsGffGeneIsoInfos.size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(i);
			if (gffGeneIsoInfo.getIsoName().equalsIgnoreCase(isoName)) {
				return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * ˳��洢ÿ��ת¼���ĵ��������
	 */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//�洢�ɱ���ӵ�mRNA
	
	public void setCoord(int coord) {
		this.coord = coord;
		ArrayList<GffGeneIsoInfo> lsGffInfo = getLsCodSplit();
		if (lsGffInfo == null || lsGffInfo.size() < 1) {
			return;
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffInfo) {
			gffGeneIsoInfo.setCoord(coord);
		}
	}
	
	/**
	 * @param chrID
	 * @param locString
	 * @param cis5to3
	 */
	protected GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}
	/**
	 * �����һ��ת¼�����exon���꣬<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ�
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 */
	protected void addExonUCSC(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonUCSC(locStart, locEnd);
	}
	/**
	 * ���ˮ�����Ͻ��GFF�ļ�
	 * �����һ��ת¼�����exon���꣬<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ�
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 */
	protected void addExonGFF(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFF(locStart, locEnd);
	}
	/**
	 * ���ˮ�����Ͻ��GFF�ļ�
	 * ��ת¼�����exon���꣬GFF3��exon�ĸ�ʽ��
	 * ��geneΪ������ʱ��exon�ǴӴ�С���е�
	 * �����exon��ʱ�������CDS��UTR֮�������ŵģ���ô�ͽ���CDS��UTR����һ�𣬷���һ��exon�� ����������Ͱ�ԭ������
	 */
	protected void addExonGFFCDSUTR(int locStart,int locEnd)
	{
		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		gffGeneIsoInfo.addExonGFFCDSUTR(locStart, locEnd);
	}
	
	/**
	 * �����һ��ת¼�����ATG��UAG���꣬<br>
	 * ������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 */
	protected void addATGUAG(int atg, int uag)
	{

		GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(lsGffGeneIsoInfos.size()-1);//include one special loc start number to end number
		if (Math.abs(atg - uag)<=1) {
			gffGeneIsoInfo.mRNA = false;
			atg = Math.min(atg, uag);
			uag = Math.min(atg, uag);
		}
		if (cis5to3) {
			gffGeneIsoInfo.ATGsite = Math.min(atg, uag);
			gffGeneIsoInfo.UAGsite = Math.max(atg, uag);
		}
		else {
			gffGeneIsoInfo.ATGsite = Math.max(atg, uag);
			gffGeneIsoInfo.UAGsite = Math.min(atg, uag);
		}
	}
	/**
	 * ֱ�����ת¼����֮����addcds()��������ת¼�����exon
	 */
	protected void addsplitlist(String splitName, String geneTpye) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(splitName,this, geneTpye);
		}
		else {
			gffGeneIsoInfo = new GffGeneIsoTrans(splitName,this, geneTpye);
		}
		gffGeneIsoInfo.setTaxID(this.taxID);
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
	}
	/**
	 * ֱ�����ת¼����֮����addcds()��������ת¼�����exon
	 */
	protected void addsplitlist(String splitName, String geneTpye, boolean cis5to3) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (cis5to3) {
			gffGeneIsoInfo = new GffGeneIsoCis(splitName,this, geneTpye);
		}
		else {
			gffGeneIsoInfo = new GffGeneIsoTrans(splitName,this, geneTpye);
		}
		gffGeneIsoInfo.setTaxID(this.taxID);
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
	 * ���Ϊnull��˵��û�з���һ��ת¼�������������Ҳ�з��������û�з���
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
    public GffGeneIsoInfo getIsolist(int splitnum)
    {  
    	return lsGffGeneIsoInfos.get(splitnum);//include one special loc start number to end number	
    }
    /**
     * ����ת¼����(UCSC��ʵ�����ǻ�����)<br>
     * û���򷵻�null
     */
    public GffGeneIsoInfo getIsolist(String splitID)
    {
    	int index = getIsoID(splitID);
    	if (index == -1) {
    		logger.info("cannotFind the ID: "+ splitID);
			return null;
		}
    	return lsGffGeneIsoInfos.get(index);//include one special loc start number to end number	
    }

    private int getLongestSplitID() {
    	if (lsGffGeneIsoInfos.size() == 1) {
			return 0;
		}
		ArrayList<Integer> lslength = new ArrayList<Integer>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			ArrayList<int[]> lsExon = gffGeneIsoInfo.getIsoInfo();
			if (lsExon.size() == 0)
				lslength.add(0);
			else
				lslength.add(Math.abs(lsExon.get(0)[0] - lsExon.get(lsExon.size()-1)[1]));
		}
		int max = lslength.get(0); int id = 0;
		for (int i = 0; i < lslength.size(); i++) {
			if (lslength.get(i) > max)
			{
				max = lslength.get(i);
				id = i;
			}
		}
		return id;
	}
    
	/**
	 * ��øû��������һ��ת¼������Ϣ
	 * 
	 * @return <br>
	 */
	public GffGeneIsoInfo getLongestSplit() {
		int id = getLongestSplitID();
		return lsGffGeneIsoInfos.get(id);
	}
	/**
	 * �������е�ת¼����Ϣ
	 * @param coord
	 */
	public ArrayList<GffGeneIsoInfo> getLsCodSplit() {
		return lsGffGeneIsoInfos;
	}
    /**
     * ��øû��������һ��ת¼���Ĳ����������Ϣ���Ѿ����ǹ�������������
     * @param type ָ��ΪINTRON,UTR5�ȣ��Ǹ���ĳ����������ֵ
     * @param num ���typeΪ"Intron"��"Exon"��ָ���ڼ���������������򷵻�0
     * num Ϊʵ�ʸ�����
     * ���5UTRֱ�ӷ���ȫ��5UTR
     * 3UTRҲֱ�ӷ���ȫ��3UTR
     * @return 
     */
	public int getTypeLength(String type,int num)  
	{
		GffGeneIsoInfo gffGeneIsoInfo = getLongestSplit();
		ArrayList<int[]> lsExon = gffGeneIsoInfo.getIsoInfo();
		int exonNum = lsExon.size();
		//TODO ���������Ҫ����0
		if (type.equals(INTRON)) {
			return Math.abs(lsExon.get(num)[0] - lsExon.get(num-1)[1]) - 1;
		}
		if (type.equals(EXON)) {
			return Math.abs(lsExon.get(num)[1] - lsExon.get(num)[0]) + 1;
		}
		if (type.equals(UTR5)) 
		{
			int FUTR=0;
			if (gffGeneIsoInfo.isCis5to3()) { //0    1     2     3     4     5   ÿ���������� 1 > 0      0    atg   1
				for (int i = 0; i <exonNum; i++) 
				{
					if(lsExon.get(i)[1] < gffGeneIsoInfo.getATGSsite())    // 0       1   atg    
						FUTR = FUTR + lsExon.get(i)[1] - lsExon.get(i)[0] + 1;
					else if (lsExon.get(i)[0] < gffGeneIsoInfo.getATGSsite() && lsExon.get(i)[1] >= gffGeneIsoInfo.getATGSsite())  //     0    atg    1 
						FUTR = FUTR + gffGeneIsoInfo.getATGSsite() - lsExon.get(i)[0];
					else if (lsExon.get(i)[0] >= gffGeneIsoInfo.getATGSsite())  //     atg   0       1   
						break;
				}
			}
			else { //5  4   3   2   1   0    ÿ���������� 0 > 1     1    gta   0
				for (int i = 0; i < exonNum; i++) 
				{
					if(lsExon.get(i)[1] > gffGeneIsoInfo.getATGSsite())  // gta   1      0
						FUTR = FUTR + lsExon.get(i)[0] - lsExon.get(i)[1] + 1;
					else if (lsExon.get(i)[0] > gffGeneIsoInfo.getATGSsite()  && lsExon.get(i)[1] <= gffGeneIsoInfo.getATGSsite() ) //   1     gta      0
						FUTR = FUTR + lsExon.get(i)[0] - gffGeneIsoInfo.getATGSsite();
					else if (lsExon.get(i)[0] <= gffGeneIsoInfo.getATGSsite())   //   1        0      gta 
						break;
				}
			}
			return FUTR;
		}
		if (type.equals(UTR3)) 
		{
			int TUTR=0;
			if (gffGeneIsoInfo.isCis5to3()) { //0    1     2     3     4     5   ÿ���������� 0 < 1      0    uag   1
				for (int i = exonNum - 1; i >=0 ; i--) 
				{
					if(lsExon.get(i)[0] > gffGeneIsoInfo.getUAGsite())  //      uag     0      1
						TUTR = TUTR + lsExon.get(i)[1] - lsExon.get(i)[0] + 1;
					else if (lsExon.get(i)[1] > gffGeneIsoInfo.getUAGsite() && lsExon.get(i)[0] <= gffGeneIsoInfo.getUAGsite())  //     0     uag    1
						TUTR = TUTR + lsExon.get(i)[1] - gffGeneIsoInfo.getUAGsite();
					else if (lsExon.get(i)[1] <= gffGeneIsoInfo.getUAGsite())   //   0      1     uag   
						break;
				}
			}
			else { //5  4   3   2   1   0    ÿ���������� 0 > 1      1    gau  0
				for (int i = exonNum-1; i >=0 ; i--) 
				{
					if(lsExon.get(i)[0] < gffGeneIsoInfo.getUAGsite())  //     1      0     gau
						TUTR = TUTR + lsExon.get(i)[0] - lsExon.get(i)[1] + 1;
					else if (lsExon.get(i)[0] >= gffGeneIsoInfo.getUAGsite() && lsExon.get(i)[1] < gffGeneIsoInfo.getUAGsite())  //     1    gau    0     
						TUTR = TUTR + gffGeneIsoInfo.getUAGsite() - lsExon.get(i)[1];
					else if (lsExon.get(i)[1] >= gffGeneIsoInfo.getUAGsite())   //   gau   1      0     
						break;
				}
			}
			return TUTR;
		}
		return -1000000;
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
	public String[] getInfo() {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		if (isCodInGenExtend()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend()) {
					hashCopedID.add(gffGeneIsoInfo.getCopedID());
				}
			}
			for (CopedID copedID : hashCopedID) {
				if (anno.equals("")) {
					anno[0] = copedID.getAccID();
					anno[1] = copedID.getSymbo();
					anno[2] = copedID.getDescription();
				}
				else {
					anno[0] = anno[0]+"//"+copedID.getAccID();
					anno[1] = anno[1]+"//"+copedID.getSymbo();
					anno[2] = anno[2]+"//"+copedID.getDescription();
				}
			}
			if (getLongestSplit().isCodInIsoExtend()) {
				anno[4] = getLongestSplit().getCodLocStr();
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend()) {
						anno[4] = gffGeneIsoInfo.getCodLocStr();
						break;
					}
				}
			}
		}
		return anno;
	}

	@Override
	public GffDetailGene clone() {
		GffDetailGene gffDetailGene = new GffDetailGene(getChrID(), getLocString(), cis5to3);
		this.clone(gffDetailGene);
		gffDetailGene.taxID = taxID;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			gffDetailGene.lsGffGeneIsoInfos.add(gffGeneIsoInfo.clone());
		}
		return gffDetailGene;
	}
	
	/**
	 * ���ڷ�Ӣ����Ŀ��������gffdetailGene��ת¼��ͷβ��������������ȡ��ԭ����ת¼����Ϣ
	 * �������iso�н������ǽ���С��0.3����ϲ�����������һ���µ�iso
	 * @param gffDetailGene
	 */
	public void addIso(GffDetailGene gffDetailGene)
	{
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfosFinal = new ArrayList<GffGeneIsoInfo>();
		ArrayList<GffGeneIsoInfo> lsIsoAdd = gffDetailGene.getLsCodSplit();
		ArrayList<GffGeneIsoInfo> lsIsoThis = getLsCodSplit();
		
		
		for (int i = 0; i < lsIsoThis.size(); i++) {
			if (i >= lsIsoAdd.size()) {
				break;
			}
			GffGeneIsoInfo gffGeneIsoInfoTmp = lsIsoThis.get(i);
			GffGeneIsoInfo gffGeneIsoInfoAddTmp = lsIsoAdd.get(i);
			

			GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.cloneDeep();
			GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.cloneDeep();
			//
			if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
				lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
				logger.error("��������һ�µ�gff���ܺϲ���"+ gffGeneIsoInfo.getIsoName() + " " + gffGeneIsoInfoAdd.getIsoName());
				continue;
			}
			//�ƺ������Ѿ��������������//////////////////////////////////////////////////////////////////////
			//�����һ��ת¼����β���͵ڶ���ת¼����ͷ���н���
			//////////////////////////////////////////////////////////////////////
			if (gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
				double[] region1 = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
				double[] region2 = new double[]{gffGeneIsoInfoAdd.getStartAbs(), gffGeneIsoInfoAdd.getEndAbs()};
				double[] overlapInfo = ArrayOperate.cmpArray(region1, region2);
				//����ص�����̫������ô�ͷֿ����������ת¼��
				if (overlapInfo[2] > OVERLAP_RATIO || overlapInfo[3] > OVERLAP_RATIO) {
					lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
					lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
					continue;
				}
				//����ص�����̳�����ô�ͽ��̵���ͷȥβ
				else {
					if (gffGeneIsoInfo.getIsoLen() >= gffGeneIsoInfoAdd.getIsoLen()) {
						while (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfoAdd.getIsoInfo().remove(0);
						}
					}
					else {
						while (gffGeneIsoInfo.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfo.getIsoInfo().remove(gffGeneIsoInfo.getIsoInfo().size() - 1);
						}
					}
				}
			}
			GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
			gffGeneIsoInfoTmpFinal.IsoName = gffGeneIsoInfoTmpFinal.getIsoName() + "///" + gffGeneIsoInfoAdd.getIsoName();
			//���exon����һ��ת¼�����ڲ���ֱ��ɾ��
			if (gffGeneIsoInfoAdd.isCis5to3()) {
				if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get(0)[0] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(gffGeneIsoInfoTmpFinal.getIsoInfo().size() - 1)[1]) {
					logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getIsoName());
				}
				gffGeneIsoInfoTmpFinal.getIsoInfo().addAll( gffGeneIsoInfoAdd.getIsoInfo());
			} else {
					if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get( gffGeneIsoInfoAdd.getIsoInfo().size() - 1)[1] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(0)[0]) {
						logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getIsoName());
					}
				gffGeneIsoInfoTmpFinal.getIsoInfo().addAll(0, gffGeneIsoInfoAdd.getIsoInfo());
			}
			gffGeneIsoInfoTmpFinal.sortIso();
			lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
		}		
		
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//���������յ�
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
		}
	}
	
	/**
	 * ѭ����ӣ�Ҳ����˵�����2vs2��ת¼������ӳ�4��iso
	 * ���ڷ�Ӣ����Ŀ��������gffdetailGene��ת¼��ͷβ��������������ȡ��ԭ����ת¼����Ϣ
	 * �������iso�н������ǽ���С��0.3����ϲ�����������һ���µ�iso
	 * @param gffDetailGene
	 */
	public void addIsoOls(GffDetailGene gffDetailGene)
	{
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfosFinal = new ArrayList<GffGeneIsoInfo>();
		ArrayList<GffGeneIsoInfo> lsIsoAdd = gffDetailGene.getLsCodSplit();
		ArrayList<GffGeneIsoInfo> lsIsoThis = getLsCodSplit();
		for (GffGeneIsoInfo gffGeneIsoInfoTmp : lsIsoThis) {
			for (GffGeneIsoInfo gffGeneIsoInfoAddTmp : lsIsoAdd) {
				GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.cloneDeep();
				GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.cloneDeep();
				//
				if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
					lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
					lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
					logger.error("��������һ�µ�gff���ܺϲ���"+ gffGeneIsoInfo.getIsoName() + " " + gffGeneIsoInfoAdd.getIsoName());
					continue;
				}
				//�ƺ������Ѿ��������������//////////////////////////////////////////////////////////////////////
				//�����һ��ת¼����β���͵ڶ���ת¼����ͷ���н���
				//////////////////////////////////////////////////////////////////////
				if (gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
					double[] region1 = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
					double[] region2 = new double[]{gffGeneIsoInfoAdd.getStartAbs(), gffGeneIsoInfoAdd.getEndAbs()};
					double[] overlapInfo = ArrayOperate.cmpArray(region1, region2);
					//����ص�����̫������ô�ͷֿ����������ת¼��
					if (overlapInfo[2] > OVERLAP_RATIO || overlapInfo[3] > OVERLAP_RATIO) {
						lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
						lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
						continue;
					}
					//����ص�����̳�����ô�ͽ��̵���ͷȥβ
					else {
						if (gffGeneIsoInfo.getIsoLen() >= gffGeneIsoInfoAdd.getIsoLen()) {
							while (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfoAdd.getIsoInfo().remove(0);
							}
						}
						else {
							while (gffGeneIsoInfo.getIsoInfo().size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfo.getIsoInfo().remove(gffGeneIsoInfo.getIsoInfo().size() - 1);
							}
						}
					}
				}
				GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
				gffGeneIsoInfoTmpFinal.IsoName = gffGeneIsoInfoTmpFinal.getIsoName() + "///" + gffGeneIsoInfoAdd.getIsoName();
				//���exon����һ��ת¼�����ڲ���ֱ��ɾ��
				if (gffGeneIsoInfoAdd.isCis5to3()) {
					if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get(0)[0] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(gffGeneIsoInfoTmpFinal.getIsoInfo().size() - 1)[1]) {
						logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getIsoName());
					}
					gffGeneIsoInfoTmpFinal.getIsoInfo().addAll( gffGeneIsoInfoAdd.getIsoInfo());
				} else {
						if (gffGeneIsoInfoAdd.getIsoInfo().size() > 0 && gffGeneIsoInfoTmpFinal.getIsoInfo().size() > 0 && gffGeneIsoInfoAdd.getIsoInfo().get( gffGeneIsoInfoAdd.getIsoInfo().size() - 1)[1] < gffGeneIsoInfoTmpFinal.getIsoInfo().get(0)[0]) {
							logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getIsoName());
						}
					gffGeneIsoInfoTmpFinal.getIsoInfo().addAll(0, gffGeneIsoInfoAdd.getIsoInfo());
				}
				gffGeneIsoInfoTmpFinal.sortIso();
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
			}
		}
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//���������յ�
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(0)[0] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(0)[0];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] < numberstart) {
				numberstart = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
			if (gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1] > numberend) {
				numberend = gffGeneIsoInfo.getIsoInfo().get(gffGeneIsoInfo.getIsoInfo().size() - 1)[1];
			}
		}
	}
	
	/**
	 * ȥ���ظ�Isoform
	 */
	public void removeDupliIso()
	{
		ArrayList<GffGeneIsoInfo> lsNew = new ArrayList<GffGeneIsoInfo>();
		
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			boolean flag = true;
			for (GffGeneIsoInfo gffGeneIsoInfo2 : lsNew) {
				if (gffGeneIsoInfo2.compIso(gffGeneIsoInfo.getIsoInfo())) {
					flag = false;
					break;
				}
			}
			if (flag) {
				lsNew.add(gffGeneIsoInfo);
			}
		}
		lsGffGeneIsoInfos = lsNew;
	}
	
	/**
	 * ���ڷ�Ӣ����Ŀ������µ�ת¼��
	 * ͬʱ�����趨�û����numberstart��numberend
	 * @param gffDetailGene
	 */
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo)
	{
		ArrayList<int[]> lsExonThis = gffGeneIsoInfo.getIsoInfo();
		if (lsExonThis == null || lsExonThis.size() == 0) {
			return;
		}
		
		if (cis5to3 != null && gffGeneIsoInfo.isCis5to3() != cis5to3) {
			cis5to3 = null;
		}
		for (GffGeneIsoInfo gffGeneIsoInfoOld : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfoOld.compIso(lsExonThis)) {
				return;
			}
		}
		
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		String IsoName = gffGeneIsoInfo.IsoName;
		int i = lsGffGeneIsoInfos.size();
		//�޸�����
		while (isContainsIso(IsoName)) {
			IsoName = FileOperate.changeFileSuffix(IsoName, "", ""+i).replace("/", "");
			i++;
		}
		gffGeneIsoInfo.IsoName = IsoName;

		if (numberstart < 0 || numberstart > gffGeneIsoInfo.getStartAbs()) {
			numberstart = gffGeneIsoInfo.getStartAbs();
		}
		if (numberend < 0 || numberend < gffGeneIsoInfo.getEndAbs()) {
			numberend = gffGeneIsoInfo.getEndAbs();
		}
	}
	
	public String getGTFformate(String title) {
		String geneGTF = "";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			gffGeneIsoInfo.sortIso();
			geneGTF = geneGTF + gffGeneIsoInfo.getGTFformat(getLocString().split(SEP_GENE_NAME)[0], title);
		}
		return geneGTF;
	}
	
	public String getGFFformate(String title) {
		String geneGFF = getChrID() + "\t" +title + "\tmRNA\t" + getNumberstart()+ "\t" + getNumberend()
        + "\t"+"."+"\t" +isCis5to3()+"\t.\t"+ "ID=" + getLocString().split(SEP_GENE_NAME)[0]
        +";Name="+getLocString().split(SEP_GENE_NAME)[0]+ ";Name="+getLocString().split(SEP_GENE_NAME)[0] + " \r\n";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			geneGFF = geneGFF + getChrID() + "\t" +title + "\tmRNA\t" +gffGeneIsoInfo.getStartAbs()+ "\t" + gffGeneIsoInfo.getEndAbs()
	        + "\t"+"."+"\t" +gffGeneIsoInfo.isCis5to3()+"\t.\t"+ "ID=" + gffGeneIsoInfo.getIsoName() 
	        +";Name="+gffGeneIsoInfo.getIsoName()+ ";Parent="+ getLocString().split(SEP_GENE_NAME)[0] + " \r\n";
			gffGeneIsoInfo.sortIso();
			geneGFF = geneGFF + gffGeneIsoInfo.getGFFformat(getLocString().split(SEP_GENE_NAME)[0], title);
		}
		return geneGFF;
	}
	
	/**
	 * �ж��Ƿ���ڸ����ֵ�ת¼��
	 * @param IsoName
	 */
	public boolean isContainsIso(String IsoName)
	{
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			if (gffGeneIsoInfo.getIsoName().equalsIgnoreCase(IsoName)) {
				return true;
			}
		}
		return false;
	}
}
