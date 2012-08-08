package com.novelbio.base;
/**
 * <b>需要配合 RunGetInfo</b><br>
 * T: 本次running打算输出的中间信息
 * 进度条多线程，需要以下操作 <br>
 * 1. 在循环中添加 suspendCheck()  来挂起线程<br>
 * 2. 在循环中检查 flagRun 来终止循环<br>
 * 3: 在循环中添加 setRunInfo() 方法来获取运行时出现的信息
 * @author zong0jie
 *
 */
public abstract class RunProcess<T> implements Runnable {
	protected RunGetInfo<T> runGetInfo;
	
	protected boolean flagStop = false;
	protected boolean suspendFlag = false;
	/** 是否结束 */
	boolean flagFinish = false;
	
	/** 给定运行中需要修改的信息 */
	public void setRunGetInfo(RunGetInfo<T> runGetInfo) {
		this.runGetInfo = runGetInfo;
	}
	/** 程序暂停 */
	public void threadSuspend() {
		this.suspendFlag = true;
	}
	/** 进程恢复 */
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
	/** 终止线程，在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadStop() {
		synchronized (this) {
			flagStop = true;
		}
	}
	/**
	 * 放在循环中，检查是否终止线程
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
	/** 运行模块写在这个里面，这样结束后自动会将flagFinish设定为true */
	protected abstract void running();
	/**
	 * 设定输入的信息，内部回调
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
