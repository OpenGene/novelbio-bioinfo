package com.novelbio.database.service;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringFactory {
	static ApplicationContext ctx;
	static protected BeanFactory factory;
	private static ApplicationContext context;
	public static BeanFactory getFactory() {
		if (factory == null) {
			ctx = new ClassPathXmlApplicationContext("spring_bioinfo.xml");
			factory = (BeanFactory) ctx;
			
			//TODO 这里可以动态绑定数据库
//			Mongo mongo = factory.getBean(Mongo.class);
//			mongo.close();
//			List<ServerAddress> seeds = new ArrayList<>();
//			try { seeds.add(new ServerAddress("192.168.0.104", 27017)); } catch (UnknownHostException e) { }
//			mongo.set(seeds);
		}
		return factory;
	}
	/**
	 * 根据id拿到spring容器中的bean
	 * @param id
	 * @return
	 */
	public static Object getBean(String id) {
		if (context == null) {
			context = new ClassPathXmlApplicationContext("spring.xml");
		}
		return context.getBean(id);
	}
	public static void main(String[] args) {
		SpringFactory.getBean("springHelper");
	}
	
	/**
	 * 根据class拿到spring容器中的bean
	 * @param <T>
	 * @param id
	 * @return
	 */
	public static <T> T getBean(Class<T> requiredType) {
		if (context == null) {
			context = new ClassPathXmlApplicationContext("spring.xml");
		}
		return context.getBean(requiredType);
	}
}
