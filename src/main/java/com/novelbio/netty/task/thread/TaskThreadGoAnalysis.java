package com.novelbio.netty.task.thread;

import org.apache.log4j.Logger;

import com.novelbio.web.model.task.TaskInfo;

public class TaskThreadGoAnalysis implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskThreadGoAnalysis.class);
	private TaskInfo taskInfo;

	public TaskThreadGoAnalysis(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("GoAnalysis任务 " + taskInfo.getTaskId() + " 线程已启动");
	}
}
