package com.novelbio.base;
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
	protected RunGetInfo<T> runGetInfo;
	
	protected boolean flagStop = false;
	protected boolean suspendFlag = false;
	/** �Ƿ���� */
	boolean flagFinish = false;
	
	/** ������������Ҫ�޸ĵ���Ϣ */
	public void setRunGetInfo(RunGetInfo<T> runGetInfo) {
		this.runGetInfo = runGetInfo;
	}
	/** ������ͣ */
	public void threadSuspend() {
		this.suspendFlag = true;
	}
	/** ���ָ̻� */
	public synchronized void threadResume() {
		synchronized (this) {
			if (suspendFlag == false) {
				return;
			}
			this.suspendFlag = false;
			if (runGetInfo != null) {
				runGetInfo.threadResume();
			}
			notify();
		}
	}
	/** ��ֹ�̣߳���ѭ�������<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadStop() {
		synchronized (this) {
			flagStop = true;
		}
	}
	/**
	 * ����ѭ���У�����Ƿ���ֹ�߳�
	 */
	protected void suspendCheck() {
		synchronized (this) {
			while (suspendFlag){
				if (runGetInfo != null) {
					runGetInfo.threadSuspend();
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
	/** ����ģ��д��������棬�����������Զ��ὫflagFinish�趨Ϊtrue */
	protected abstract void running();
	/**
	 * �趨�������Ϣ���ڲ��ص�
	 * @param runInfo
	 */
	protected void setRunInfo(T runInfo) {
		synchronized (this) {
			if (runGetInfo != null) {
				runGetInfo.setRunningInfo(runInfo);
			}
		}
	}
	public boolean isFinished() {
		return flagFinish;
	}
}
