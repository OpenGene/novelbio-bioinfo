package com.novelbio.analysis.seq.genomeNew2.gffOperate;

public abstract class GffGeneIsoInfoCod {
	GffGeneIsoInfo gffGeneIso = null;
	public GffGeneIsoInfoCod(GffGeneIsoInfo gffgeneIso, int coord)
	{
		this.gffGeneIso = gffgeneIso;
		this.coord = coord;
		searchCoord();
	}
	/**
	 * ���codInExon������������
	 */
	public static final int COD_LOC_EXON = 100;
	/**
	 * ���codInExon�����ں�����
	 */
	public static final int COD_LOC_INTRON = 200;
	/**
	 * ���codInExon����ת¼����
	 */
	public static final int COD_LOC_OUT = 300;
	/**
	 * ���codInExon����5UTR��
	 */
	public static final int COD_LOCUTR_5UTR = 5000;
	/**
	 * ���codInExon����3UTR��
	 */
	public static final int COD_LOCUTR_3UTR = 3000;
	/**
	 * ���codInExon����UTR��
	 */
	public static final int COD_LOCUTR_OUT = 0;	
	/**
	 * ����
	 */
	protected int coord = GffCodAbs.LOC_ORIGINAL;
	
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * �������������Ϊ����������Ϊ����
	 */
	protected int cod2TSS = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ���굽��ת¼���յ�ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
	 */
	protected int cod2TES = GffCodAbs.LOC_ORIGINAL;
	
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���
	 * ���굽��ת¼�����ľ��룬ֻ��mRNAˮƽ������������
	 * ֻ�е����괦���������в��о��룬�������ں���\
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	protected int cod2TSSmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���
	 * ���굽��ת¼���յ�ľ��룬ֻ��mRNAˮƽ������������
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	protected int cod2TESmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���<br>
	 * ���굽��ת¼��atg�ľ��룬ֻ��mRNAˮƽ������������<br>
	 * �������������Ϊ����������Ϊ����<br>
	 */
	protected int cod2ATGmRNA= GffCodAbs.LOC_ORIGINAL;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���<br>
	 * ���굽��ת¼��uag�ľ��룬ֻ��mRNAˮƽ������������<br>
	 * �������յ�����Ϊ����������Ϊ����<br>
	 * Cnn nn  nuaG ����Ϊ8
	 */
	protected int cod2UAGmRNA = GffCodAbs.LOC_ORIGINAL;
	
	
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں������ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInStart = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں����յ�ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInEnd = GffCodAbs.LOC_ORIGINAL;
	/**
	 * �����ں���
	 * ���굽ATG�ľ��룬����������.
	 * ��ATG����Ϊ����������Ϊ����
	 * @return
	 */
	protected int cod2ATG = GffCodAbs.LOC_ORIGINAL;
	/**
	 * �����ں���
	 * ���굽UAG�ľ��룬����������.
	 * ��UAG����Ϊ����������Ϊ����
	 * @return
	 */
	protected int cod2UAG = GffCodAbs.LOC_ORIGINAL;
	/**
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 * ʵ����Ŀ����1��ʼ����
	 */
	protected int numExIntron = -1;
	
	/**
	 * ������5UTR��3UTR���ǲ���
	 */
	protected int codLocUTR = COD_LOCUTR_OUT;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2start/cod2cdsEnd
	 */
	protected int cod2UTRstartmRNA = GffCodAbs.LOC_ORIGINAL;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 */
	protected int cod2UTRendmRNA = GffCodAbs.LOC_ORIGINAL;

	/**
	 * �����������ӡ��ں��ӻ����ڸ�ת¼����
	 * ��codLocExon��codLocIntron�Ƚϼ���
	 */
	protected int codLoc = 0;
	public GffGeneIsoInfo getGffGeneIso() {
		return gffGeneIso;
	}
	/**
	 * coord�Ƿ���promoter����ķ�Χ�ڣ���Tss����UpStreamTSSbp��Tss����DownStreamTssbp
	 * @return
	 */
	public boolean isCodInIsoTss()
	{
		if (codLoc == COD_LOC_OUT && getCod2Tss() < 0 && Math.abs(getCod2Tss()) <= GffGeneIsoInfo.UpStreamTSSbp ) {
			return true;
		}
		else if ( codLoc != COD_LOC_OUT && getCod2Tss() > 0 && Math.abs(getCod2Tss()) <= GffGeneIsoInfo.DownStreamTssbp ) {
			return true;
		}
		return false;
	}
	
