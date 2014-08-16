package com.novelbio.netty.server;

import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * 服务器借口类
 * @author novelbio
 *
 */
public interface IServer {
	 /** 
     * 启动服务器
     */  
    public boolean start(int listenPort, SimpleChannelHandler handler);  
    /** 
     * 重启服务器
     */  
    public boolean restart();  
      
    /** 
     * 停止服务器的运行 
     */  
    public void stop();  
}
