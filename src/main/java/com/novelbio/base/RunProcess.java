package com.novelbio.base;
/**
 * 进度条多线程，需要
 * 1. setAllLoopNum 来设定循环数目
 * 2. 在循环中添加 count++以增加计数器
 * 3. 在循环中添加 stopCheck 来终止线程
 * @author zong0jie
 *
 */
public abstract class RunProcess implements Runnable{
	protected boolean flagRun = true;
	/** 计数器 */
	protected int count = 0;
	/** 总体循环数 */
	protected int allLoopNum = 1;
	protected boolean suspendFlag = false;
	/** 是否结束 */
	boolean flagFinish = false;
	/**
	 * 设定总循环数
	 * @param allLoopNum
	 */
	protected void setAllLoopNum(int allLoopNum) {
		this.allLoopNum = allLoopNum;
	}
	/** 获得比例 */
	public double getProperty() {
		return (double)count/allLoopNum;
	}
	/** 程序暂停 */
	public void setSuspend() {
		this.suspendFlag = true;
	}
	/** 进程恢复 
	 * */
	public synchronized void setResume() {
		this.suspendFlag = false;
		notify();
	}
	/** 终止线程，在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void stopThread() {
		flagRun = false;
	}
	/**
	 * 放在循环中，检查是否终止线程
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
	/** 运行模块写在这个里面，这样结束后自动会将flagFinish设定为true */
	protected abstract void running();
	public boolean isFinished() {
		return flagFinish;
	}
}
