package com.novelbio.base;

public interface RunGetInfo<T> {
	/** ����������еĳ�����Ϣ��Ȼ�������Щ��Ϣ����������������
	 * @param info �Զ����������Ϣ
	 *  */
	public void setRunningInfo(T info);
	/** �߳���Ͻ��еĲ��� */
	public void done();
	/** �̹߳�����еĲ��� */
	public void suspendThread();
	/** �ָ̻߳����еĲ��� */
	public void wakeupThread();
	/** �߳��жϽ��еĲ��� */
	public void interruptThread();
}