	/**
	 * coord�Ƿ���gene�⣬������geneEnd�ӳ�����ķ�Χ��
	 * @return
	 */
	public boolean isCodInIsoGenEnd()
	{
		if (codLoc == COD_LOC_OUT && getCod2Tes() > 0 && Math.abs(getCod2Tes()) <= GffGeneIsoInfo.GeneEnd3UTR ) {
			return true;
		}
		return false;
	}
	/**
	 * coord�Ƿ��ڸ�ת¼������promoter��geneEnd�ӳ�����ķ�Χ��
	 * @return
	 */
	public boolean isCodInIsoExtend() {
		return (codLoc != COD_LOC_OUT) || isCodInIsoTss() || isCodInIsoGenEnd();
	}
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
	 * @return
	 */
	public int getCodLoc() {
		return codLoc;
	}
	/**
	 * ��ת¼�����ĸ�λ��
	 * ��COD_LOCUTR_5UTR��COD_LOCUTR_3UTR������
	 * @return
	 */
	public int getCodLocUTR() {
		return codLocUTR;
	}
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
	 * @return
	 */
	public int getCod2Tss() {
		return cod2TSS;
	}
	/**
	 * ���굽��ת¼���յ�ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
	 * @return
	 */
	public int getCod2Tes() {
		return cod2TES;
	}
	public int getCoord() {
		return coord;
	}
	public void setCoord(int coord) {
	
		this.coord = coord;
		searchCoord();
	}
	/**
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 * ʵ����Ŀ����1��ʼ����
	 * @return
	 */
	public int getCodExInNum() {
		return numExIntron;
	}
	/**
	 * ���굽��������/�ں������ľ��룬����������
	 * @return
	 */
	public int getCod2ExInStart() {
		return cod2ExInStart;
	}
	/**
	 * ���굽��������/�ں����յ�ľ��룬����������
	 * @return
	 */
	public int getCod2ExInEnd() {
		return cod2ExInEnd;
	}
	/**
	 * ���굽ATG�ľ��룬����������.
	 * ��ATG����Ϊ����������Ϊ����
	 * @return
	 */
	public int getCod2ATG() {
		return cod2ATG;
	}
	/**
	 * ���굽UAG�����һ������ľ��룬����������.
	 * ��UAG����Ϊ����������Ϊ����
	 * @return
	 */
	public int getCod2UAG() {
		return cod2UAG;
	}
	/**
	 * ʹ��ǰ���ж���UTR��<br>
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں��� <br>
	 */
	public int getCod2UTRstartmRNA() {
		return cod2UTRstartmRNA;
	}
	/**
	 * ʹ��ǰ���ж���UTR��<br>
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���<br>
	 */
	public int getCod2UTRendmRNA() {
		return cod2UTRendmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽��ת¼��atg�ľ���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 * ��������ں����У���Ϊ�ܴ�ĸ��������-10000000
	 */
	public int getCod2ATGmRNA() {
		return cod2ATGmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽UAG�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * �������յ�����Ϊ����������Ϊ����<br>
	 * ��������ں����У���Ϊ�ܴ�ĸ��������-10000000
	 */
	public int getCod2UAGmRNA() {
		return cod2UAGmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽TSS�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ֻ�е����괦���������в��о��룬�������ں���\
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	public int getCod2TSSmRNA() {
		return cod2TSSmRNA;
	}
	/**
	 * ʹ��ǰ���ж���Exon�У����굽TES�ľ��룬mRNAˮƽ
	 * ��ȥ���ں��ӵ�ֱ����getCod2UAG
	 * ��Ϊcod���������У����Կ϶���tss���Σ����Ը�ֵʼ��Ϊ����
	 */
	public int getCod2TESmRNA() {
		return cod2TESmRNA;
	}

	
	/**
	 */
	private void searchCoord()
	{
		init();
		codSearchNum();
		if (codLocUTR == COD_LOCUTR_5UTR) {
			setCod2UTR5();
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			setCod2UTR3();
		}
		if (codLoc == COD_LOC_EXON) {
			setCod2StartEndmRNA();
			setCod2StartEndCDS();
		}
	}
	/**
	 * ��ʼ������
	 */
	private void init()
	{
		 coord = GffCodAbs.LOC_ORIGINAL;
		 cod2TSS = GffCodAbs.LOC_ORIGINAL;
		 cod2TES = GffCodAbs.LOC_ORIGINAL;
		 cod2TSSmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2TESmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2ATGmRNA= GffCodAbs.LOC_ORIGINAL;
		 cod2UAGmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2ExInStart = GffCodAbs.LOC_ORIGINAL;
		 cod2ExInEnd = GffCodAbs.LOC_ORIGINAL;
		 cod2ATG = GffCodAbs.LOC_ORIGINAL;
		 cod2UAG = GffCodAbs.LOC_ORIGINAL;
		 numExIntron = -1;
		 codLocUTR = COD_LOCUTR_OUT;
		 cod2UTRstartmRNA = GffCodAbs.LOC_ORIGINAL;
		 cod2UTRendmRNA = GffCodAbs.LOC_ORIGINAL;
		 codLoc = 0;
	}
	/**
	 * ���������ڵڼ��������ӻ��ں�����
	 * ����ָ�������������ӻ����ں���
	 * �Ƿ���UTR��
	 * ͬʱ���		
	 * cod2ATG
		cod2cdsEnd 
		cod2start 
		cod2end 
		��
	 */
	protected void codSearchNum()
	{
		int ExIntronnum = gffGeneIso.getLocExInNum(coord);
		if (ExIntronnum == 0) {
			codLoc = COD_LOC_OUT;
		}
		else if (ExIntronnum > 0) {
			codLoc = COD_LOC_EXON;
			if((coord < gffGeneIso.ATGsite && gffGeneIso.isCis5to3()) || (coord > gffGeneIso.ATGsite && !gffGeneIso.isCis5to3())){        //����С��atg����5��UTR��,Ҳ������������
				codLocUTR = COD_LOCUTR_5UTR;
			}
			else if((coord > gffGeneIso.UAGsite && gffGeneIso.isCis5to3()) || (coord < gffGeneIso.UAGsite && !gffGeneIso.isCis5to3())){       //����cds��ʼ������3��UTR��
				codLocUTR = COD_LOCUTR_3UTR; 
			}
		}
		else {
			codLoc = COD_LOC_INTRON;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		setCod2SiteAbs();
		cod2ExInStart = gffGeneIso.getLoc2ExInStart(coord);
		cod2ExInEnd = gffGeneIso.getLoc2ExInEnd(coord);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		numExIntron = Math.abs(ExIntronnum);
	}
	protected abstract void setCod2SiteAbs();
	
	/**
	 * ��������5UTR����������ʱʹ��
	 * ����Ϊ����
	 */
	protected abstract void setCod2UTR5();
	
	/**
	 * ��������3UTR����������ʱʹ��
	 * ����Ϊ����
	 */
	protected abstract void setCod2UTR3();
	/**
	 * ����EXON��ʱ��ʹ�ã���cod�뱾mRNA�������յ�ľ���
	 * �������ں���
	 */
	protected abstract void setCod2StartEndmRNA();
	/**
	 * ����EXON��ʱ��ʹ�ã���cod�뱾mRNA��atg��uag�ľ���
	 * �������ں���
	 */
	protected abstract void setCod2StartEndCDS();

//	/**
//	 * ����ĳ����������ڵ��ں�����������Ŀ
//	 */
//	HashMap<Integer, Integer> hashLocExInNum;
//
//	/**
//	 * ����ĳ�����굽���ڵ��ں���/���������ľ���
//	 */
//	HashMap<Integer, Integer> hashLocExInStart;

	/**
	 * ������ʽ�Ķ�λ����
	 * null: ���ڸ�ת¼����
	 * 
	 * ָ��������������������peakץ��������ע�ͣ���Ҫ��ɸѡ�����ʵ�peakȻ���������ȽϹ���
	 * �����ϵĻ�����
	 * @param filterTss �Ƿ����tssɸѡ��null�����У�������У���ô������int[2],0��tss���ζ���bp  1��tss���ζ���bp����Ϊ���� <b>ֻ�е�filterGeneBodyΪfalseʱ��tss���βŻᷢ������</b>
	 * @param filterGenEnd �Ƿ����geneEndɸѡ��null�����У�������У���ô������int[2],0��geneEnd���ζ���bp  1��geneEnd���ζ���bp����Ϊ����<b>ֻ�е�filterGeneBodyΪfalseʱ��geneEnd���βŻᷢ������</b>
	 * @param filterGeneBody �Ƿ���geneBody��true��������geneBody�Ļ���ȫ��ɸѡ������false��������geneBody��ɸѡ<br>
	 * <b>��������ֻ�е�filterGeneBodyΪfalseʱ���ܷ�������</b>
	 * @param filter5UTR �Ƿ���5UTR��
	 * @param filter3UTR �Ƿ���3UTR��
	 * @param filterExon �Ƿ�����������
	 * @param filterIntron �Ƿ����ں�����
	 * 0-n:�����loc��Ϣ<br>
	 * n+1: ������<br>
	 * n+2: ������Ϣ<br>
	 **/
	public String getCodLocStrFilter(int[] filterTss, int[] filterGenEnd, 
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron) {
		boolean filter = false;
		if (filterTss != null) {
			if (cod2TSS >= -filterTss[0] && cod2TSS <= filterTss[1]) {
				filter = true;
			}
		}
		if (filterGenEnd != null) {
			if (cod2TES >= -filterGenEnd[0] && cod2TES <= filterGenEnd[1]) {
				filter = true;
			}
		}
		if (filterGeneBody && getCodLoc() != COD_LOC_OUT) {
			filter = true;
		}
		if (filter5UTR && getCodLocUTR() == COD_LOCUTR_5UTR) {
			filter = true;
		}
		if (filter3UTR && getCodLocUTR() == COD_LOCUTR_3UTR) {
			filter = true;
		}
		if (filterExon && getCodLoc() == COD_LOC_EXON) {
			filter = true;
		}
		if (filterIntron && getCodLoc() == COD_LOC_INTRON) {
			filter = true;
		}
		if (filter) {
			return getCodLocStr();
		}
		else {
			return null;
		}
	}
	
	/**
	 * ������ʽ�Ķ�λ����
	 * @return
	 * null: ���ڸ�ת¼����
	 */
	public String getCodLocStr() {
		String result = "";
		if (isCodInIsoExtend()) {
			return null;
		}
		//promoter
		if (isCodInIsoTss() && getCodLoc() == COD_LOC_OUT) {
			if (cod2TSS > GffGeneIsoInfo.PROMOTER_INTERGENIC_MAMMUM) {
				result = GffGeneIsoInfo.PROMOTER_INTERGENIC_STR;
			}
			else if (cod2TSS > GffGeneIsoInfo.PROMOTER_DISTAL_MAMMUM) {
				result = GffGeneIsoInfo.PROMOTER_DISTAL_STR;
			}
			else {
				result = GffGeneIsoInfo.PROMOTER_PROXIMAL_STR;;
			}
		}
		else if (isCodInIsoTss() && getCodLoc() != COD_LOC_OUT) {
			result = GffGeneIsoInfo.PROMOTER_DOWNSTREAMTSS_STR;
		}
		
		result = result + "Distance to Tss is: " + Math.abs(cod2TSS) + " ";
		//UTR
		if (codLocUTR == COD_LOCUTR_5UTR) {
			result = result + "5UTR_";
		}
		else if (codLocUTR == COD_LOCUTR_3UTR) {
			result = result + "3UTR_";
		}
		//exon intron
		if (codLoc == COD_LOC_EXON) {
			result = result + "Exon_Exon Position Number is:" + getCodExInNum();
		}
		else if (codLoc == COD_LOC_INTRON) {
			result = result + "Intron_Intron Position Number is:" + getCodExInNum();
		}
		//gene end
		if (isCodInIsoGenEnd()) {
			result = result + "Distance to GeneEnd: "+ getCod2Tes();
		}
		return result;
	}
}
