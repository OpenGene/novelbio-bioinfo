package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.ArrayList;

import com.novelbio.base.RunGetInfo;
import com.novelbio.base.RunProcess;

public abstract class MTmulitCopeInfo<T extends MTrecordCoper<K>, K extends MTRecordCope> implements RunGetInfo<K> {
	int threadStopNum = 0;
	/** һ���̶߳�ȡ */
	MTRecoreReader mtOneThreadReadFile;	
	protected ArrayList<T> lsCopeRecorders = new ArrayList<T>();
	
	/** ��read���󱣴����� */
	public void setReader(MTRecoreReader mtOneThreadReadFile) {
		this.mtOneThreadReadFile = mtOneThreadReadFile;
	}
	/**����̣߳���ȻҲ�����������װһ�� �����½��߳� */
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
	/** ������ǰ����׼������ */
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
	/** ĳ���߳����ʱ�Ĺ���������Ҫsynchronized */
	protected abstract void doneOneThread(RunProcess<K> runProcess);
	
	/** ȫ���߳����ʱ�Ĺ���������Ҫsynchronized */
	protected abstract void doneAllThread();
}
