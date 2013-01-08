package com.novelbio.web.model;

import java.io.File;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
public final class User {  
    
	
	private boolean check;
	public void setCheck(boolean check) {
		this.check = check;
	}
	public boolean getCheck() {
		return check;
	}
	
    private String name;  
      
 
    private String email;  
      
 
    private String text;  
    
    private String password;
    public void setPassword(String password) {
		this.password = password;
	}
    public String getPassword() {
		return password;
	}
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
      
    public String getEmail() {  
        return email;  
    }  
  
    public void setEmail(String email) {  
        this.email = email;  
    }  
  
    public String getText() {  
        return text;  
    }  
  
    public void setText(String text) {  
        this.text = text;  
    }  
    
    
    public MultipartFile  multipartFile;
    public void setFile(MultipartFile multipartFile) {  
        this.multipartFile = multipartFile;  
    }  
}  
