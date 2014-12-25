package com.novelbio.netty.client;

import java.net.SocketAddress;

import org.springframework.util.StopWatch.TaskInfo;



/**
 * 客户端线程类，用来启动一个客户端线程
 * @author novelbio
 *
 */
public class ClientThread extends ClientBaseImpl implements Runnable {
	private TaskInfo taskInfo;
	private SocketAddress socketAddress;
	
	public ClientThread(TaskInfo taskInfo,SocketAddress socketAddress) {
		this.taskInfo = taskInfo;
		this.socketAddress = socketAddress;
	}
	
	/**
	 * 连接服务器
	 */
	public boolean connectServer() {
		return super.connectServer(socketAddress, new ClientReceiverHander());
	}
	
	@Override
	public void run() {
		if(connectServer()){
			super.assignTask(taskInfo);
			super.waitForSendTaskSucess();
		}else{
			reConnectServer(socketAddress);
		}
	}
}
