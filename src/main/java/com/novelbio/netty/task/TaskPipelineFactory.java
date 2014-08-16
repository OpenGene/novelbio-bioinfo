package com.novelbio.netty.task;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 产生任务消息通道的工厂类
 * @author novelbio
 *
 */
public class TaskPipelineFactory implements ChannelPipelineFactory {
	private SimpleChannelHandler handler;

	public TaskPipelineFactory(SimpleChannelHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * 在DefaultChannelPipeline的过滤器 链中实现了 encode 、decode、handler
	 * 其中encode实现自ChannelDownstreamHandler接口
	 * decode、Handler实现自ChannelUpstreamHandler接口
	 * ObjectEncoder加密序列化对象
	 * 也就说明了在client发送消息的时候，默认按照顺序会先调用decode
	 * 在client接收到响应的时候，会按照顺序调用encode和Handler。
	 */
	@SuppressWarnings("deprecation")
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("encode", new ObjectEncoder());
		pipeline.addLast("decode", new ObjectDecoder());
		pipeline.addLast("handler", this.handler);
		return pipeline;
	}
}
