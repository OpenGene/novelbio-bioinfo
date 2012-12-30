package com.novelbio.analysis.seq.sam;

import com.novelbio.analysis.seq.AlignRecord;

/** 这是类似监听器的东西
 * 只需要继承该接口，然后添加进 samfile reader中，
 * 读取sam文件后就可以获得结果
 * @author zong0jie
 *
 */
public interface AlignmentRecorder {
	public void addAlignRecord(AlignRecord alignRecord);
}
