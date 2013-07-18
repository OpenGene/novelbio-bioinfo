package com.novelbio.netty.task;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.novelbio.base.SpringFactory;
import com.novelbio.netty.client.ClientThread;
import com.novelbio.netty.client.ClientThreadPool;
import com.novelbio.web.model.task.Computer;
import com.novelbio.web.model.task.TaskInfo;
/**
 * 任务工厂类
 * 
 * @author novelbio
 * 
 */
public class TaskFactory {
	private static final Logger logger = Logger.getLogger(TaskFactory.class);

	/**
	 * 创建任务线程
	 * 
	 * @param task
	 */
	public static void createTaskThread(TaskInfo taskInfo) {
		logger.info("开始执行任务 " + taskInfo.getTaskId());
		try {
			Runnable taskThread = EnumTaskType.getTaskThread(taskInfo);
			Thread thread = new Thread(taskThread);
			thread.start();
			// 以上可以扩展
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("任务 " + taskInfo.getTaskId() + " 执行异常！");
			taskInfo.setProgress(101);
			feedbackTask(taskInfo);
		}

	}
	
	/***
	 * 反馈任务消息给web服务器
	 * @param taskInfo
	 * @param mongoTemplate
	 */
	public static void feedbackTask(TaskInfo taskInfo) {
		Computer computer = findCurrentWebServer();
		if (computer != null) {
			//建立一个反馈消息的线程到线程池里云
			SocketAddress webServerAddress = new InetSocketAddress(computer.getIp(), computer.getPort());
			ClientThread clientThread = new ClientThread(taskInfo, webServerAddress);
			ClientThreadPool.submitCliendThread(clientThread);
		}
	}
	
	/**
	 * 找到当前正在运行的web服务器
	 * @param mongoTemplate
	 * @return
	 */
	private static Computer findCurrentWebServer() {
		MongoTemplate mongoTemplate = (MongoTemplate) SpringFactory.getFactory().getBean("mongoTemplate");
		List<Computer> lsComputers = mongoTemplate.find(new Query(Criteria.where("openState").is(1).and("type").is(1)), Computer.class);
		if (lsComputers.size() != 0) {
			return lsComputers.get(0);
		}
		return null;
	}

}
