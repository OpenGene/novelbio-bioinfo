package com.novelbio.analysis.seq.sam;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.mappingoperate.MapReads;
import com.novelbio.analysis.seq.mapping.Align;
/** 
 * 待读取的samFile必须排过序
 * 这是类似监听器的东西
 * 只需要继承该接口，然后添加进 samfile reader中，
 * 读取sam文件后就可以获得结果
 * @author zong0jie
 *
 */
public interface AlignmentRecorder {
	
	/** 返回本Recorder所记录的区域，超过这个区域就不理会了
	 * 返回null表示全程记录
	 *  */
	public Align getReadingRegion();
	
	public void addAlignRecord(AlignRecord alignRecord);
	/**
	 * 顺序读取sam/bam文件，读完最后一条染色体后做的总结工作，根据需要实现功能
	 * 譬如{@link MapReads}最后需要压缩染色体，就需要该summary方法。否则一般用不到
	 */
	public void summary();
}
