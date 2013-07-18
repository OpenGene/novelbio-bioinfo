package com.novelbio.netty.task.thread;

import java.util.Date;

import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.novelbio.base.SpringFactory;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.netty.task.TaskFactory;
import com.novelbio.web.model.task.TaskInfo;

public class TaskThreadMapping implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskThreadMapping.class);
	private TaskInfo taskInfo;

	public TaskThreadMapping(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}

	@Override
	public void run() {
		logger.info("mapping任务 " + taskInfo.getTaskId() + " 线程已启动");
		MongoTemplate mongoTemplate = (MongoTemplate) SpringFactory.getFactory().getBean("mongoTemplate");
		FileSystem fileSystem = (FileSystem) SpringFactory.getFactory().getBean("hadoopFs");
		// TODO Mapping的具体实现过程，这过程中要不定时的反馈信息
		taskInfo.setStartDate(DateUtil.date2String(new Date(), DateUtil.PATTERN_DATETIME));
		try {
			for (int i = 1; i < 101; i++) {
				Thread.sleep(1000);
				if (i == 20) {
					//测试异常
					int c = i / 0;
				}
				taskInfo.setProgress(i);
				TaskFactory.feedbackTask(taskInfo);
			}
			taskInfo.setFinishDate(DateUtil.date2String(new Date(), DateUtil.PATTERN_DATETIME));
			TaskFactory.feedbackTask(taskInfo);
		} catch (Exception e) {
			//异常原因
			taskInfo.setFailReason(TaskThreadMapping.class.getName() + ": " + e.getMessage());
			taskInfo.setProgress(101);
			taskInfo.setFinishDate(DateUtil.date2String(new Date(), DateUtil.PATTERN_DATETIME));
			TaskFactory.feedbackTask(taskInfo);
			logger.info("mapping任务 " + taskInfo.getTaskId() + " 执行失败");
		}
	}

}
