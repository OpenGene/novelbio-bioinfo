package com.novelbio.base;

public interface RunGetInfo<T> {
	/** 获得正在运行的程序信息，然后根据这些信息可以做想做的事情
	 * @param info 自定义的输入信息
	 *  */
	public void setRunningInfo(T info);
	/** 线程完毕进行的操作，输入会调用该方法的线程对象。仅用于多个线程时查询具体哪个线程 <br>
	 * 如果线程对象RunProcess类处于List或者set中，务必<b>不能重写</b>equal和hashcode<br>
	 * 这样可以强制类型转换然后获得线程 <br>
	 * <br>
	 * 内部需要 synchronized
	 */
	public void done(RunProcess<T> runProcess);
	/** 线程挂起进行的操作 */
	public void threadSuspend();
	/** 线程恢复进行的操作 */
	public void threadResume();
	/** 线程中断进行的操作 */
	public void threadStop();
	/** 
	 * 先设定信息<br>
	 * 	RunProcess.setRunGetInfo(this);<br>
	 * 然后运行该线程<br>
	 * 	Thread thread = new Thread(gffChrAnno);<br>
		thread.start();
	 *  */
	public void execute();
}
