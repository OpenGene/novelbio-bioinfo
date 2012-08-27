package com.novelbio.base.multithread;
/**
 * <b>��Ҫ��� RunGetInfo</b><br>
 * T: ����running����������м���Ϣ
 * ���������̣߳���Ҫ���²��� <br>
 * 1. ��ѭ������� suspendCheck()  �������߳�<br>
 * 2. ��ѭ���м�� flagRun ����ֹѭ��<br>
 * 3: ��ѭ������� setRunInfo() ��������ȡ����ʱ���ֵ���Ϣ
 * @author zong0jie
 *
 */
public abstract class RunProcess<T> implements Runnable {
	protected RunGetInfo runGetInfo;
	
	protected boolean flagStop = false;
	protected boolean suspendFlag = false;
	/** �Ƿ���� */
	boolean flagFinish = false;
	
	/** ������������Ҫ�޸ĵ���Ϣ */
	public void setRunGetInfo(RunGetInfo runGetInfo) {
		this.runGetInfo = runGetInfo;
	}
	/** ������ͣ */
	public void threadSuspend() {
		this.suspendFlag = true;
	}
	/** ���ָ̻� */
	public void threadResume() {
		if (suspendFlag == false) {
			return;
		}
		this.suspendFlag = false;
		if (runGetInfo != null) {
			runGetInfo.threadResumed(this);
		}
		notify();
	}
	/** ��ֹ�̣߳���ѭ�������<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadStop() {
		threadResume();
		flagStop = true;		
		if (runGetInfo != null) {
			runGetInfo.threadStop(this);
		}
	}
	/**
	 * ����ѭ���У�����Ƿ���ֹ�߳�
	 */
	protected void suspendCheck() {
		byte[] lock = new byte[0];
		synchronized (lock) {
			while (suspendFlag){
				if (runGetInfo != null) {
					runGetInfo.threadSuspended(this);
				}
				try {wait();} catch (InterruptedException e) {}
			}
		}
	}
	@Override
	public void run() {
		running();
		flagFinish = true;
		if (runGetInfo != null) {
			runGetInfo.done(this);
		}
	}
	/** ��run�������á�����ģ��д��������棬�����������Զ��ὫflagFinish�趨Ϊtrue */
	protected abstract void running();
	/**
	 * �趨�������Ϣ���ڲ��ص�
	 * @param runInfo
	 */
	protected void setRunInfo(T runInfo) {
		if (runGetInfo != null) {
			runGetInfo.setRunningInfo(runInfo);
		}
	}
	public boolean isFinished() {
		return flagFinish;
	}
}
