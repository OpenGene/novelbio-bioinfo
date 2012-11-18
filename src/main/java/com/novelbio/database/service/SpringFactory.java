package com.novelbio.database.service;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringFactory {
	static ApplicationContext ctx;
	static protected BeanFactory factory;
	public static BeanFactory getFactory() {
		if (factory == null) {
			ctx = new ClassPathXmlApplicationContext("spring.xml");
			factory = (BeanFactory) ctx;
		}
		return factory;
	}
}
