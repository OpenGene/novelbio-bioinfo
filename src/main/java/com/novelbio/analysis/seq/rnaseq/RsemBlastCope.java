package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.collections.functors.IfClosure;

import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.database.domain.geneanno.BlastInfo;

/** trinity获得的结果会做blast，因为是用iso做的blast，
 * 所以得到的结果要进行整理成为表格
 * 仅将最同源的挑选出来
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
/** 实现hashCode和equals方法的blastInfo，方便放入hashset中用来去重复 */
class BlastInfoHash extends BlastInfo {
	
}