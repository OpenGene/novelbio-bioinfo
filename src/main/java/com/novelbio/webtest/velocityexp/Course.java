package com.novelbio.webtest.velocityexp;

import java.util.Date;

public class Course {  
	private String id;  
	private String name;  
	private Instructor instructor;  
	private Date startDate;  
	private Date endDate;  
	public Date getEndDate() {  
	    return endDate;  
	}  
	public void setEndDate(Date endDate) {  
	    this.endDate = endDate;  
	}  
	public String getId() {  
	    return id;  
	}  
	public void setId(String id) {  
	    this.id = id;  
	}  
	public Instructor getInstructor() {  
	    return instructor;  
	}  
	public void setInstructor(Instructor instructor) {  
	    this.instructor = instructor;  
	}  
	public String getName() {  
	    return name;  
	}  
	public void setName(String name) {  
	    this.name = name;  
	}  
	public Date getStartDate() {  
	    return startDate;  
	}  
	public void setStartDate(Date startDate) {  
	    this.startDate = startDate;  
	}    
	}
