package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.database.model.modgeneid.GeneID;

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
	/** ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ�� */
	boolean utr5 = false;
	boolean utr3 = false;
	boolean exon = false;
	boolean intron = false;
	/** ��������ע����Ϣ */
	ArrayList<String[]> lsAnno;
	
	/** �������ѡ�񵽵�gene key: geneName + sep +chrID */
	HashSet<GffDetailGene> hashGffDetailGene;
	
	/** ������걣��Ļ�����Ϣ��Up��Down֮��û�н���
	 * ���汣���GffDetailGene����clone��
	 */
	LinkedHashSet<GffDetailGene> setGffDetailGenesLeft = null;
	/** �Ҳ����걣��Ļ�����Ϣ��Up��Down֮��û�н���
	 * ���汣���GffDetailGene����clone��
	 */
	LinkedHashSet<GffDetailGene> setGffDetailGenesRight = null;
	public void cleanFilter() {
		tss = null;
		tes = null;
		geneBody = false;
		/** ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ�� */
		utr5 = false;
		utr3 = false;
		exon = false;
		intron = false;
	}
	/**
	 * �趨tss�ķ�Χ��Ĭ��Ϊnull
	 * @param tss
	 */
	public void setTss(int[] tss) {
		if (this.tss != null && tss != null) {
			if (this.tss[0] == tss[0] && this.tss[1] == tss[1]) {
				return;
			}
		}
		this.tss = tss;
		resetFlag();
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
		resetFlag();
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
		resetFlag();
	}

	/**
	 * �趨�Ƿ�ץȡ����5UTR��gene��Ĭ��Ϊfalse����Ϊgenebody�Ѿ���true��ֻ��genebodyΪfalseʱ�Ż�������
	 * @param utr5
	 */
	public void setUTR5(boolean utr5) {
		if (this.utr5 == utr5) {
			return;
		}
		this.utr5 = utr5;
		resetFlag();
	}

	/**
	 * �趨�Ƿ�ץȡ����3UTR��gene��Ĭ��Ϊfalse����Ϊgenebody�Ѿ���true��ֻ��genebodyΪfalseʱ�Ż�������
	 * @param utr3
	 */
	public void setUTR3(boolean utr3) {
		if (this.utr3 == utr3) {
			return;
		}
		this.utr3 = utr3;
		resetFlag();
	}
	/**
	 * �趨����exon��gene��Ĭ��Ϊfalse����Ϊgenebody�Ѿ���true��ֻ��genebodyΪfalseʱ�Ż�������
	 * @param exon
	 */
	public void setExon(boolean exon) {
		if (this.exon == exon) {
			return;
		}
		this.exon = exon;
		resetFlag();
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
		resetFlag();
	}
	private void resetFlag() {
		flagSearchAnno = false; flagSearchHash = false; flagSearch = false;
	}
	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail, GffCodGene gffCod1, GffCodGene gffCod2) {
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
		if (flagSearchAnno) {
			return lsAnno;
		}
		flagSearchAnno = true;
		lsAnno = new ArrayList<String[]>();

		setStructureGene_And_Remove_IsoNotBeFiltered();
		for (GffDetailGene gffDetailGene : setGffDetailGenesLeft) {
			String[] anno = getAnnoCod(gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				String[] anno = getAnnoMid(gffDetailGene);
				lsAnno.add(anno);
			}
		}
		for (GffDetailGene gffDetailGene : setGffDetailGenesRight) {
			String[] anno = getAnnoCod(gffCod1.getCoord(), gffDetailGene, "peak_Left_point:");
			String[] anno2 = getAnnoCod(gffCod2.getCoord(), gffDetailGene, "peak_Right_point:");
			anno[3] = anno[3] + "  " + anno2[3];
			lsAnno.add(anno);
		}
		return lsAnno;
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
		anno[3] = peakPointInfo + gffDetailGene.getLongestSplit().toStringCodLocStr(coord);
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
	/** ������λ��串�ǵ��Ļ�����ȡ������������hashGffDetailGene */
	private void setHashCoveredGenInfo() {
		if (flagSearchHash && hashGffDetailGene != null) {
			return;
		}
		flagSearchHash = true;
		hashGffDetailGene = new LinkedHashSet<GffDetailGene>();
		//TODO: �����޸�tss��tes��gffDetailgeneҪ�޸�tss��tes��gffisoҲҪ�޸�tss��tes
		setStructureGene_And_Remove_IsoNotBeFiltered();
		for (GffDetailGene gffDetailGene : setGffDetailGenesLeft) {
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
		for (GffDetailGene gffDetailGene : setGffDetailGenesRight) {
			if (hashGffDetailGene.contains(gffDetailGene))
				continue;
			hashGffDetailGene.add(gffDetailGene);
		}
	}
	/**
	 * �����ǵ�ָ������Ļ���ȫ����ȡ������������setGffDetailGenesLeft��setGffDetailGenesRight
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
	private void setStructureGene_And_Remove_IsoNotBeFiltered() {
		if (flagSearch) {
			return;
		}
		flagSearch = true;
		int tssUp = 0;
		int tesDown = 0;
		if (tss != null) {
			tssUp = tss[0];
		}
		if (tes != null) {
			tesDown = tes[1];
		}
		set_SetGffDetailGenes_Clone(tssUp, tesDown);
		ArrayList<GffDetailGene> lsRemove = new ArrayList<GffDetailGene>();
		for (GffDetailGene gffDetailGene : setGffDetailGenesLeft) {
			//�����жϸ�gffdetailgene�Ƿ�������������������˵��������Ѿ�û��gffiso�ˣ�ҲҪɾ��
			if (!isInRegion2Cod_And_Remove_IsoNotBeFiltered(gffDetailGene) || gffDetailGene.getLsCodSplit().size() == 0) {
				lsRemove.add(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			setGffDetailGenesLeft.remove(gffDetailGene);
		}
		lsRemove.clear();
		for (GffDetailGene gffDetailGene : setGffDetailGenesRight) {			
			if (!isInRegion2Cod_And_Remove_IsoNotBeFiltered(gffDetailGene) || gffDetailGene.getLsCodSplit().size() == 0) {
				lsRemove.add(gffDetailGene);
			}
		}
		for (GffDetailGene gffDetailGene : lsRemove) {
			setGffDetailGenesRight.remove(gffDetailGene);
		}
	}

	/**
	 * ��clone�ķ�����ö˵���ǣ�浽�Ļ���
	 * @param tssUp
	 * @param tesDown ������չ����bp
	 */
	private void set_SetGffDetailGenes_Clone(int tssUp, int tesDown) {
		setGffDetailGenesLeft = new LinkedHashSet<GffDetailGene>();
		setGffDetailGenesRight = new LinkedHashSet<GffDetailGene>();
		// //////////////// up /////////////////////////////////
		if (this.gffCod1.isInsideUpExtend(tssUp, tesDown)) {
			setGffDetailGenesLeft.add(gffCod1.getGffDetailUp().clone());
		}
		// //////////////////// this /////////////////////////////////
		if (this.gffCod1.isInsideLoc()) {
			setGffDetailGenesLeft.add(gffCod1.getGffDetailThis().clone());
		}
		if (this.gffCod1.isInsideDownExtend(tssUp, tesDown)) {
			setGffDetailGenesLeft.add(gffCod1.getGffDetailDown().clone());
		}
		// //////////////////////////// cod2
		//˵����һ�����뱾�㲻��ͬһ��������
		if (this.gffCod2.isInsideUpExtend(tssUp, tesDown) && !setGffDetailGenesLeft.contains(gffCod2.getGffDetailUp())) {
			setGffDetailGenesRight.add(gffCod2.getGffDetailUp().clone());
		}
		if (this.gffCod2.isInsideLoc() && !setGffDetailGenesLeft.contains(gffCod2.getGffDetailThis())) {
			setGffDetailGenesRight.add(gffCod2.getGffDetailThis().clone());
		}
		if (this.gffCod2.isInsideDownExtend(tssUp, tesDown) && !setGffDetailGenesLeft.contains(gffCod2.getGffDetailDown())) {
			setGffDetailGenesRight.add(gffCod2.getGffDetailDown().clone());
		}
	}
	/**
	 * <b>�ڲ���ɾ��iso��Ϣ�����������gffDetail������clone��</b> ʹ��ǰ���ж�cod�Ƿ���������ͬ��gffDetailGene��
	 * ��������������ͬһ�������ڲ������ʱ Ч���Ե͵��Ǻ�ȫ�棬ÿ��isoform�����ж�
	 * 
	 * @param gffDetailGene
	 *            
	 * @return
	 */
	private boolean isInRegion2Cod_And_Remove_IsoNotBeFiltered(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = null;
		flag = getInRegion2Cod(getGffCod1().getCoord(), getGffCod2().getCoord(), gffDetailGene);

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
	private int[] getInRegion2Cod(int coord1, int coord2, GffDetailGene gffDetailGene) {
		// һ������㣬һ�����յ�
		int coordStart = 0;
		int coordEnd = 0;
		/** ��ǣ�0��ʾ��Ҫȥ����1��ʾ���� */
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
			if (tss != null) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= tss[1]
						&& gffGeneIsoInfo.getCod2Tss(coordEnd) >= tss[0]) {
					flag[i] = 1;
				}
			}
			if (tes != null) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getCod2Tes(coordStart) <= tes[1]
						&& gffGeneIsoInfo.getCod2Tes(coordEnd) >= tes[0]) {
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
			if (utr5) {
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
			if (utr3) {
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
			if (flag[i] == 0 && (exon || intron)) {
				if (gffGeneIsoInfo.getCod2Tss(coordStart) <= 0 && gffGeneIsoInfo.getCod2Tes(coordEnd) > 0) {
					flag[i] = 1;
				}
			}
			if (exon) {
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
			if (intron) {
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

}
