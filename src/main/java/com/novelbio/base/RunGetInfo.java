package com.novelbio.base;

public interface RunGetInfo<T> {
	/** ����������еĳ�����Ϣ��Ȼ�������Щ��Ϣ����������������
	 * @param info �Զ����������Ϣ
	 *  */
	public void setRunningInfo(T info);
	/** �߳���Ͻ��еĲ��� */
	public void done();
	/** �̹߳�����еĲ��� */
	public void threadSuspend();
	/** �ָ̻߳����еĲ��� */
	public void threadResume();
	/** �߳��жϽ��еĲ��� */
	public void threadStop();
	/** ���и��߳� */
	public void execute();
}
