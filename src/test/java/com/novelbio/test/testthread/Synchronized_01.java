package com.novelbio.test.testthread;

public class Synchronized_01 {
	
	private void lock() {
		byte[] lock = new byte[0];
		synchronized (lock) {
		}
	}
	
	private void methord_A() {
		lock();
			String threadName = Thread.currentThread().getName();

			System.out.println("entering   methord_A: " + threadName);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}

			System.out.println("leaving   methord_A: " + threadName);
		
	
	}

	private void methord_B() {
		lock();
			String threadName = Thread.currentThread().getName();

			System.out.println("entering   methord_B: " + threadName);
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
			}
			System.out.println("leaving   methord_B: " + threadName);
		
	}

	public static void main(String[] args) {
		final Synchronized_01 syn = new Synchronized_01();

		Runnable runA = new Runnable() {
			public void run() {
				syn.methord_A();
			}
		};// end of new runA

		Runnable runB = new Runnable() {
			public void run() {
				syn.methord_A();
			}
		};// end of new runB

		Thread thread_A = new Thread(runA, "A ");
		Thread thread_B = new Thread(runB, "B ");

		thread_A.start();

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}

		thread_B.start();
	}
}
