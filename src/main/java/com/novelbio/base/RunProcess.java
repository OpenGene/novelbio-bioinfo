package com.novelbio.base;
/**
 * ���������̣߳���Ҫ
 * 1. setAllLoopNum ���趨ѭ����Ŀ
 * 2. ��ѭ������� count++�����Ӽ�����
 * 3. ��ѭ������� stopCheck ����ֹ�߳�
 * @author zong0jie
 *
 */
public abstract class RunProcess implements Runnable{
	protected boolean flagRun = true;
	/** ������ */
	protected int count = 0;
	/** ����ѭ���� */
	protected int allLoopNum = 1;
	protected boolean suspendFlag = false;
	/** �Ƿ���� */
	boolean flagFinish = false;
	/**
	 * �趨��ѭ����
	 * @param allLoopNum
	 */
	protected void setAllLoopNum(int allLoopNum) {
		this.allLoopNum = allLoopNum;
	}
	/** ��ñ��� */
	public double getProperty() {
		return (double)count/allLoopNum;
	}
	/** ������ͣ */
	public void setSuspend() {
		this.suspendFlag = true;
	}
	/** ���ָ̻� 
	 * */
	public synchronized void setResume() {
		this.suspendFlag = false;
		notify();
	}
	/** ��ֹ�̣߳���ѭ�������<br>
	 * if (!flagRun)<br>
	*			break; */
	public void stopThread() {
		flagRun = false;
	}
	/**
	 * ����ѭ���У�����Ƿ���ֹ�߳�
	 */
	protected void stopCheck() {
		synchronized (this) {
			while (suspendFlag){
				try {wait();} catch (InterruptedException e) {}
			}
		}
	}
	@Override
	public void run() {
		running();
		flagFinish = true;
	}
	/** ����ģ��д��������棬�����������Զ��ὫflagFinish�趨Ϊtrue */
	protected abstract void running();
	public boolean isFinished() {
		return flagFinish;
	}
}
