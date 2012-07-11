package com.novelbio.base;
/**
 * ���������̣߳���Ҫ���²��� <br>
 * 1. setAllLoopNum ���趨ѭ����Ŀ<br>
 * 2. ��ѭ������� addCount(int num) �����Ӽ�����<br>
 * 3. ��ѭ������� suspendCheck()  �������߳�<br>
 * 4. ��ѭ���м�� flagRun ����ֹѭ��
 * 5: ��ѭ������� setRunInfo() ��������ȡ����ʱ���ֵ���Ϣ
 * @author zong0jie
 *
 */
public abstract class RunProcess<T> implements Runnable{
	protected RunGetInfo<T> runGetInfo;
	
	protected boolean flagStop = false;
	protected boolean suspendFlag = false;
	/** �Ƿ���� */
	boolean flagFinish = false;
	
	public void setRunGetInfo(RunGetInfo<T> runGetInfo) {
		this.runGetInfo = runGetInfo;
	}

	/** ������ͣ */
	public void setSuspend() {
		this.suspendFlag = true;
	}
	/** ���ָ̻� */
	public synchronized void setResume() {
		synchronized (this) {
			if (suspendFlag == false) {
				return;
			}
			this.suspendFlag = false;
			if (runGetInfo != null) {
				runGetInfo.wakeupThread();
			}
			notify();
		}
	}
	/** ��ֹ�̣߳���ѭ�������<br>
	 * if (!flagRun)<br>
	*			break; */
	public void stopThread() {
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
					runGetInfo.suspendThread();
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
			runGetInfo.done();
		}
	}
	/** ����ģ��д��������棬�����������Զ��ὫflagFinish�趨Ϊtrue */
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
