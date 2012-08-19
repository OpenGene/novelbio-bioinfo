package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.ArrayList;

import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;

public abstract class MTmulitCopeInfo<T extends MTrecordCoper<K>, K extends MTRecordCope> implements RunGetInfo<K> {
	int threadStopNum = 0;
	Boolean isFinished = false;
	/** һ���̶߳�ȡ */
	MTRecoreReader mtOneThreadReader;
	protected ArrayList<T> lsCopeRecorders = new ArrayList<T>();
	
	/** ��read���󱣴����� */
	public void setReader(MTRecoreReader mtOneThreadReadFile) {
		this.mtOneThreadReader = mtOneThreadReadFile;
	}
	/**����̣߳���ȻҲ�����������װһ�� �����½��߳� */
	public void addMTcopedRecord(T mTcopeRecorder) {
		lsCopeRecorders.add(mTcopeRecorder);
	}
	/**�����ȵ�����������
	 * @param time �����ʱ�䣬ÿ�����ʱ�����Ƿ����
	 * @return false �����նˣ�û����ɣ�true������ɹ�����
	 */
	public boolean isFinished(int time) {
		while (true) {
			if (isFinished == null ) {
				return false;
			}
			else if (isFinished == true) {
				return true;
			}
			try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	@Override
	public void done(RunProcess<K> runProcess) {
		synchronized (this) {
			doneOneThread(runProcess);
			threadStopNum ++;
			if (threadStopNum == lsCopeRecorders.size()) {
				mtOneThreadReader.close();
				doneAllThread();
				isFinished = true;
			}
		}
	}
	public void execute() {
		threadStopNum = 0;
		isFinished = false;
		beforeExecute();
		startThread();
	}
	
	@Override
	public void setRunningInfo(K info) {
		synchronized (this) {
			copeReadInfo(info);
		}
	}
	/** �趨��Ҫ���Ĺ���������Ҫ���� 
	 * ��д����ı�����������д��
	 * */
	protected abstract void copeReadInfo(K info);
	
	public void suspendThread() {
		mtOneThreadReader.threadSuspend();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.threadSuspend();
		}
	}

	public void resumeThread() {
		mtOneThreadReader.threadResume();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.threadResume();
		}
	}

	public void stopThread() {
		mtOneThreadReader.threadStop();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.threadStop();
		}
		isFinished = null;
	}
	
	@Override
	public void threadSuspended(RunProcess<K> runProcess) {}

	@Override
	public void threadResumed(RunProcess<K> runProcess) {}

	@Override
	public void threadStop(RunProcess<K> runProcess) {}
	
	/** ������ǰ����׼������ */
	protected abstract void beforeExecute();		
	
	protected void startThread() {
		isFinished = false;
		mtOneThreadReader.setLsCopedThread(lsCopeRecorders);
		Thread thread = new Thread(mtOneThreadReader);
		thread.start();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.setRunGetInfo(this);
			thread = new Thread(copeRecorder);
			thread.start();
		}
	}
	
	/** ĳ���߳����ʱ�Ĺ���������Ҫsynchronized */
	protected abstract void doneOneThread(RunProcess<K> runProcess);
	
	/** ȫ���߳����ʱ�Ĺ���������Ҫsynchronized, ����Ҫ�ر�reader */
	protected abstract void doneAllThread();
	
}
