package com.novelbio.analysis.seq.sam;

import com.novelbio.analysis.seq.AlignRecord;

/** �������Ƽ������Ķ���
 * ֻ��Ҫ�̳иýӿڣ�Ȼ����ӽ� samfile reader�У�
 * ��ȡsam�ļ���Ϳ��Ի�ý��
 * @author zong0jie
 *
 */
public interface AlignmentRecorder {
	public void addAlignRecord(AlignRecord alignRecord);
}
