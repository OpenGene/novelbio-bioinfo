package com.novelbio.netty.client;

import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.novelbio.netty.task.TaskPipelineFactory;
import com.novelbio.web.model.task.TaskInfo;

/**
 * 客户端的基本实现类
 * @author novelbio
 *
 */
public class ClientBaseImpl implements IClient {
	/** 相当用来传输的工具 */
	public ChannelFuture future;
	public ClientBootstrap bootstrap;
	private SimpleChannelHandler handler;

	private static final Logger logger = Logger.getLogger(ClientBaseImpl.class
			.getName());

	@Override
	public boolean connectServer(SocketAddress socketAddress,SimpleChannelHandler handler) {
		this.handler = handler;
		// 创建客户端channel的辅助类,发起connection请求,两个参数，一个是boss的线程池，一个是worker执行的线程池。
		// 两个线程池都使用了java.util.concurrent.Executors中的线程池来创建
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		// 为factory定义一些参数
		// 是否重用地址
		bootstrap.setOption("reuseAddress", false);
		bootstrap.setOption("child.reuseAddress", false);
		// TODO 没查
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		// 设置读写内存大小
		bootstrap.setOption("readBufferSize", 1024);
		bootstrap.setOption("writeBufferSize", 1024);
		// ChannelPipeline的生产工厂，当接受到一个连接时，会生产一个新的ChannelPipeline
		bootstrap.setPipelineFactory(new TaskPipelineFactory(handler));
		logger.info("客户端开始连接 " + socketAddress.toString());
		future = bootstrap.connect(socketAddress);
		// 等待连接成功
		future.awaitUninterruptibly();
		if (!future.isSuccess()) {
			logger.info("连接服务器 " + socketAddress.toString() + " 发生异常!");
			return false;
		}
		return true;
	}

	@Override
	public void waitForSendTaskSucess() {
		// 等待或监听数据全部完成
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		// shutdown netty的线程执行器
		bootstrap.releaseExternalResources();
	}

	@Override
	public void reConnectServer(SocketAddress socketAddress) {
		this.stopConnectServer();
		logger.info("客户端重连中...");
		this.connectServer(socketAddress,handler);

	}

	@Override
	public void stopConnectServer() {
		logger.info("客户端主动断开连接!");
		if (future.getChannel() != null) {
			future.getChannel().close();
			bootstrap.releaseExternalResources();
		}
	}

	@Override
	public void assignTask(TaskInfo taskInfo) {
		future.getChannel().write(taskInfo);
	}

}
