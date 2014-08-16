package com.novelbio.netty.client;

import java.net.InetSocketAddress;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.novelbio.web.model.task.TaskInfo;



/**
 * 客户端消息处理工具类
 * @author novelbio
 *
 */
public class ClientReceiverHander extends SimpleChannelHandler {
	private static final Logger logger = Logger.getLogger(ClientReceiverHander.class);
	private TaskInfo taskInfo;
	/**
	 * 接收到消息所做的回应方法
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		taskInfo = (TaskInfo) e.getMessage();
		// TODO 干什么乱七八糟的事，比如推送任务状态给用户或者把任务状态写进数据库
		logger.info("任务 " + taskInfo.getTaskId() + " 状态:" + taskInfo.getProgress());
		if (taskInfo.getProgress() == 100 || taskInfo.getProgress() == 101) {
			e.getChannel().close();
		}
	}

	/**
	 * 连接到服务器时执行的方法
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		InetSocketAddress address = (InetSocketAddress) (e.getChannel()
				.getRemoteAddress());
		logger.info("已成功连接服务器: " + address.getAddress());
	}
	
	/**
	 * 服务器连接关闭时执行的方法
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		logger.info("已断开与服务器的连接: " + e.getChannel().getRemoteAddress().toString());
	}

	/**
	 * 发生异常时调用的方法
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error(e.getCause());
		if(taskInfo.getProgress() != 100){
			taskInfo.setProgress(101);
			logger.info("任务 " + taskInfo.getTaskId() + " 状态:" + taskInfo.getProgress());
		}
		if (e.getChannel() != null) {
			e.getChannel().close().addListener(ChannelFutureListener.CLOSE);
		}
	}
}
