package com.novelbio.nbcgui.controltest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.novelbio.database.service.SpringFactory;
import com.novelbio.nbcgui.controltest.CtrlGO;
import com.novelbio.nbcgui.controltest.CtrlGOPath;

/**
 * 给GO添加日志等
 * @author zong0jie
 *
 */
@Component
@Aspect
public class CtrlGOPathLog {
	public static void main(String[] args) {
//		Object obj = SpringFactory.getFactory().getBean("ctrlGO");
//		System.out.println(obj.getClass().getName());
		
		CtrlGO ctrlGO = (CtrlGO)SpringFactory.getFactory().getBean("ctrlGO");
		ctrlGO.clearParam();
		
		ctrlGO.saveExcel("ssssssssss");
	}
	@Before("execution (* com.novelbio.nbcgui.controltest.CtrlGOPath.saveExcel(*)) && args(excelPath) && target(ctrlGOPath)")
	 public void logRun(String excelPath, CtrlGOPath ctrlGOPath) {  
		
		excelPath = excelPath + "kkk";
		System.out.println("fese");
	}
	 
//	 @Before("logRun(String excelPath, CtrlGOPath ctrlGOPath)")
//	 private void copeGO(String excelPath, CtrlGOPath ctrlGOPath) {
//		excelPath = excelPath + "kkk";
//		System.out.println(excelPath);
//	 }
}
