package com.novelbio.netty.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 反馈消息 线程池
 * @author novelbio
 *
 */
public class ClientThreadPool {
	public static ThreadPoolExecutor threadPoolExecutor = null;
	
	/**
	 * 提交反馈给客户端的消息线程到线程池
	 * 这个线程池不需要关闭
	 * @param thread
	 */
	public static void submitCliendThread(ClientThread thread) {
		if(threadPoolExecutor == null){
			threadPoolExecutor = new ThreadPoolExecutor(3, 10,
					10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10),
					new ThreadPoolExecutor.CallerRunsPolicy());
		}
		threadPoolExecutor.execute(thread);
	}

}
