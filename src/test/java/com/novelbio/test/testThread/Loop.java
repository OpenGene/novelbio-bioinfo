package com.novelbio.test.testThread;

import com.novelbio.base.RunProcess;

public class Loop  extends RunProcess {
//	private boolean suspendFlag = false;// 控制线程的执行
	
	public static void main(String[] args) throws InterruptedException {
		
		Loop loop = new Loop();
		Thread thread = new Thread(loop);
		thread.start();
		System.out.println("current:  " + loop.getProperty());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loop.setSuspend();
		System.out.println("current:  " + loop.getProperty());
	System.out.println(thread.isAlive());
		Thread.sleep(2000);
		loop.setResume();
		Thread.sleep(1000);
		loop.stopThread();
		Thread.sleep(1000);
		System.out.println(thread.isAlive());
	}

	public void run() {
		setAllLoopNum(1000);
		for (int i = 0; i < 1000; i++) {
			System.out.println(i);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			if (!flagRun){
				break;
			}
				
			stopCheck();
			count++;
		}
	}
	
}
