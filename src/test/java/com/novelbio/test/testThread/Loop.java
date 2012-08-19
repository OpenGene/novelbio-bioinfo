package com.novelbio.test.testThread;

import com.novelbio.base.multithread.RunProcess;

public class Loop  extends RunProcess {
//	private boolean suspendFlag = false;// 控制线程的执行
	
	public static void main(String[] args) throws InterruptedException {
		
		Loop loop = new Loop();
		Thread thread = new Thread(loop);
		thread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loop.threadSuspend();
		System.out.println("current:  " + loop.getProperty());
		System.out.println(thread.isAlive());
		Thread.sleep(2000);
		loop.threadResume();
		Thread.sleep(1000);
		loop.threadStop();
		Thread.sleep(1000);
		System.out.println(thread.isAlive());
	}

	public void running() {
		for (int i = 0; i < 1000; i++) {
			System.out.println(i);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			if (!flagStop){
				break;
			}
			suspendCheck();
		}
	}
	
}
