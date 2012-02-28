package com.novelbio.analysis.seq.genomeNew.listOperate;

public interface ElementAbs {
	public Boolean isCis5to3();
	/**
	 * ���ݷ�����ȡ
	 * @return
	 */
	public int getStartCis();
	/**
	 * �����С��ֵ
	 * @return
	 */
	public int getStartAbs();
	
	public int getEndCis();
	/**
	 * �������ֵ
	 * @return
	 */
	public int getEndAbs();
	
	public int getLen();
	
	/**
	 * ���ر�element������
	 * @return
	 */
	public String getName();
	/**
	 * ����listabs��name
	 * @return
	 */
	public String getParentName();
	
	public boolean equals(Object obj);
	
}

