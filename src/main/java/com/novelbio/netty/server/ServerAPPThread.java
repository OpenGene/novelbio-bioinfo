package com.novelbio.netty.server;

import org.apache.log4j.Logger;

/**
 * 应用端服务器接收web客户端指派任务的监听线程
 * @author novelbio
 *
 */
public class ServerAPPThread extends ServerImpl implements Runnable{
	private static final Logger logger = Logger.getLogger(ServerAPPThread.class.getName());
	
	private int port;
	public ServerAPPThread(int port){
		this.port = port;
	}
	
	@Override
	public void run() {
		boolean result = start(port, new ServerHandlerForAPP());
		if(result){
			logger.info("应用端服务器已启动！监听"+port+"端口");
		}else{
			logger.error("应用端服务器端口被占用等原因引起的服务器启动异常！");
		}
	}

}
