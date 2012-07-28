package com.novelbio.base;

public interface RunGetInfo<T> {
	/** 获得正在运行的程序信息，然后根据这些信息可以做想做的事情
	 * @param info 自定义的输入信息
	 *  */
	public void setRunningInfo(T info);
	/** 线程完毕进行的操作 */
	public void done();
	/** 线程挂起进行的操作 */
	public void threadSuspend();
	/** 线程恢复进行的操作 */
	public void threadResume();
	/** 线程中断进行的操作 */
	public void threadStop();
	/** 运行该线程 */
	public void execute();
}
