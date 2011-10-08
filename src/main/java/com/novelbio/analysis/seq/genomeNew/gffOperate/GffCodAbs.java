package com.novelbio.analysis.seq.genomeNew.gffOperate;


/**
 * peak��λ������Ϣ�Ļ�����,����ֱ�ӿ�������CG��Peak<br>
 * ������GffCodInfoGene 
 * @author zong0jie
 */
public abstract class GffCodAbs<T extends GffDetailAbs> {

	/**
	 * �����������ʼ��Ϣ
	 */
	public static final int LOC_ORIGINAL = -1000000000;
	/**
	 * ���캯������ֵ
	 */
	protected  GffCodAbs(String chrID, int Coordinate) {
		this.chrID = chrID;
		this.Coordinate = Coordinate;
	}
	
	
	
	
	
	String chrID = "";
	int Coordinate = -1;
	/**
	 * ����Ⱦɫ��
	 * @return
	 */
	public String getChrID() {
		return chrID;
	}
	/**
	 * ���ؾ�������
	 * @return
	 */
	public int getCoord() {
		return Coordinate;
	}
	/**
	 * �����Ƿ�鵽 ���ҵ�/û�ҵ�
	 */
	protected boolean booFindCod = false;
	/**
	 * �Ƿ�ɹ��ҵ�cod
	 * @return
	 */
	public boolean findCod() {
		return booFindCod;
	}
	/**
	 * ��λ��� ��Ŀ��/��Ŀ��
	 */
	protected boolean insideLOC = false;
	/**
	 * ��λ��� ��Ŀ��/��Ŀ�⣬������Tss���κ�geneEnd����֮�����Ϣ
	 */
	public boolean isInsideLoc() {
		return insideLOC;
	}
	/**
	 * �Ƿ�����һ����Ŀ��
	 * @return
	 */
	public boolean isInsideUp() {
		if (gffDetailUp == null) {
			return false;
		}
		return gffDetailUp.isCodInGene();
	}
	/**
	 * �Ƿ�����һ����Ŀ��
	 * @return
	 */
	public boolean isInsideDown() {
		if (gffDetailDown == null) {
			return false;
		}
		return gffDetailDown.isCodInGene();
	}
	/**
	 * Ϊ�ϸ���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)<br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)
	 */
	protected T gffDetailUp = null;
	/**
	 * ֻ��geneDetail�õ�
	 * ����ϸ���Ŀ�ľ�����Ϣ
	 * @return
	 */
	public T getGffDetailUp()
	{
		return gffDetailUp;
	}

	/**
	 *  �������Ŀ�ڣ�Ϊ����Ŀ�ľ�����Ϣ��û�ж�λ�ڻ�������Ϊnull<br>
	 */
	protected T gffDetailThis = null;
	/**
	 * ֻ��geneDetail�õ�
	 * ��ñ���Ŀ�ľ�����Ϣ��
	 * �������ĿΪnull��˵��������Ŀ��
	 * @return
	 */
	public T getGffDetailThis()
	{
		return gffDetailThis;
	}
	/**
	 * ֻ��geneDetail�õ�
	 * Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ������)
	 */
	protected T gffDetailDown = null;
	/**
	 * ֻ��geneDetail�õ�
	 * �����һ����Ŀ�ľ�����Ϣ
	 * @return
	 */
	public T getGffDetailDown()
	{
		return gffDetailDown;
	}
	
	/**
	 * �ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1</b><br>
	 */
	protected int ChrHashListNumUp = -1;
	/**
	 * �ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1</b><br>
	 */
	public int getItemNumUp() {
		return ChrHashListNumUp;
	}
	/**
	 * Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�����ڣ���Ϊ-1<br>
	 */
	protected int ChrHashListNumThis = -1;
	/**
	 * Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�����ڣ���Ϊ-1<br>
	 */
	public int getItemNumThis() {
		return ChrHashListNumThis;
	}
	/**
	 * Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	protected int ChrHashListNumDown = -1;
	/**
	 * Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	public int getItemNumDown() {
		return ChrHashListNumDown;
	}
	
	
}
