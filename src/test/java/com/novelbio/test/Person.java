package com.novelbio.test;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@XStreamAlias("Person")
public class Person {
	@XStreamAsAttribute
	private String type;
	public void setType(String type) {
		this.type = type+"fefe";
	}
	public String getType() {
		return type;
	}
	
//	@XStreamAlias("firstname")
	  private String firstname;
//	@XStreamAlias("lastname")
	  private String lastname;
	  public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	  public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	  public String getFirstname() {
		return firstname;
	}
	  public String getLastname() {
		return lastname;
	}
	  // ... constructors and methods
}


