package com.novelbio.analysis.seq.genomeNew2.mappingOperate;

import com.novelbio.analysis.seq.genomeNew2.gffOperate.GffCodAbs;

public interface MapInt {
	/**
	 * ����������
	 * @return
	 */
	public int getStart()
	{
		return startLoc;
	}
	/**
	 * ����յ�����
	 * @return
	 */
	public int getEnd()
	{
		return endLoc;
	}
	/**
	 * ��øû��������
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * ���ڱȽϵģ���С�����
	 */
	@Override
	public int compareTo(MapInfo map) {
		if (flag == map.weight) {
			return 0;
		}
		if (min2max) {
			return flag < map.weight ? -1:1;
		}
		else {
			return flag > map.weight ? -1:1;
		}
	}
	
	

}
