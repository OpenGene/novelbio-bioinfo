package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.novelbio.analysis.annotation.copeID.CopedID;

public class GffCodGeneDU extends GffCodAbsDu<GffDetailGene, GffCodGene>{

	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail,
			GffCodGene gffCod1, GffCodGene gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}
	/**
	 * ���gffDetailGene�ľ�����Ϣ�������gffDetailGene�������copedID�����á�///���ָ�
	 * @param Tss tss���� tssǰΪ��������Ϊ����
	 * @param Tes tes���� tesǰΪ��������Ϊ����
	 * @param geneBody true or false
	 * @param UTR5 true or false ���ж��Ƿ���mRNA
	 * @param UTR3 true or false ���ж��Ƿ���mRNA
	 * @param Exon true or false
	 * @param Intron true or false
	 * @return
	 * 0��accID <br>
	 * 1��symbol<br>
	 * 2��description<br>
	 * 3�������Ǿ�����Ϣ���м���covered
	 */
	public ArrayList<String[]> getAnno(int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		HashSet<GffDetailGene> hashGffDetailGeneAnno = new HashSet<GffDetailGene>();
		ArrayList<String[]> lsAnno = new ArrayList<String[]>();
		
		Set<GffDetailGene> gffUpGene = getStructureUpGene(Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		for (GffDetailGene gffDetailGene : gffUpGene) {
			if (hashGffDetailGeneAnno.contains(gffDetailGene))
				continue;
			hashGffDetailGeneAnno.add(gffDetailGene);
			
			String[] anno = getAnnoCod(gffDetailGene);
			lsAnno.add(anno);
		}
		
		if (lsgffDetailsMid != null) {
			for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
				if (hashGffDetailGeneAnno.contains(gffDetailGene))
					continue;
				hashGffDetailGeneAnno.add(gffDetailGene);
				
				String[] anno = getAnnoMid(gffDetailGene);
				lsAnno.add(anno);
			}
		}
		
		Set<GffDetailGene> gffDownGene = getStructureDownGene(Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		for (GffDetailGene gffDetailGene : gffDownGene) {
			if (hashGffDetailGeneAnno.contains(gffDetailGene))
				continue;
			hashGffDetailGeneAnno.add(gffDetailGene);
			
			String[] anno = getAnnoCod(gffDetailGene);
			lsAnno.add(anno);
		}
		return lsAnno;
	}
	/**
	 * ���gffDetailGene�ľ�����Ϣ�������gffDetailGene�������copedID�����á�///���ָ�
	 * @param gffDetailGene
	 * @return
	 * 0��accID<br>
	 * 1��symbol<br>
	 * 2��description<br>
	 * 3��������ʽ�Ķ�λ����
	 */
	private String[] getAnnoCod(GffDetailGene gffDetailGene)
	{
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";
		
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbo();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", ""); anno[1] = anno[1].replaceFirst("///", ""); anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = gffDetailGene.getLongestSplit().getCodLocStr();
		return anno;
	}
	/**
	 * ���gffDetailGene�ľ�����Ϣ�������gffDetailGene�������copedID�����á�///���ָ�
	 * @param gffDetailGene
	 * @return
	 * 0��accID<br>
	 * 1��symbol<br>
	 * 2��description<br>
	 * 3��Covered
	 */
	private String[] getAnnoMid(GffDetailGene gffDetailGene)
	{
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++)
			anno[i] = "";
		
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), gffDetailGene.getTaxID(), false);
			if (hashCopedID.contains(copedID)) {
				continue;
			}
			hashCopedID.add(copedID);
			anno[0] = anno[0] + "///" + copedID.getAccID();
			anno[1] = anno[1] + "///" + copedID.getSymbo();
			anno[2] = anno[2] + "///" + copedID.getDescription();
		}
		anno[0] = anno[0].replaceFirst("///", ""); anno[1] = anno[1].replaceFirst("///", ""); anno[2] = anno[2].replaceFirst("///", "");
		anno[3] = "Covered";
		return anno;
	}
	
	/**
	 * �����ǵ�ָ������Ļ���ȫ����ȡ����������
	 * @param Tss Tss�����ζ���bp������Ϊ��������Ϊ������ ������Ϊ������ʾֻѡȡTss���Σ�������Ϊ������ʾֻѡȡTss����
	 * @param Tes ͬTss
	 * @param geneBody �Ƿ���genebody
	 * @param Exon ��genebodyΪfalseʱ���Ƿ񸲸�exon
	 * @param Intron ��genebodyΪfalseʱ���Ƿ񸲸�exon
	 * @return
	 * û���򷵻�һ��sizeΪ0��set
	 */
	private Set<GffDetailGene> getStructureUpGene(int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		LinkedHashSet<GffDetailGene> hashGene = new LinkedHashSet<GffDetailGene>();
		ArrayList<GffDetailGene[]> lsGffDetailGenes = getSameGeneDetail();
		
		for (GffDetailGene[] gffDetailGenes : lsGffDetailGenes) {
			if (gffDetailGenes[0] == null && gffDetailGenes[1] == null) {
				continue;
			}
			else if (gffDetailGenes[0] != null && gffDetailGenes[1] == null) {
				if (isInRegion1CodUp(gffDetailGenes[0], Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
					hashGene.add(gffDetailGenes[0]);
				}
			}
			else if (gffDetailGenes[0] != null && gffDetailGenes[1] != null) {
				if (isInRegion2Cod(gffDetailGenes[0], gffDetailGenes[1], Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
					//ֻ���ӵ�һ������Ϣ
					hashGene.add(gffDetailGenes[0]);
				}
			}
		}
		return hashGene;
	}
	
	
	/**
	 * 
	 * �����ǵ�ָ������Ļ���ȫ����ȡ����������
	 * @param Tss Tss�����ζ���bp������Ϊ��������Ϊ������ ������Ϊ������ʾֻѡȡTss���Σ�������Ϊ������ʾֻѡȡTss����
	 * @param Tes ͬTss
	 * @param geneBody �Ƿ���genebody
	 * @param Exon ��genebodyΪfalseʱ���Ƿ񸲸�exon
	 * @param Intron ��genebodyΪfalseʱ���Ƿ񸲸�exon
	 * @return
	 * û���򷵻�һ��sizeΪ0��set
	 */
	private Set<GffDetailGene> getStructureDownGene(int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron) {
		LinkedHashSet<GffDetailGene> hashGene = new LinkedHashSet<GffDetailGene>();
		ArrayList<GffDetailGene[]> lsGffDetailGenes = getSameGeneDetail();
		
		for (GffDetailGene[] gffDetailGenes : lsGffDetailGenes) {
			if (gffDetailGenes[0] == null && gffDetailGenes[1] == null) {
				continue;
			}
			else if (gffDetailGenes[0] == null && gffDetailGenes[1] != null) {
				if (isInRegion1CodDown(gffDetailGenes[1], Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron)) {
					hashGene.add(gffDetailGenes[1]);
				}
			}
		}
		return hashGene;
	}
	
	ArrayList<GffDetailGene[]> lsGffDetailGenes = null;
	/**
	 * ������������Ƿ���ͬһ��geneDetail��
	 * @return
	 * lsGffDetailGenes - gffDetailGene[2] <br>
	 * 0: gffDetailGene 1: null λ����ǰ���gff�� <br>
	 * 0: null 1: gffDetailGene λ���ں����gff�� <br>
	 *  0: gffDetailGene 1: gffDetailGene λ��ͬʱ������gff��
	 */
	private ArrayList<GffDetailGene[]> getSameGeneDetail()
	{
		if (lsGffDetailGenes != null) {
			return lsGffDetailGenes;
		}
		lsGffDetailGenes = new ArrayList<GffDetailGene[]>();
		
		
		int[] flag = new int[6];
		//////////////////up
		if (this.gffCod1.isInsideUp()) {
			GffDetailGene[] gffDetailGenesUp = new GffDetailGene[2];
			gffDetailGenesUp[0] = gffCod1.getGffDetailUp();
			flag[0] = 1;
			if (this.gffCod2.isInsideUp() && this.gffCod1.getGffDetailUp().equals(this.gffCod2.getGffDetailUp())) {
				flag[0] = 2; flag[3] = -1;
				gffDetailGenesUp[1] = gffCod2.getGffDetailUp();
			}
			lsGffDetailGenes.add(gffDetailGenesUp);
		}
		//////////////////////this
		if (this.gffCod1.isInsideLoc()) {
			flag[1] = 1;
			GffDetailGene[] gffDetailGenes = new GffDetailGene[2];
			gffDetailGenes[0] = gffCod1.getGffDetailThis();
			//cod2 up
			if (this.gffCod2.isInsideUp() && this.gffCod1.getGffDetailThis().equals(this.gffCod2.getGffDetailUp())) {
				flag[1] = 2; flag[3] = -1;
				gffDetailGenes[1] = gffCod2.getGffDetailUp();
			}
			//cod2 this
			else if ( this.gffCod2.isInsideLoc() && this.gffCod1.getGffDetailThis().equals(this.gffCod2.getGffDetailThis())) {
				flag[1] = 2; flag[4] = -1;
				gffDetailGenes[1] = gffCod2.getGffDetailThis();
			}
			lsGffDetailGenes.add(gffDetailGenes);
		}
	
		if (this.gffCod1.isInsideDown()) {
			GffDetailGene[] gffDetailGenesDown = new GffDetailGene[2];
			gffDetailGenesDown[0] = gffCod1.getGffDetailUp();
			flag[2] = 1;
			if (this.gffCod2.isInsideUp() && this.gffCod1.getGffDetailDown().equals(this.gffCod2.getGffDetailUp())) {
				flag[2] = 2; flag[3] = -1;
				gffDetailGenesDown[1] = gffCod2.getGffDetailUp();
			}
			if ( this.gffCod1.getGffDetailDown().equals(this.gffCod2.getGffDetailThis())) {
				flag[2] = 2; flag[4] = -1;
				gffDetailGenesDown[1] = gffCod2.getGffDetailThis();
			}
			if (this.gffCod2.isInsideDown() && this.gffCod1.getGffDetailDown().equals(this.gffCod2.getGffDetailDown())) {
				flag[2] = 2; flag[5] = -1;
				gffDetailGenesDown[1] = gffCod2.getGffDetailDown();
			}
			lsGffDetailGenes.add(gffDetailGenesDown);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (this.gffCod2.isInsideUp() && flag[3] != -1) {
			flag[3] = 1;
			GffDetailGene[] gffDetailGenesUp2 = new GffDetailGene[2];
			gffDetailGenesUp2[1] = gffCod2.getGffDetailUp();
			lsGffDetailGenes.add(gffDetailGenesUp2);
		}
		
		if (flag[4] != -1) {
			flag[4] = 1;
			GffDetailGene[] gffDetailGenes2 = new GffDetailGene[2];
			gffDetailGenes2[1] = gffCod2.getGffDetailThis();
			lsGffDetailGenes.add(gffDetailGenes2);
		}
		if (this.gffCod2.isInsideDown() && flag[5] != -1) {
			flag[5] = 1;
			GffDetailGene[] gffDetailGenes2 = new GffDetailGene[2];
			gffDetailGenes2[1] = gffCod2.getGffDetailDown();
			lsGffDetailGenes.add(gffDetailGenes2);
		}
		return lsGffDetailGenes;
	}
	
	
	
	
	/**
	 * ���������˵��У��漰��Tss�Ļ���ȫ����ȡ����
	 * @return
	 */
	public Set<GffDetailGene> getTSSGene() {
		/**
		 * �����صĻ���
		 */
		Set<GffDetailGene> setGffDetailGenes = new LinkedHashSet<GffDetailGene>();
		//��ǰ�������ת¼����Χ��
		if (gffCod1 != null)
		{
			//��һ�������й�ϵ
			if (isUpTss(gffCod1.getGffDetailUp())) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//�������ϵ
			if (isUpTss(gffCod1.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		
		for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
			setGffDetailGenes.add(gffDetailGene);
		}
		
		if (gffCod2 != null)
		{
			//��һ�������й�ϵ
			if (isDownTss(gffCod2.getGffDetailThis()))
			{
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//�������ϵ
			if (isDownTss(gffCod2.getGffDetailDown())) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	
	private boolean isUpTss(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss()
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend())
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTss(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss()
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend())
		)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * ��Ҫ���
	 * �����������㲻��ͬһ�������ڲ������ʱ����һ��������
	 * Ч���Ե͵��Ǻ�ȫ�棬ÿ��isoform�����ж�
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ��
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion1CodUp(GffDetailGene gffDetailGene, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = null;
		//����
		if ( gffDetailGene.isCis5to3() ) {
			flag = isInRegion1CodCisRegion_Cis5to3(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		}
		//����
		else {
			flag = isInRegion1CodTransRegion_Cis5to3(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		}
		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0 ; i--) {
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
	 * ��Ҫ���
	 * �����������㲻��ͬһ�������ڲ������ʱ����һ��������
	 * Ч���Ե͵��Ǻ�ȫ�棬ÿ��isoform�����ж�
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ��
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion1CodDown(GffDetailGene gffDetailGene, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		if (gffDetailGene == null) {
			return false;
		}
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = null;
		//����
		if ( gffDetailGene.isCis5to3() ) {
			flag = isInRegion1CodTransRegion_Cis5to3(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		}
		//����
		else {
			flag = isInRegion1CodCisRegion_Cis5to3(gffDetailGene, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
		}
		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0 ; i--) {
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
	 * ʹ��ǰ���ж�cod�Ƿ���������ͬ��gffDetailGene��
	 * �����������㲻��ͬһ�������ڲ������ʱ����һ��������
	 * Ч���Ե͵��Ǻ�ȫ�棬ÿ��isoform�����ж�
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ��
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 */
	private boolean isInRegion2Cod(GffDetailGene gffDetailGene1, GffDetailGene gffDetailGene2, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		if (gffDetailGene1 == null || gffDetailGene2 == null) {
			return false;
		}
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = null;
		flag = getInRegion2Cod(gffDetailGene1, gffDetailGene2, Tss, Tes, geneBody, UTR5, UTR3, Exon, Intron);
	
		boolean flagResult = false;
		for (int i = flag.length - 1; i >= 0 ; i--) {
			if (flag[i] == 0) {
				gffDetailGene1.removeIso(i);
				gffDetailGene2.removeIso(i);
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
	 * �����������㲻��ͬһ�������ڲ������ʱ����һ��������
	 * ��cod���Ƿ����ת¼������һ�µ�ʱ�����
	 * Ч���Ե͵��Ǻ�ȫ�棬ÿ��isoform�����ж�
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ��
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 * int[iso.size] �������ÿ��ת¼���Ƿ�ϸ�0���ϸ�1�ϸ�
	 * 
	 */
	private int[] isInRegion1CodCisRegion_Cis5to3(GffDetailGene gffDetailGene, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
 {
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = new int[gffDetailGene.getLsCodSplit().size()];
		// ����
		for (int i = 0; i < gffDetailGene.getLsCodSplit().size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLsCodSplit().get(i);
			if (Tss != null) {
				if (gffGeneIsoInfo.getCod2Tss() <= Tss[1]) {
					flag[i] = 1;
				}
			}
			if (Tes != null) {
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes() <= Tes[1]) {
					flag[i] = 1;
				}
			}
			if (geneBody) {
				// �ڻ������ο϶����ڻ�������
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes() <= 0) {
					flag[i] = 1;
				}
			}
			if (UTR5) {
				if (flag[i] == 0 && geneBody == false
						&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& gffGeneIsoInfo.getCod2ATG() <= 0) {
					flag[i] = 1;
				}
			}
			if (UTR3) {
				if (flag[i] == 0 && geneBody == false
						&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& gffGeneIsoInfo.getCod2Tes() <= 0) {
					flag[i] = 1;
				}
			}
			if (Exon) {
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes() <= 0) {
					flag[i] = 1;
				}
			}
			if (Intron) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getCod2Tes() <= 0
						&& gffGeneIsoInfo.getCodExInNum() < gffGeneIsoInfo.getIsoInfo().size()) {
					flag[i] = 1;
				}
			}
		}
		return flag;
	}
	
	
	
	/**
	 * ��Ҫ���
	 * �����������㲻��ͬһ�������ڲ������ʱ����һ��������
	 * Ч���Ե͵��Ǻ�ȫ�棬ÿ��isoform�����ж�
	 * @param gffDetailGene
	 * @param Tss
	 * @param Tes
	 * @param geneBody
	 * @param UTR5 ���λ����Tssǰ����ô��ʹ������û��5UTR��Ҳ�ᱻѡ��
	 * @param UTR3
	 * @param Exon
	 * @param Intron
	 * @return
	 * int[iso.size] �������ÿ��ת¼���Ƿ�ϸ�0���ϸ�1�ϸ�
	 */
	private int[] isInRegion1CodTransRegion_Cis5to3(GffDetailGene gffDetailGene, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = new int[gffDetailGene.getLsCodSplit().size()];
		for (int i = 0; i < gffDetailGene.getLsCodSplit().size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLsCodSplit().get(i);
			if (Tss != null) {
				if (gffGeneIsoInfo.getCod2Tss() >= Tss[0]) {
					flag[i] = 1;
				}
			}
			if (Tes != null) {
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tes() >= Tes[0]) {
					flag[i] = 1;
				}
			}
			if (geneBody) {
				// �ڻ������ο϶����ڻ�������
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tss() >= 0) {
					flag[i] = 1;
				}
			}
			if (UTR5) {
				if (flag[i] == 0 && geneBody == false
						&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& gffGeneIsoInfo.getCod2Tss() >= 0) {
					flag[i] = 1;
				}
			}
			if (UTR3) {
				if (flag[i] == 0
						&& geneBody == false
						&& (gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& (gffGeneIsoInfo.getCod2UAG() >= 0)) {
					flag[i] = 1;
				}
			}
			if (Exon) {
				if (flag[i] == 0 && gffGeneIsoInfo.getCod2Tss() >= 0) {
					flag[i] = 1;
				}
			}
			if (Intron) {
				if (flag[i] == 0
						&& gffGeneIsoInfo.getCod2Tss() >= 0
						&& (gffGeneIsoInfo.getCodExInNum() < 0
								|| gffGeneIsoInfo.getCodExInNum() > 1
								|| (gffGeneIsoInfo.getCodExInNum() == 1 && gffGeneIsoInfo.codLoc == GffGeneIsoInfo.COD_LOC_INTRON))) 
				{
					flag[i] = 1;
				}
			}
		}
		return flag;
	}
	
	/**
	 * ����coord����ͬһ��gffDetailGene��ʱ�����ж�
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
	private int[] getInRegion2Cod(GffDetailGene gffDetailGene1, GffDetailGene gffDetailGene2, int[] Tss, int[] Tes,boolean geneBody, Boolean UTR5, boolean UTR3, boolean Exon, boolean Intron)
	{
		/**
		 * ��ǣ�0��ʾ��Ҫȥ����1��ʾ����
		 */
		int[] flag = new int[gffDetailGene1.getLsCodSplit().size()];
		for (int i = 0; i < gffDetailGene1.getLsCodSplit().size(); i++) {
			GffGeneIsoInfo gffGeneIsoInfo1 = gffDetailGene1.getLsCodSplit().get(i);
			GffGeneIsoInfo gffGeneIsoInfo2 = gffDetailGene2.getLsCodSplit().get(i);
			if (Tss != null) {
				if (gffGeneIsoInfo1.getCod2Tss() <= Tss[1] && gffGeneIsoInfo2.getCod2Tss() >= Tss[0]) {
					flag[i] = 1;
				}
			}
			if (Tes != null) {
				if (flag[i] == 0 && gffGeneIsoInfo1.getCod2Tes() <= Tes[1] && gffGeneIsoInfo2.getCod2Tes() >= Tes[0]) {
					flag[i] = 1;
				}
			}
			if (geneBody) {
				// �ڻ������ο϶����ڻ�������
				if (flag[i] == 0 && gffGeneIsoInfo1.getCod2Tes() <= 0 && gffGeneIsoInfo2.getCod2Tss() >= 0) {
					flag[i] = 1;
				}
			}
			if (UTR5) {
				if (flag[i] == 0 && geneBody == false
						&& (gffGeneIsoInfo1.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo1.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo1.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& 
						gffGeneIsoInfo1.getCod2ATG() <=0 && gffGeneIsoInfo2.getCod2Tss() >= 0
						&&
						(gffGeneIsoInfo1.getCodExInNum() != gffGeneIsoInfo2.getCodExInNum()  
								||
						gffGeneIsoInfo1.getCodExInNum() == gffGeneIsoInfo2.getCodExInNum() && (gffGeneIsoInfo1.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON)		
						)
						) {
					flag[i] = 1;
				}
			}
			if (UTR3) {
				if (flag[i] == 0 && geneBody == false
						&& (gffGeneIsoInfo1.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA) || gffGeneIsoInfo1.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_MRNA_TE) || gffGeneIsoInfo1.getGeneType().equals(GffGeneIsoInfo.TYPE_GENE_PSEU_TRANSCRIPT))
						&& 
						gffGeneIsoInfo1.getCod2Tes() <= 0 && gffGeneIsoInfo2.getCod2UAG() >= 0
						&&
						(gffGeneIsoInfo1.getCodExInNum() != gffGeneIsoInfo2.getCodExInNum()  
								||
						gffGeneIsoInfo1.getCodExInNum() == gffGeneIsoInfo2.getCodExInNum() && (gffGeneIsoInfo1.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON)		
						)
								) {
					flag[i] = 1;
				}
			}
			if (Exon) {
				if (flag[i] == 0 &&
						(gffGeneIsoInfo1.getCodExInNum() != gffGeneIsoInfo2.getCodExInNum()  
								||
						gffGeneIsoInfo1.getCodExInNum() == gffGeneIsoInfo2.getCodExInNum() && (gffGeneIsoInfo1.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON || gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON)		
						)
					) {
					flag[i] = 1;
				}
			}
			if (Intron) {
				if (flag[i] == 0 &&
						(gffGeneIsoInfo1.getCodExInNum() != gffGeneIsoInfo2.getCodExInNum() && gffGeneIsoInfo1.getCodExInNum() != 0 && gffGeneIsoInfo2.getCodExInNum() != 0)
								||
								
						(gffGeneIsoInfo1.getCodExInNum() == gffGeneIsoInfo2.getCodExInNum() && (gffGeneIsoInfo1.getCodLoc() == GffGeneIsoInfo.COD_LOC_INTRON || gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_INTRON) )
						||
						(gffGeneIsoInfo1.getCodExInNum() == 0 && (gffGeneIsoInfo2.getCodExInNum() >= 2 || gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_INTRON))
						||
						(gffGeneIsoInfo2.getCodExInNum() == 0 && (gffGeneIsoInfo1.getCodExInNum() <= gffGeneIsoInfo1.getExonNum() - 1 || gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_INTRON))
				) 
				{
					flag[i] = 1;
				}
			}
		}
		return flag;
	}
	
	
	/**
	 * ���������˵��У��漰��Tes�Ļ���ȫ����ȡ����
	 * @return
	 */
	public Set<GffDetailGene> getTESGene() {
		/**
		 * �����صĻ���
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		//��ǰ�������ת¼����Χ��
		if (gffCod1 != null)
		{
			//��һ�������й�ϵ
			if (isUpTes(gffCod1.getGffDetailUp())) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//�������ϵ
			if (isUpTes(gffCod1.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		
		for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
			setGffDetailGenes.add(gffDetailGene);
		}
		
		if (gffCod2 != null)
		{
			//��һ�������й�ϵ
			if (isDownTes(gffCod2.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//�������ϵ
			if (isDownTes(gffCod2.getGffDetailDown())) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	
	private boolean isUpTes(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend()
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd() )
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTes(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend()
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd())
		)
		{
			return true;
		}
		return false;
	}
	/**
	 * �������и��ǵ��Ļ����copedID
	 * @return
	 */
	public ArrayList<CopedID> getAllCoveredGenes() {
		//����ȥ�����
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		ArrayList<CopedID> lsCopedIDs = new ArrayList<CopedID>();
		if (getGffCodLeft() != null && getGffCodLeft().isInsideLoc()) {
			if (getGffCodLeft().isInsideUp())
			{
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailUp().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
						CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodLeft().getGffDetailUp().getTaxID(), false);
						if (hashCopedID.contains(copedID)) {
							continue;
						}
						hashCopedID.add(copedID);
						lsCopedIDs.add(copedID);
					}
				}
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodLeft().getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
					CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodLeft().getGffDetailThis().getTaxID(), false);
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
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					// ���Ƿ����������ڸû����ڲ�
					CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), gffDetailGene.getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}
		
		
		if (getGffCodRight() != null && getGffCodRight().isInsideLoc()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight().getGffDetailThis().getLsCodSplit()) {
				if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
					CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodRight().getGffDetailThis().getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
			if (getGffCodRight().isInsideDown())
			{
				for (GffGeneIsoInfo gffGeneIsoInfo : getGffCodRight().getGffDetailDown().getLsCodSplit()) {
					if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
						CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), getGffCodRight().getGffDetailDown().getTaxID(), false);
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