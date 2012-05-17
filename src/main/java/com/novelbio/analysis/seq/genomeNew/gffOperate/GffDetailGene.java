package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
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
public class GffDetailGene extends ListDetailAbs
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
	/** ����ת¼����һ�������£���ô��������������ǽ�����������֣��ø÷��ŷָ� */
	public final static String SEP_GENE_NAME = "/";
	/**  ͬһ��iso����ж�����֣����ø÷��ŷָ�ISO */
	private final static String SEP_ISO_NAME = "@//@";
	/** ˳��洢ÿ��ת¼���ĵ�������� */
	private ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();//�洢�ɱ���ӵ�mRNA
	int taxID = 0;
	
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
	private int getIsoID(String isoName) {
		for (int i = 0; i < lsGffGeneIsoInfos.size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = lsGffGeneIsoInfos.get(i);
			String[] tmpName = gffGeneIsoInfo.getName().split(SEP_ISO_NAME);
			for (String string : tmpName) {
				string = CopedID.removeDot(string);
				if(string.equalsIgnoreCase( CopedID.removeDot(isoName) ))
				{
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
	 * ����Tss��Χ����Ϊ����������Ϊ����
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int[] Tss) {
		if (Tss != null) {
			this.upTss = Tss[0];
			this.downTss = Tss[1];
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
				gffGeneIsoInfo.setTssRegion(upTss, downTss);
			}
		}
	}
	/**
	 * ����Tss��Χ����Ϊ����������Ϊ����
	 * @param upTss
	 * @param downTss
	 */
	public void setTesRegion(int[] Tes) {
		if (Tes != null) {
			this.upGeneEnd3UTR = Tes[0];
			this.downGeneEnd3UTR = Tes[1];
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
				gffGeneIsoInfo.setTesRegion(upGeneEnd3UTR, downGeneEnd3UTR);
			}
		}
	}
	/**
	 * @param chrID
	 * @param locString
	 * @param cis5to3
	 */
	public GffDetailGene(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
	}
	/**
	 * �����һ��ת¼�����exon���꣬<br>
	 * ֻ��Ҫע�ⰴ�մ���װ��Ҳ����˵�������Ҫ��С����ļӣ�����Ӵ�С�ļ�
	 * Ȼ�����������һ�������ʱ�򣬲�����Ҫ�ֱ��С����������gene�����Զ��ж�
	 * <b>�������һ��û��ת¼���ģ��������һ��gene��������Ϊpseudo</b>
	 */
	protected void addExon(int locStart,int locEnd) {
		if (lsGffGeneIsoInfos.size() == 0) {//�������һ��û��ת¼���ģ��������һ��gene��������Ϊpseudo
			addsplitlist(getName(), "pseudo");
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
	public int getTypeLength(String type,int num) {
		GffGeneIsoInfo gffGeneIsoInfo = getLongestSplit();
		//TODO ���������Ҫ����0
		if (type.equals(INTRON)) {
			return gffGeneIsoInfo.getLenIntron(num);
		}
		if (type.equals(EXON)) {
			return gffGeneIsoInfo.getLenExon(num);
		}
		if (type.equals(UTR5)) 
		{
			return gffGeneIsoInfo.getLenUTR5();
		}
		if (type.equals(UTR3)) 
		{
			return gffGeneIsoInfo.getLenUTR3();
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
	public String[] getInfo(int coord) {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		if (isCodInGeneExtend(coord)) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
				if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
					hashCopedID.add(gffGeneIsoInfo.getCopedID());
				}
			}
			for (CopedID copedID : hashCopedID) {
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
				anno[4] = getLongestSplit().getCodLocStr(coord);
			}
			else {
				for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend(coord)) {
						anno[4] = gffGeneIsoInfo.getCodLocStr(coord);
						break;
					}
				}
			}
		}
		return anno;
	}
	
	/**
	 * ���ڷ�Ӣ����Ŀ��������gffdetailGene��ת¼��ͷβ��������������ȡ��ԭ����ת¼����Ϣ
	 * �������iso�н������ǽ���С��0.3����ϲ�����������һ���µ�iso
	 * @param gffDetailGene
	 */
	public void addIso(GffDetailGene gffDetailGene) {
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfosFinal = new ArrayList<GffGeneIsoInfo>();
		ArrayList<GffGeneIsoInfo> lsIsoAdd = gffDetailGene.getLsCodSplit();
		ArrayList<GffGeneIsoInfo> lsIsoThis = getLsCodSplit();
		
		
		for (int i = 0; i < lsIsoThis.size(); i++) {
			if (i >= lsIsoAdd.size()) {
				break;
			}
			GffGeneIsoInfo gffGeneIsoInfoTmp = lsIsoThis.get(i);
			GffGeneIsoInfo gffGeneIsoInfoAddTmp = lsIsoAdd.get(i);
			

			GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.clone();
			GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.clone();
			//
			if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
				lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
				logger.error("��������һ�µ�gff���ܺϲ���"+ gffGeneIsoInfo.getName() + " " + gffGeneIsoInfoAdd.getName());
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
					if (gffGeneIsoInfo.getLen() >= gffGeneIsoInfoAdd.getLen()) {
						while (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfoAdd.remove(0);
						}
					}
					else {
						while (gffGeneIsoInfo.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
							gffGeneIsoInfo.remove(gffGeneIsoInfo.size() - 1);
						}
					}
				}
			}
			GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
			gffGeneIsoInfoTmpFinal.setName( gffGeneIsoInfoTmpFinal.getName() + "///" + gffGeneIsoInfoAdd.getName() );
			//���exon����һ��ת¼�����ڲ���ֱ��ɾ��
			if (gffGeneIsoInfoAdd.isCis5to3()) {
				if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get(0).getStartCis() < gffGeneIsoInfoTmpFinal.get(gffGeneIsoInfoTmpFinal.size() - 1).getEndCis()) {
					logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getName());
				}
				gffGeneIsoInfoTmpFinal.addAll( gffGeneIsoInfoAdd);
			} else {
					if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get( gffGeneIsoInfoAdd.size() - 1).getEndCis() < gffGeneIsoInfoTmpFinal.get(0).getStartCis()) {
						logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getName());
					}
				gffGeneIsoInfoTmpFinal.addAll(0, gffGeneIsoInfoAdd);
			}
			gffGeneIsoInfoTmpFinal.sort();
			lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
		}		
		
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//���������յ�
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.get(0).getStartCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(0).getStartCis() > numberend) {
				numberend = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() > numberend) {
				numberend = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
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
				GffGeneIsoInfo gffGeneIsoInfo = gffGeneIsoInfoTmp.clone();
				GffGeneIsoInfo gffGeneIsoInfoAdd = gffGeneIsoInfoAddTmp.clone();
				//
				if (gffGeneIsoInfo.isCis5to3() != gffGeneIsoInfoAdd.isCis5to3()) {
					lsGeneIsoInfosFinal.add(gffGeneIsoInfo);
					lsGeneIsoInfosFinal.add(gffGeneIsoInfoAdd);
					logger.error("��������һ�µ�gff���ܺϲ���"+ gffGeneIsoInfo.getName() + " " + gffGeneIsoInfoAdd.getName());
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
						if (gffGeneIsoInfo.getLen() >= gffGeneIsoInfoAdd.getLen()) {
							while (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfoAdd.remove(0);
							}
						}
						else {
							while (gffGeneIsoInfo.size() > 0 && gffGeneIsoInfo.getEndAbs() > gffGeneIsoInfoAdd.getStartAbs()) {
								gffGeneIsoInfo.remove(gffGeneIsoInfo.size() - 1);
							}
						}
					}
				}
				GffGeneIsoInfo gffGeneIsoInfoTmpFinal = gffGeneIsoInfo;
				gffGeneIsoInfoTmpFinal.setName( gffGeneIsoInfoTmpFinal.getName() + "///" + gffGeneIsoInfoAdd.getName()  );
				//���exon����һ��ת¼�����ڲ���ֱ��ɾ��
				if (gffGeneIsoInfoAdd.isCis5to3()) {
					if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get(0).getStartCis() < gffGeneIsoInfoTmpFinal.get(gffGeneIsoInfoTmpFinal.size() - 1).getEndCis()) {
						logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getName());
					}
					gffGeneIsoInfoTmpFinal.addAll( gffGeneIsoInfoAdd);
				} else {
						if (gffGeneIsoInfoAdd.size() > 0 && gffGeneIsoInfoTmpFinal.size() > 0 && gffGeneIsoInfoAdd.get( gffGeneIsoInfoAdd.size() - 1).getEndCis() < gffGeneIsoInfoTmpFinal.get(0).getStartCis()) {
							logger.error("�����ص�ת¼����" + gffGeneIsoInfoAdd.getName());
						}
					gffGeneIsoInfoTmpFinal.addAll(0, gffGeneIsoInfoAdd);
				}
				gffGeneIsoInfoTmpFinal.sort();
				lsGeneIsoInfosFinal.add(gffGeneIsoInfoTmpFinal);
			}
		}
		lsGffGeneIsoInfos = lsGeneIsoInfosFinal;		
		//���������յ�
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfosFinal) {
			if (gffGeneIsoInfo.get(0).getStartCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(0).getStartCis() > numberend) {
				numberend = gffGeneIsoInfo.get(0).getStartCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() < numberstart) {
				numberstart = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
			}
			if (gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis() > numberend) {
				numberend = gffGeneIsoInfo.get(gffGeneIsoInfo.size() - 1).getEndCis();
			}
		}
	}
	/**
	 * ȥ���ظ�Isoform
	 */
	public void removeDupliIso()
	{
		HashMap<GffGeneIsoInfo, Integer> hashIso = new HashMap<GffGeneIsoInfo, Integer>();
		ArrayList<GffGeneIsoInfo> lsResult = new ArrayList<GffGeneIsoInfo>();
		int numIso = 0;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			//��������ظ�iso������ԭ���Ǹ�iso��lsResult�����λ�ã����ҽ�ԭ����iso����������
			if (hashIso.containsKey(gffGeneIsoInfo)) {
				int num = hashIso.get(gffGeneIsoInfo);
				//���ԭ����iso
				GffGeneIsoInfo gffGeneIsoInfoInside = lsResult.get(num);
				gffGeneIsoInfoInside.setName(gffGeneIsoInfoInside.getName() + SEP_ISO_NAME + gffGeneIsoInfo.getName());
				continue;
			}
			lsResult.add(gffGeneIsoInfo);
			hashIso.put(gffGeneIsoInfo, numIso); numIso ++;
		}
		this.lsGffGeneIsoInfos = lsResult;
	}
	
	/**
	 * Ч�ʵ��£��ȴ��Ż�
	 * ���ڷ�Ӣ����Ŀ������µ�ת¼��
	 * ͬʱ�����趨�û����numberstart��numberend
	 * @param gffDetailGene
	 */
	public void addIso(GffGeneIsoInfo gffGeneIsoInfo)
	{
		if (gffGeneIsoInfo == null || gffGeneIsoInfo.size() == 0) {
			return;
		}
		
		if (cis5to3 != null && gffGeneIsoInfo.isCis5to3() != cis5to3) {
			cis5to3 = null;
		}
		for (GffGeneIsoInfo gffGeneIsoInfoOld : lsGffGeneIsoInfos) {
			//�Ƚ�����list�Ƿ�һ�£�exon��equalsֻ�Ƚ�����յ�
			if (gffGeneIsoInfoOld.compIso(gffGeneIsoInfo)) {
				return;
			}
		}
		lsGffGeneIsoInfos.add(gffGeneIsoInfo);
		String IsoName = gffGeneIsoInfo.getName();
		int i = lsGffGeneIsoInfos.size();
		//�޸�����
		while (isContainsIso(IsoName)) {
			IsoName = FileOperate.changeFileSuffix(IsoName, "", ""+i).replace("/", "");
			i++;
		}
		gffGeneIsoInfo.setName(IsoName);

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
			gffGeneIsoInfo.sort();
			geneGTF = geneGTF + gffGeneIsoInfo.getGTFformat(getName().split(SEP_GENE_NAME)[0], title);
		}
		return geneGTF;
	}
	
	public String getGFFformate(String title) {
		String strand = "+";
		if (!isCis5to3()) {
			strand = "-";
		}
		String geneGFF = getParentName() + "\t" +title + "\tgene\t" + getStartAbs()+ "\t" + getEndAbs()
        + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=" + getName().split(SEP_GENE_NAME)[0]
        +";Name="+getName().split(SEP_GENE_NAME)[0]+ ";Name="+getName().split(SEP_GENE_NAME)[0] + " \r\n";
		for (GffGeneIsoInfo gffGeneIsoInfo : getLsCodSplit()) {
			String strandmRNA = "+";
			if (!gffGeneIsoInfo.isCis5to3()) {
				strandmRNA = "-";
			}
			geneGFF = geneGFF + getParentName() + "\t" +title + "\tmRNA\t" +gffGeneIsoInfo.getStartAbs()+ "\t" + gffGeneIsoInfo.getEndAbs()
	        + "\t"+"."+"\t" +strandmRNA+"\t.\t"+ "ID=" + gffGeneIsoInfo.getName() 
	        +";Name="+gffGeneIsoInfo.getName()+ ";Parent="+ getName().split(SEP_GENE_NAME)[0] + " \r\n";
			gffGeneIsoInfo.sort();
			geneGFF = geneGFF + gffGeneIsoInfo.getGFFformat(getName().split(SEP_GENE_NAME)[0], title);
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
			return super.getCod2End(coord);
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
		result.lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfos) {
			result.getLsCodSplit().add(gffGeneIsoInfo);
		}
		return result;
	}
}
