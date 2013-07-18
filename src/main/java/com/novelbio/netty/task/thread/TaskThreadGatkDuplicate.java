package com.novelbio.netty.task.thread;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.novelbio.base.SpringFactory;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.gatk.GATKDuplicate;
import com.novelbio.util.JsonOperate;
import com.novelbio.web.model.task.TaskInfo;

public class TaskThreadGatkDuplicate implements Runnable {
	private static final Logger logger = Logger.getLogger(TaskThreadGatkDuplicate.class);
	private TaskInfo taskInfo;

	public TaskThreadGatkDuplicate(TaskInfo taskInfo) {
		this.taskInfo = taskInfo;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("GatkDuplicate任务 " + taskInfo.getTaskId() + " 线程已启动");
		//得到mongoTemplate来操作数据库
		MongoTemplate mongoTemplate = (MongoTemplate) SpringFactory.getFactory().getBean("mongoTemplate");
		//取得hdfs文件系统来操作文件
		FileSystem fileSystem = (FileSystem) SpringFactory.getFactory().getBean("hadoopFs");
		//用我们的json工具来解析taskInfo中的taskData
		JsonOperate jsonOperate = new JsonOperate(JSONObject.fromObject(taskInfo.getTaskData()));
		//从spring里获得全局变量tempDir临时文件目录，然后创建一个临时文件目录
		String tempDir = FileOperate.addSep(SpringFactory.getGlobeParameters().get("tempDir")) + "temp" + DateUtil.getDateAndRandom();
		//TODO 从任务数据信息里获取输入文件路径
		Path inputPath = new Path(taskInfo.getTaskData());
		//在临时文件夹下建立一个临时文件
		//TODO 这个结果文件也是需要从taskInfo里面获取的
		Path ouputPath = new Path(FileOperate.addSep(tempDir) + "abc.bam");
		try {
			//把文件从hdfs中下载为本地的临时文件
			fileSystem.copyToLocalFile(inputPath,ouputPath);
		} catch (IOException e) {
			logger.info("GatkDuplicate任务 " + taskInfo.getTaskId() + "出现异常:" + e.getMessage());
		}
		//根据刚刚下载下来的输入文件和临时文件夹实例化一个GATKDuplicate
		GATKDuplicate gatkDuplicate = new GATKDuplicate(ouputPath.toString(),tempDir);
		try {
			//开始duplicate,并把最后的结果文件上传到hdfs上
			//TODO 这个结果文件也应该保存在taskInfo传过来的输出结果路径下
			fileSystem.copyFromLocalFile(new Path(gatkDuplicate.removeDuplicate()), new Path("/"));
		} catch (IOException e) {
			logger.info("GatkDuplicate任务 " + taskInfo.getTaskId() + "出现异常:" + e.getMessage());
		}
		//最后成功后把这个临时文件夹删除掉,包括里面的所有子文件
		FileOperate.delFolder(tempDir);
	}
}
