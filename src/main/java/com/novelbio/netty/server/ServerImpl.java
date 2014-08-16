package com.novelbio.netty.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.novelbio.netty.task.TaskPipelineFactory;

/**
 * 服务器接口基本实现类
 * @author novelbio
 *
 */
public class ServerImpl implements IServer {
	//服务器发送消息的工具
	private Channel channel;
	private SimpleChannelHandler handler;
	private int port;
	private static final Logger logger = Logger.getLogger(ServerImpl.class
			.getName());

	@Override
	public boolean start(int listenPort,SimpleChannelHandler handler) {
		this.handler = handler;
		this.port = listenPort;
		// 启用一个TCP的factory 并创建一直线程池
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		// Server初始化channel的辅助类ServerBootstrap
		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		// 为factory定义一些参数
		// 是否重用地址
		bootstrap.setOption("reuseAddress", false);
		bootstrap.setOption("child.reuseAddress", false);
		//TODO 没查
		bootstrap.setOption("child.tcpNoDelay", true);    
        bootstrap.setOption("child.keepAlive", true); 
		// 设置读写内存大小
		bootstrap.setOption("readBufferSize", 1024);
		bootstrap.setOption("writeBufferSize", 1024);
		// ChannelPipeline的生产工厂，当接受到一个连接时，会生产一个新的ChannelPipeline
		bootstrap.setPipelineFactory(new TaskPipelineFactory(handler));
		// 新建一个监听端口
		SocketAddress serverAddress = new InetSocketAddress(port);
		// 绑定这个端口
		try{
			this.channel = bootstrap.bind(serverAddress);
		}catch(Exception e){
			return false;
		}
		
		return true;
	}

	@Override
	public boolean restart() {
		logger.info("服务器开始重启");
		this.stop();
		return this.start(port,handler);
		
	}

	@Override
	public void stop() {
		if (this.channel != null) {
			this.channel.close().addListener(ChannelFutureListener.CLOSE);
		}
	}
}

