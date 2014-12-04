package com.novelbio.netty.client;

import java.net.SocketAddress;

import org.jboss.netty.channel.SimpleChannelHandler;
import org.springframework.util.StopWatch.TaskInfo;


/**
 * 客户端接口类
 * 
 * @author novelbio
 * 
 */
public interface IClient {
	/**
	 * 根据地址及消息处理工具连接服务器
	 */
	public boolean connectServer(SocketAddress socketAddress, SimpleChannelHandler handler);

	/**
	 * 开始监听等待任务成功发送至 执行项目的 客户端服务器
	 */
	public void waitForSendTaskSucess();

	/**
	 * 重连服务器
	 */
	public void reConnectServer(SocketAddress socketAddress);

	/**
	 * 断开连接
	 */
	public void stopConnectServer();

	/**
	 * 指派任务
	 */
	public void assignTask(TaskInfo taskInfo);

}
