package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.test.mytest;

/**
 * ����飬Ĭ����ȫ�����Ǹû���û��5UTR��3UTR
 * 
 * @author zong0jie
 * 
 */
public class GffCodGeneDU extends ListCodAbsDu<GffDetailGene, GffCodGene> {
	private static Logger logger = Logger.getLogger(GffCodGeneDU.class);
	/** �Ƿ���Ҫ��ѯIso */
	private boolean flagSearchAnno = false;
	/** �Ƿ���Ҫ��ѯHash */
	private boolean flagSearchHash = false;
	/** �Ƿ���Ҫ���²�ѯ */
	private boolean flagSearch = false;
	int[] tss = null;
	int[] tes = null;
	boolean geneBody = true;
	boolean utr5 = false;
	boolean utr3 = false;
	boolean exon = false;
	boolean intron = false;
	/** ��������ע����Ϣ */
	ArrayList<String[]> lsAnno;
	/**
	 * �������ѡ�񵽵�gene key: geneName + sep +chrID
	 * */
	HashSet<GffDetailGene> hashGffDetailGene;
	/**
	 * �趨tss�ķ�Χ��Ĭ��Ϊnull
	 * 
	 * @param tss
	 */
	public void setTss(int[] tss) {
		// һģһ���ͷ���
		if (this.tss != null && tss != null) {
			if (this.tss[0] == tss[0] && this.tss[1] == tss[1]) {
				return;
			}
		}
		this.tss = tss;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * �趨tes�ķ�Χ��Ĭ��Ϊnull
	 * 
	 * @param tes
	 */
	public void setTes(int[] tes) {
		// һģһ���ͷ���
		if (this.tes != null && tes != null) {
			if (this.tes[0] == tes[0] && this.tes[1] == tes[1]) {
				return;
			}
		}
		this.tes = tes;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * �趨genebody�ķ�Χ��Ĭ��Ϊtrue��Ҳ���ǻ��genebody����Ϣ
	 * 
	 * @param geneBody
	 */
	public void setGeneBody(boolean geneBody) {
		if (this.geneBody == geneBody) {
			return;
		}
		this.geneBody = geneBody;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * �趨�Ƿ�ץȡ����5UTR��gene��Ĭ��Ϊfalse����Ϊgenebody�Ѿ���true��ֻ��genebodyΪfalseʱ�Ż�������
	 * 
	 * @param utr5
	 */
	public void setUTR5(boolean utr5) {
		if (this.utr5 == utr5) {
			return;
		}
		this.utr5 = utr5;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * �趨�Ƿ�ץȡ����3UTR��gene��Ĭ��Ϊfalse����Ϊgenebody�Ѿ���true��ֻ��genebodyΪfalseʱ�Ż�������
	 * 
	 * @param utr3
	 */
	public void setUTR3(boolean utr3) {
		if (this.utr3 == utr3) {
			return;
		}
		this.utr3 = utr3;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * �趨����exon��gene��Ĭ��Ϊfalse����Ϊgenebody�Ѿ���true��ֻ��genebodyΪfalseʱ�Ż�������
	 * 
	 * @param exon
	 */
	public void setExon(boolean exon) {
		if (this.exon == exon) {
			return;
		}
		this.exon = exon;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	/**
	 * �趨����intron��gene��Ĭ��Ϊfalse����Ϊgenebody�Ѿ���true��ֻ��genebodyΪfalseʱ�Ż�������
	 * 
	 * @param intron
	 */
	public void setIntron(boolean intron) {
		if (this.intron == intron) {
			return;
		}
		this.intron = intron;
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}

	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail,
			GffCodGene gffCod1, GffCodGene gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}

	public GffCodGeneDU(GffCodGene gffCod1, GffCodGene gffCod2) {
		super(gffCod1, gffCod2);
	}

	/**
	 * ���gffDetailGene�ľ�����Ϣ�������gffDetailGene�������copedID�����á�///���ָ�
	 * @return 0��accID <br>
	 *         1��symbol<br>
	 *         2��description<br>
	 *         3�������Ǿ�����Ϣ���м���covered
	 */
	public ArrayList<String[]> getAnno() {
		if (flagSearchAnno && lsAnno != null) {
			return lsAnno;
		}
		flagSearchAnno = true;
		flagSearchHash = true;
		lsAnno = new ArrayList<String[]>();
		getStructureGene(tss, tes, geneBody, utr5, utr3, exon, intron);
		
		hashGffDetailGene = new LinkedHashSet<GffDetailGene>();
		//TODO: �����޸�tss��tes��gffDetailgeneҪ�޸�tss��tes��gffisoҲҪ�޸�tss��tes
		getStructureGene(tss, tes, geneBody, utr5, utr3, exon, intron);
		for (GffDetailGene gffDetailGene : lsGffDetailGenesUp) {
			hashGffDetailGene.add(gffDetailGene);
			if (gffCod1.getCoord() == 80391798) {
				System.out.println("stop");
			}
			String[] anno = getAnnoCod(gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				if (hashGffDetailGene.contains(gffDetailGene)) {
//					logger.error("lsmid�������һ����һ����iso�����鸴��");
					continue;
				}
				hashGffDetailGene.add(gffDetailGene);
				String[] anno = getAnnoMid(gffDetailGene);
				lsAnno.add(anno);
			}
		}
		for (GffDetailGene gffDetailGene : lsGffDetailGenesDown) {
			if (hashGffDetailGene.contains(gffDetailGene))
				continue;
			hashGffDetailGene.add(gffDetailGene);
			String[] anno = getAnnoCod(gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		return lsAnno;
	}
	
	private void setHashCoveredGenInfo() {
		if (flagSearchHash && hashGffDetailGene != null) {
			return;
		}
		flagSearchHash = true;
		hashGffDetailGene = new LinkedHashSet<GffDetailGene>();
		//TODO: �����޸�tss��tes��gffDetailgeneҪ�޸�tss��tes��gffisoҲҪ�޸�tss��tes
		getStructureGene(tss, tes, geneBody, utr5, utr3, exon, intron);
		for (GffDetailGene gffDetailGene : lsGffDetailGenesUp) {
			hashGffDetailGene.add(gffDetailGene);
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				if (hashGffDetailGene.contains(gffDetailGene)) {
//					logger.error("lsmid�������һ����һ����iso�����鸴��");
					continue;
				}
				hashGffDetailGene.add(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsGffDetailGenesDown) {
			if (hashGffDetailGene.contains(gffDetailGene))
				continue;
			hashGffDetailGene.add(gffDetailGene);
		}
	}
	
	/**
	 * ���gffDetailGene�ľ�����Ϣ�������gffDetailGene�������copedID�����á�///���ָ�
	 * 
	 * @param gffDetailGene
	 * @return 0��accID<br>
	 *         1��symbol<br>
	 *         2��description<br>
	 *         3��������ʽ�Ķ�λ����
	 */
	private String[] getAnnoCod(int coord, GffDetailGene gffDetailGene, String peakPointInfo) {
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";

		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			GeneID copedID = new GeneID(gffGeneIsoInfo.getName(), gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbol();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", "");
		anno[1] = anno[1].replaceFirst("///", "");
		anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = peakPointInfo + gffDetailGene.getLongestSplit().getCodLocStr(coord);
		return anno;
	}

	/**
	 * ���gffDetailGene�ľ�����Ϣ�������gffDetailGene�������copedID�����á�///���ָ�
	 * 
	 * @param gffDetailGene
	 * @return 0��accID<br>
	 *         1��symbol<br>
	 *         2��description<br>
	 *         3��Covered
	 */
	private String[] getAnnoMid(GffDetailGene gffDetailGene) {
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";

		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
					gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbol();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", "");
		anno[1] = anno[1].replaceFirst("///", "");
		anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = "Covered";
		return anno;
	}

	/**
	 * �����ǵ�ָ������Ļ���ȫ����ȡ����������
	 * 
	 * @param Tss
	 *            Tss�����ζ���bp������Ϊ��������Ϊ������ ������Ϊ������ʾֻѡȡTss���Σ�������Ϊ������ʾֻѡȡTss����
	 * @param Tes
	 *            ͬTss
	 * @param geneBody
	 *            �Ƿ���genebody
	 * @param Exon
	 *            ��genebodyΪfalseʱ���Ƿ񸲸�exon
	 * @param Intron
	 *            ��genebodyΪfalseʱ���Ƿ񸲸�exon
	 * @return û���򷵻�һ��sizeΪ0��set
	 */
	private void getStructureGene(int[] Tss, int[] Tes, boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		if (flagSearch) {
			return;
		}
		flagSearch = true;
		int tssUp = 0;
		int tesDown = 0;
		if (Tss != null) {
			tssUp = Tss[0];
		}
		if (Tes != null) {
			tesDown = Tes[1];
		}
		setSameGeneDetail(tssUp, tesDown);
		ArrayList<GffDetailGene> lsRemove = new ArrayList<GffDetailGene>();
		for (GffDetailGene gffDetailGenes : lsGffDetailGenesUp) {
			GffDetailGene gffDetailGene = gffDetailGenes.clone();
			if (!isInRegion2Cod(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
//				logger.error("��������ǲ�����ȷ����������Ҫɾ����iso");
				lsRemove.add(gffDetailGene);
//				lsGffDetailGenesUp.remove(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			lsGffDetailGenesUp.remove(gffDetailGene);
		}
		lsRemove.clear();
		for (GffDetailGene gffDetailGenes : lsGffDetailGenesDown) {
			GffDetailGene gffDetailGene = gffDetailGenes.clone();
			if (!isInRegion2Cod(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
//				logger.error("��������ǲ�����ȷ����������Ҫɾ����iso");
				lsRemove.add(gffDetailGene);
//				lsGffDetailGenesDown.remove(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			lsGffDetailGenesDown.remove(gffDetailGene);
		}
	}
	/** Up��Down֮��û�н��� */
	LinkedHashSet<GffDetailGene> lsGffDetailGenesUp = null;
	/** Up��Down֮��û�н��� */
	LinkedHashSet<GffDetailGene> lsGffDetailGenesDown = null;
	/**
	 * ��ö˵���ǣ�浽�Ļ���
	 * @return lsGffDetailGenes - gffDetailGene ��֮��ص�����
	 */
	private void setSameGeneDetail(int tssUp, int tesDown) {
		lsGffDetailGenesUp = new LinkedHashSet<GffDetailGene>();
		lsGffDetailGenesDown = new LinkedHashSet<GffDetailGene>();
		// //////////////// up /////////////////////////////////
		if (this.gffCod1.isInsideUpExtend(tssUp, tesDown)) {
			lsGffDetailGenesUp.add(gffCod1.getGffDetailUp());
		}
		// //////////////////// this /////////////////////////////////
		if (this.gffCod1.isInsideLoc()) {
			lsGffDetailGenesUp.add(gffCod1.getGffDetailThis());
		}
		if (this.gffCod1.isInsideDownExtend(tssUp, tesDown)) {
			lsGffDetailGenesUp.add(gffCod1.getGffDetailDown());
		}
		// //////////////////////////// cod2
		//˵����һ�����뱾�㲻��ͬһ��������
		if (this.gffCod2.isInsideUpExtend(tssUp, tesDown) && !lsGffDetailGenesUp.contains(gffCod2.getGffDetailUp())) {
			lsGffDetailGenesDown.add(gffCod2.getGffDetailUp());
		}
		if (this.gffCod2.isInsideLoc() && !lsGffDetailGenesUp.contains(gffCod2.getGffDetailThis())) {
			lsGffDetailGenesDown.add(gffCod2.getGffDetailThis());
		}
		if (this.gffCod2.isInsideDownExtend(tssUp, tesDown) && !lsGffDetailGenesUp.contains(gffCod2.getGffDetailDown())) {
			lsGffDetailGenesDown.add(gffCod2.getGffDetailDown());
		}
	}
	/**
	 * <b>�ڲ���ɾ��iso��Ϣ�����������gffDetail������clone��</b> ʹ��ǰ���ж�cod�Ƿ���������ͬ��gffDetailGene��
	 * ��������������ͬһ�������ڲ������ʱ Ч���Ե͵��Ǻ�ȫ�棬ÿ��isoform�����ж�
	 * 
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5
	 *            ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ��
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion2Cod(GffDetailGene gffDetailGene, int[] Tss,int[] Tes, boolean geneBody, Boolean UTR5, boolean UTR3,
			boolean Exon, boolean Intron) {
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = null;
		flag = getInRegion2Cod(getGffCod1().getCoord(), getGffCod2().getCoord(), gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);

		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0; i--) {
			if (flag[i] == 0) {
				gffDetailGene.removeIso(i);
			}
			if (flagResult == true) {
				continue;
			}
			if (flag[i] == 1) {
				flagResult = true;
			}
		}
		return flagResult;
	}
	/**
	 * ����coord����ͬһ��gffDetailGene��ʱ�����ж�
	 * 
	 * @param gffDetailGene1
	 * @param gffDetailGene2
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private int[] getInRegion2Cod(int coord1, int coord2, GffDetailGene gffDetailGene, int[] Tss, int[] Tes, 
			boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		// һ������㣬һ�����յ�
		int coordStart = 0;
		int coordEnd = 0;
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = new int[gffDetailGene.getLsCodSplit().size()];
		for (int i = 0; i < gffDetailGene.getLsCodSplit().size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLsCodSplit().get(i);
			// �������ͬһ��GffGeneDetail������ÿһ��gffGeneDetail����һ��cod������ cod1 ����ֵ< cod2
			// ����ֵ
			// ��ô������Ҫ��cod1�ڻ����е�λ��С��cod2�����Ե�gene�����ʱ����Ҫ��cod����
			if (gffDetailGene.getLsCodSplit().get(i).isCis5to3()) {
				coordStart = Math.min(coord1, coord2);
				coordEnd = Math.max(coord1, coord2);
			} else {
				coordStart = Math.max(coord1, coord2);
				coordEnd = Math.min(coord1, coord2);
			}
			if (Tss != null) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= Tss[1]
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= Tss[0]) {
					flag[i] = 1;
				}
			}
			if (Tes != null) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getCod2Tes(coordStart) <= Tes[1]
						&& gffGeneIsoInfo.getCod2Tes(coordEnd) >= Tes[0]) {
					flag[i] = 1;
				}
			}
			if (geneBody) {
				// �ڻ������ο϶����ڻ�������
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes(coordStart) <= 0
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0) {
					flag[i] = 1;
				}
			}
			if (UTR5) {
				if (flag[i] == 0
						&& geneBody == false
						&& GffGeneIsoInfo.hashMRNA.contains(gffGeneIsoInfo.getGeneType())
						&& gffGeneIsoInfo.getCod2ATG(coordStart) <= 0
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= 0
						&& (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordStart) 
						|| gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordStart)
								&& (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON 
					                 || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_EXON))) 
				{
					flag[i] = 1;
				}
			}
			if (UTR3) {
				if (flag[i] == 0
						&& geneBody == false
						&& GffGeneIsoInfo.hashMRNA.contains(gffGeneIsoInfo.getGeneType())
						&& gffGeneIsoInfo.getCod2Tes(coordStart) <= 0
						&& gffGeneIsoInfo.getCod2UAG(coordEnd) >= 0
						&& (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd) 
						|| gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd)
								&& (gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON 
								|| gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_EXON))) 
				{
					flag[i] = 1;
				}
			}
			//λ�������˿϶��ǰ�����
			if (flag[i] == 0 && (Exon || Intron)) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) {
					flag[i] = 1;
				}
			}
			if (Exon) {
				if (flag[i] == 0
						&& 
				      (
						(gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) 
						|| gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd) 
					    || (gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd)
								&&
								gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_EXON )
					 )
				)
				{
					flag[i] = 1;
				}
			}
			if (Intron) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getExonNum() > 1 &&
				( (gffGeneIsoInfo.getNumCodInEle(coordStart) != gffGeneIsoInfo.getNumCodInEle(coordEnd)
				    && gffGeneIsoInfo.getNumCodInEle(coordStart) != 0 && gffGeneIsoInfo.getNumCodInEle(coordEnd) != 0)
				|| (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0)
				||
				(gffGeneIsoInfo.getNumCodInEle(coordStart) == gffGeneIsoInfo.getNumCodInEle(coordEnd) 
						      && gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_INTRON  )
				|| (gffGeneIsoInfo.getNumCodInEle(coordStart) == 0 && (gffGeneIsoInfo.getNumCodInEle(coordEnd) >= 2 
					          || gffGeneIsoInfo.getCodLoc(coordEnd) == GffGeneIsoInfo.COD_LOC_INTRON))
				|| (gffGeneIsoInfo.getNumCodInEle(coordEnd) == 0 
						      && (gffGeneIsoInfo.getNumCodInEle(coordStart) <= gffGeneIsoInfo.getExonNum() - 1 
						          || gffGeneIsoInfo.getCodLoc(coordStart) == GffGeneIsoInfo.COD_LOC_INTRON))
			   )
		) {
					flag[i] = 1;
				}
			}
		}
		return flag;
	}
	
	public HashSet<GffDetailGene> getCoveredGffGene() {
		setHashCoveredGenInfo();
		return hashGffDetailGene;
	}
	/**
	 * ��ǰ����趨�������з���Ҫ���gene��ȫ����ȡ����
	 * @return
	 */
	public ArrayList<GeneID> getCoveredGene() {
		setHashCoveredGenInfo();
		// ����ȥ�����
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		ArrayList<GeneID> lsCopedIDs = new ArrayList<GeneID>();
	
		for (GffDetailGene gffDetailGene : hashGffDetailGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
						getGffCodLeft().getGffDetailUp().getTaxID(),
						false);
				if (hashCopedID.contains(copedID)) {
					continue;
				}
				hashCopedID.add(copedID);
				lsCopedIDs.add(copedID);
			}
		}
		return lsCopedIDs;
	}

	/**
	 * ���������˵��У��漰��Tes�Ļ���ȫ����ȡ���� ������iso
	 * 
	 * @return
	 */
	@Deprecated
	public Set<GffDetailGene> getTESGene(int[] tes) {
		/**
		 * �����صĻ���
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		// ��ǰ�������ת¼����Χ��
		if (gffCod1 != null) {
			// ��һ�������й�ϵ
			if (isUpTes(gffCod1.getGffDetailUp(), gffCod1.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			// �������ϵ
			if (isUpTes(gffCod1.getGffDetailThis(), gffCod1.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				setGffDetailGenes.add(gffDetailGene);
			}
		}
		if (gffCod2 != null) {
			// ��һ�������й�ϵ
			if (isDownTes(gffCod2.getGffDetailThis(), gffCod2.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			// �������ϵ
			if (isDownTes(gffCod2.getGffDetailDown(), gffCod2.getCoord(), tes)) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}

	/**
	 * ����� ���������˵��У��漰��Tes�Ļ���ȫ����ȡ���� ������iso
	 * 
	 * @return
	 */
	@Deprecated
	private boolean isUpTes(GffDetailGene gffDetailGene, int coord, int[] tes) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTesRegion(tes);
		if (gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoExtend(coord)
				|| (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoGenEnd(coord))) {
			return true;
		}
		return false;
	}
	@Deprecated
	private boolean isDownTes(GffDetailGene gffDetailGene, int coord, int[] tes) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTesRegion(tes);
		if (!gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoExtend(coord)
				|| (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoGenEnd(coord))) {
			return true;
		}
		return false;
	}

	/**
	 * ����� ���������˵��У��漰��Tss�Ļ���ȫ����ȡ����
	 * 
	 * @return
	 */
	@Deprecated
	public HashSet<GffDetailGene> getTSSGene(int[] tss) {
		/**
		 * �����صĻ���
		 */
		HashSet<GffDetailGene> setGffDetailGenes = new LinkedHashSet<GffDetailGene>();
		// ��ǰ�������ת¼����Χ��
		if (gffCod1 != null) {
			// ��һ�������й�ϵ
			if (isUpTss(gffCod1.getGffDetailUp(), gffCod1.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			// �������ϵ
			if (isUpTss(gffCod1.getGffDetailThis(), gffCod1.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}

		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				setGffDetailGenes.add(gffDetailGene);
			}
		}

		if (gffCod2 != null) {
			// ��һ�������й�ϵ
			if (isDownTss(gffCod2.getGffDetailThis(), gffCod2.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			// �������ϵ
			if (isDownTss(gffCod2.getGffDetailDown(), gffCod2.getCoord(), tss)) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	@Deprecated
	private boolean isUpTss(GffDetailGene gffDetailGene, int coord, int[] tss) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTssRegion(tss);
		if (gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoTss(coord)
				|| (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoExtend(coord))) {
			return true;
		}
		return false;
	}
	@Deprecated
	private boolean isDownTss(GffDetailGene gffDetailGene, int coord, int[] tss) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGeneExtend(coord)) {
			return false;
		}
		gffDetailGene.setTssRegion(tss);
		if (!gffDetailGene.getLongestSplit().isCis5to3()
				&& gffDetailGene.getLongestSplit().isCodInIsoTss(coord)
				|| (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene
						.getLongestSplit().isCodInIsoExtend(coord))) {
			return true;
		}
		return false;
	}

	/**
	 * �������и��ǵ��Ļ����copedID
	 * 
	 * @return
	 */
	@Deprecated
	public ArrayList<GeneID> getAllCoveredGenes() {
		// ����ȥ�����
		HashSet<GeneID> hashCopedID = new HashSet<GeneID>();
		ArrayList<GeneID> lsCopedIDs = new ArrayList<GeneID>();
		if (getGffCodLeft() != null && getGffCodLeft().isInsideLoc()) {
			if (getGffCodLeft().isInsideUp()) {
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailUp().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc(getGffCodLeft().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
						GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
								getGffCodLeft().getGffDetailUp().getTaxID(),
								false);
						if (hashCopedID.contains(copedID)) {
							continue;
						}
						hashCopedID.add(copedID);
						lsCopedIDs.add(copedID);
					}
				}
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft()
					.getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc(getGffCodLeft().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
					GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
							getGffCodLeft().getGffDetailThis().getTaxID(),
							false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}

		if (getLsGffDetailMid() != null) {
			for (GffDetailGene gffDetailGene : getLsGffDetailMid()) {
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene
						.getLsCodSplit()) {
					// ���Ƿ����������ڸû����ڲ�
					GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
							gffDetailGene.getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}

		if (getGffCodRight() != null && getGffCodRight().isInsideLoc()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight()
					.getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc(getGffCodRight().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
					GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
							getGffCodRight().getGffDetailThis().getTaxID(),
							false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
			if (getGffCodRight().isInsideDown()) {
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight()
						.getGffDetailDown().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc(getGffCodRight().getCoord()) != GffGeneIsoInfo.COD_LOC_OUT) {
						GeneID copedID = new GeneID(gffGeneIsoInfo.getName(),
								getGffCodRight().getGffDetailDown().getTaxID(),
								false);
						if (hashCopedID.contains(copedID)) {
							continue;
						}
						hashCopedID.add(copedID);
						lsCopedIDs.add(copedID);
					}
				}
			}
		}
		return lsCopedIDs;
	}
}
