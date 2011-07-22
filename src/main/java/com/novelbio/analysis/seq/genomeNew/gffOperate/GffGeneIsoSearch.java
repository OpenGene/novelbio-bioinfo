package com.novelbio.analysis.seq.genomeNew.gffOperate;

import org.apache.log4j.Logger;

public abstract class GffGeneIsoSearch extends GffGeneIsoInfo {

	public GffGeneIsoSearch(GffGeneIsoInfo gffGeneIsoInfo, int coord) {
		super(gffGeneIsoInfo);
		this.coord = coord;
		searchCoord();
	}
	
	private static final Logger logger = Logger.getLogger(GffGeneIsoSearch.class);
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
	protected int coord = -100;
	
	/**
	 * ���굽��ת¼�����ľ��룬����������
	 * �������������Ϊ����������Ϊ����
	 */
	protected int cod2TSS = -1000000000;
	/**
	 * ���굽��ת¼���յ�ľ��룬����������
	 * �������յ�����Ϊ����������Ϊ����
	 */
	protected int cod2TES = -1000000000;
	
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���
	 * ���굽��ת¼�����ľ��룬ֻ��mRNAˮƽ������������
	 * һֱΪ����
	 */
	protected int cod2TSSmRNA = -1000000000;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���
	 * ���굽��ת¼���յ�ľ��룬ֻ��mRNAˮƽ������������
	 * �������յ�����Ϊ����������Ϊ����
	 */
	protected int cod2TESmRNA = -1000000000;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���<br>
	 * ���굽��ת¼��atg�ľ��룬ֻ��mRNAˮƽ������������<br>
	 * �������������Ϊ����������Ϊ����<br>
	 */
	protected int cod2ATGmRNA= -1000000000;
	/**
	 * ֻ�е����괦���������в��о��룬�������ں���<br>
	 * ���굽��ת¼��uag�ľ��룬ֻ��mRNAˮƽ������������<br>
	 * �������յ�����Ϊ����������Ϊ����<br>
	 * Cnn nn  nuaG ����Ϊ8
	 */
	protected int cod2UAGmRNA = -1000000000;
	
	
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں������ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInStart = -1000000000;
	/**
	 * ���������������/�ں����У�
	 * �������������/�ں����յ�ľ���
	 * ��Ϊ����
	 */
	protected int cod2ExInEnd = -1000000000;
	/**
	 * �����ں���
	 * ���굽ATG�ľ��룬����������.
	 * ��ATG����Ϊ����������Ϊ����
	 * @return
	 */
	protected int cod2ATG = -1000000000;
	/**
	 * �����ں���
	 * ���굽UAG�ľ��룬����������.
	 * ��UAG����Ϊ����������Ϊ����
	 * @return
	 */
	protected int cod2UAG = -1000000000;
	/**
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 * ʵ����Ŀ����1��ʼ����
	 */
	protected int numExIntron = -1;
	
	/**
	 * ������5UTR��3UTR���ǲ���
	 */
	protected int codLocUTR = 0;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR����㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2start/cod2cdsEnd
	 */
	protected int cod2UTRstartmRNA = -100000000;
	/**
	 * ʹ��ǰ���ж���UTR��
	 * ���������UTR�У��������UTR���յ㣬ע�������ȥ���ں���
	 * ��ȥ���ں��ӵ�ֱ����cod2atg/cod2End
	 */
	protected int cod2UTRendmRNA = -100000000;

	/**
	 * �����������ӡ��ں��ӻ����ڸ�ת¼����
	 * ��codLocExon��codLocIntron�Ƚϼ���
	 */
	protected int codLoc = 0;
	
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
	 * ��COD_LOC_EXON��COD_LOC_INTRON��COD_LOC_OUT����
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
	/**
	 * �����ڵڼ��������ӻ��ں����У�������ھ�Ϊ����
	 * ʵ����Ŀ����1��ʼ����
	 * @return
	 */
	public int getExInNum() {
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
	 */
	public int getCod2TESmRNA() {
		return cod2TESmRNA;
	}
	
	/**
	 */
	private void searchCoord()
	{
		setCod2ExInStartEnd();
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
	 * ��һ������ģ����������뱾 ������/�ں��� �� ���/�յ� �ľ���
	 */
	protected abstract void setCod2ExInStartEnd();
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
	protected abstract void codSearchNum();
	
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
	
	
}
