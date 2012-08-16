package com.novelbio.base;

public interface RunGetInfo<T> {
	/** ����������еĳ�����Ϣ��Ȼ�������Щ��Ϣ����������������
	 * @param info �Զ����������Ϣ
	 *  */
	public void setRunningInfo(T info);
	/** �߳���Ͻ��еĲ������������ø÷������̶߳��󡣽����ڶ���߳�ʱ��ѯ�����ĸ��߳� <br>
	 * ����̶߳���RunProcess�ദ��List����set�У����<b>������д</b>equal��hashcode<br>
	 * ��������ǿ������ת��Ȼ�����߳� <br>
	 * <br>
	 * �ڲ���Ҫ synchronized
	 */
	public void done(RunProcess<T> runProcess);
	/** �̹߳�����еĲ��� */
	public void threadSuspend();
	/** �ָ̻߳����еĲ��� */
	public void threadResume();
	/** �߳��жϽ��еĲ��� */
	public void threadStop();
	/** 
	 * ���趨��Ϣ<br>
	 * 	RunProcess.setRunGetInfo(this);<br>
	 * Ȼ�����и��߳�<br>
	 * 	Thread thread = new Thread(gffChrAnno);<br>
		thread.start();
	 *  */
	public void execute();
}
