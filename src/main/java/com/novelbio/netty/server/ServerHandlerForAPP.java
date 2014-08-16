package com.novelbio.netty.server;


import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.novelbio.netty.task.TaskFactory;
import com.novelbio.web.model.task.TaskInfo;

/**
 * slaver端 应用服务端响应请求的处理类
 * @author novelbio
 *
 */
public class ServerHandlerForAPP extends SimpleChannelHandler {
	private static final Logger logger = Logger.getLogger(ServerHandlerForAPP.class);
	
	private TaskInfo taskInfo;
	
	/**
	 * 接收到消息时的处理方法
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		taskInfo = (TaskInfo)e.getMessage();
		logger.info("APP服务器接收到任务: " + taskInfo.getTaskId() );
		//TODO 启动一个线程来执行任务
		TaskFactory.createTaskThread(taskInfo);
		e.getChannel().close();
	}
	
	/**
	 * 当客户端连接时的方法
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)  
            throws Exception {
		logger.info(e.getChannel().getRemoteAddress() + " 连接到本APP服务器");
    }
	

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error(e.getCause());
		if(taskInfo != null && taskInfo.getProgress() != 100){
			taskInfo.setProgress(101);
			TaskFactory.feedbackTask(taskInfo);
		}
	}
}