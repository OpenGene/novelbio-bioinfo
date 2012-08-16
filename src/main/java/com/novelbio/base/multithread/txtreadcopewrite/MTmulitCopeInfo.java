package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.ArrayList;

import com.novelbio.base.RunGetInfo;
import com.novelbio.base.RunProcess;

public abstract class MTmulitCopeInfo<T extends MTrecordCoper<K>, K extends MTRecordCope> implements RunGetInfo<K> {
	int threadStopNum = 0;
	/** 一个线程读取 */
	MTRecoreReader mtOneThreadReadFile;	
	protected ArrayList<T> lsCopeRecorders = new ArrayList<T>();
	
	/** 将read对象保存起来 */
	public void setReader(MTRecoreReader mtOneThreadReadFile) {
		this.mtOneThreadReadFile = mtOneThreadReadFile;
	}
	/**添加线程，当然也可以在外面包装一个 方法新建线程 */
	public void addMTcopedRecord(T mTcopeRecorder) {
		lsCopeRecorders.add(mTcopeRecorder);
	}
	@Override
	public void done(RunProcess<K> runProcess) {
		synchronized (this) {
			doneOneThread(runProcess);
			threadStopNum ++;
			if (threadStopNum == lsCopeRecorders.size()) {
				doneAllThread();
			}
		}
	}
	public void execute() {
		threadStopNum = 0;
		beforeExecute();
		startThread();
	}
	/** 在启动前做的准备工作 */
	protected abstract void beforeExecute();		
	
	protected void startThread() {
		mtOneThreadReadFile.setLsCopedThread(lsCopeRecorders);
		
		Thread thread = new Thread(mtOneThreadReadFile);
		thread.start();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.setRunGetInfo(this);
			thread = new Thread(copeRecorder);
			thread.start();
		}
	}
	/** 某个线程完成时的工作，不需要synchronized */
	protected abstract void doneOneThread(RunProcess<K> runProcess);
	
	/** 全部线程完成时的工作，不需要synchronized */
	protected abstract void doneAllThread();
}
