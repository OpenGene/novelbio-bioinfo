package com.novelbio.database.service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;


public class SpringFactory {
	static ApplicationContext ctx;
	static protected BeanFactory factory;
	public static BeanFactory getFactory() {
		if (factory == null) {
			ctx = new ClassPathXmlApplicationContext("spring.xml");
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
}
