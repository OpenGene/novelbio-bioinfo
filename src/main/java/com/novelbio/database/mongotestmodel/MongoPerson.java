package com.novelbio.database.mongotestmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

//本类保存到person这个表中
@Document(collection="person")
public class MongoPerson {
	@Id
	private String id;
	private long age = 0;
	/** 标题 */
	private String name;
	
//	@Transient
	private String info;
	
	public void setAge(long age) {
		this.age = age;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public long getAge() {
		return age;
	}
	public String getName() {
		return name;
	}

	
	public void setInfo(String info) {
		this.info = info;
	}
	public String getInfo() {
		return info;
	}
}
