package com.novelbio.base.dataStructure.listOperate;



/**
 * peak��λ������Ϣ�Ļ�����,����ֱ�ӿ�������CG��Peak<br>
 * ������GffCodInfoGene 
 * @author zong0jie
 */
public class ListCodAbs<T extends ListDetailAbs> {

	/** �����������ʼ��Ϣ  */
	public static final int LOC_ORIGINAL = -1000000000;
	String chrID = "";
	int Coordinate = -1;
	/**  �����Ƿ�鵽 ���ҵ�/û�ҵ�  */
	protected boolean booFindCod = false;
	/** �ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1*/
	protected int ChrHashListNumUp = -1;
	/** Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br> �������Ŀ�����ڣ���Ϊ-1 */
	protected int ChrHashListNumThis = -1;
	/** Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1 */
	protected int ChrHashListNumDown = -1;
	/**
	 * Ϊ�ϸ���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)<br>
	 * 1: �������Ŀ�ڣ�Ϊ�¸���Ŀ�ľ�����Ϣ<br>
	 * �������Ŀ�䣬Ϊ�¸���Ŀ�ľ�����Ϣ�����û����Ϊnull(Ʃ�綨λ����ǰ��)
	 */
	protected T gffDetailUp = null;
	
	/**  ���캯������ֵ */
	public  ListCodAbs(String chrID, int Coordinate) {
		this.chrID = chrID;
		this.Coordinate = Coordinate;
	}
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
		return gffDetailUp.isCodInGene(Coordinate);
	}
	/**
	 * �Ƿ�����һ����Ŀ��
	 * @return
	 */
	public boolean isInsideDown() {
		if (gffDetailDown == null) {
			return false;
		}
		return gffDetailDown.isCodInGene(Coordinate);
	}
	/**
	 * 
	 * �Ƿ�����һ����Ŀ��
	 * ��չtss��tes����������
	 * @param upTss ����Ϊ����
	 * @param downTes ����Ϊ����
	 * @return
	 */
	public boolean isInsideUpExtend(int upTss, int downTes) {
		if (gffDetailUp == null) {
			return false;
		}
		gffDetailUp.setTssRegion(upTss, 0);
		gffDetailUp.setTesRegion(0, downTes);
		return gffDetailUp.isCodInGeneExtend(Coordinate);
	}
	/**
	 * �Ƿ�����һ����Ŀ��
	 * ��չtss��tes������������
	 * @return
	 */
	public boolean isInsideDownExtend(int upTss, int downTes) {
		if (gffDetailDown == null) {
			return false;
		}
		gffDetailDown.setTssRegion(upTss, 0);
		gffDetailDown.setTesRegion(0, downTes);
		return gffDetailDown.isCodInGeneExtend(Coordinate);
	}

	/**
	 * ֻ��geneDetail�õ�
	 * ����ϸ���Ŀ�ľ�����Ϣ
	 * @return
	 */
	public T getGffDetailUp() {
		return gffDetailUp;
	}
	public void setGffDetailUp(T gffDetailUp) {
		this.gffDetailUp = gffDetailUp;
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
	public T getGffDetailThis() {
		return gffDetailThis;
	}
	public void setGffDetailThis(T gffDetailThis) {
		this.gffDetailThis = gffDetailThis;
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
	public T getGffDetailDown() {
		return gffDetailDown;
	}
	public void setGffDetailDown(T gffDetailDown) {
		this.gffDetailDown = gffDetailDown;
	}

	/** �ϸ���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����ϸ���Ŀ�����ڣ���Ϊ-1*/
	public void setChrHashListNumUp(int chrHashListNumUp) {
		ChrHashListNumUp = chrHashListNumUp;
	}
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
	public void setChrHashListNumThis(int chrHashListNumThis) {
		ChrHashListNumThis = chrHashListNumThis;
	}
	/**
	 * Ϊ����Ŀ��ChrHash-list�еı�ţ���0��ʼ<br>
	 * �������Ŀ�����ڣ���Ϊ-1<br>
	 */
	public int getItemNumThis() {
		return ChrHashListNumThis;
	}
	/** Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1 */
	public void setChrHashListNumDown(int chrHashListNumDown) {
		ChrHashListNumDown = chrHashListNumDown;
	}
	/**
	 * Ϊ�¸���Ŀ��ChrHash-list�еı�ţ���0��ʼ��<b>����¸���Ŀ�����ڣ���Ϊ-1</b>
	 */
	public int getItemNumDown() {
		return ChrHashListNumDown;
	}
}
