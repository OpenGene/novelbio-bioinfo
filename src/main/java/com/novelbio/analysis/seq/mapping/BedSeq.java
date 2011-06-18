package com.novelbio.analysis.seq.mapping;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * bed��ʽ���ļ���ͳͳ��������bed�ļ�
 * @author zong0jie
 *
 */
public class BedSeq {
	String bedFile = null;
	
	int lineNum = -1;
	
	public BedSeq(String bedFile) {
		this.bedFile = bedFile;
	}
	
	public int getSeqNum() {
		if (lineNum >= 0 ) {
			return lineNum;
		}
		else {
			TxtReadandWrite txtBed = new TxtReadandWrite();
			txtBed.setParameter(bedFile, false, true);
			try {
				lineNum = txtBed.ExcelRows();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lineNum;
	}
	
	
	
}
