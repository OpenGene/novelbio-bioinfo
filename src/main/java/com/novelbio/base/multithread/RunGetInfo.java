package com.novelbio.base.multithread;

public interface RunGetInfo<T> {
	/** ����������еĳ�����Ϣ��Ȼ�������Щ��Ϣ����������������
	 * �ڲ���Ҫ synchronized<br>
	 * @param info �Զ����������Ϣ
	 *  */
	public void setRunningInfo(T info);
	/** �߳���Ͻ��еĲ������������ø÷������̶߳��󡣽����ڶ���߳�ʱ��ѯ�����ĸ��߳� <br>
	 * ����̶߳���RunProcess�ദ��List����set�У����<b>������д</b>equal��hashcode<br>
	 * ��������ǿ������ת��Ȼ�����߳� <br>
	 * <br>
	 * �ڲ���Ҫ synchronized ����<br>
	 */
	public  void done(RunProcess<T> runProcess);
	/** �̹߳�����еĲ������Ǳ��߳������ö����ǵ����߳� */
	public void threadSuspended(RunProcess<T> runProcess);
	/** �ָ̻߳����еĲ������Ǳ��߳������ö����ǵ����߳� */
	public void threadResumed(RunProcess<T> runProcess);
	/** �߳��жϽ��еĲ������Ǳ��߳������ö����ǵ����߳� */
	public void threadStop(RunProcess<T> runProcess);
}
