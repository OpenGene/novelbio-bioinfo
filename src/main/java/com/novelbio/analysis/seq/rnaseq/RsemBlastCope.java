package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections.functors.IfClosure;

import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.database.domain.geneanno.BlastInfo;

/** trinity��õĽ������blast����Ϊ����iso����blast��
 * ���Եõ��Ľ��Ҫ���������Ϊ���
 * ������ͬԴ����ѡ����
 * @author zong0jie
 *
 */
public class RsemBlastCope {
	BlastType blastType;
	String blastFile;
	
	HashSet<BlastInfo> setBlastInfo = new HashSet<BlastInfo>();
	
	public void setBlastFile(String blastFile) {
		this.blastFile = blastFile;
	}
	public void setBlastType(BlastType blastType) {
		this.blastType = blastType;
	}
	public void copeBlastFile() {
		
	}
}
/** ʵ��hashCode��equals������blastInfo���������hashset������ȥ�ظ� */
class BlastInfoHash extends BlastInfo {
	
}