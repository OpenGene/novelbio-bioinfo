package com.novelbio.webtest.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.novelbio.webtest.annotation.go.GoParam;
import com.novelbio.webtest.model.User;
@Component
public class ValidGoParam {

	private static final Pattern EMAIL_PATTERN = Pattern // 合法Email正则表达式
			.compile("(?:\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,3}$)");

	public boolean supports(Class clazz) { // 该校验器支持的目标类
		return clazz.equals(GoParam.class);
	}

	public void validate(Object target, Errors errors) { // 对目标类对象进行校验，错误记录在errors中
		GoParam user = (GoParam) target; // 1 造型为User对象
		// ③-2 通过Spring提供的校验工具类进行简单的规则校验
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name",
				"required.name", "用户名必须填写");
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password",
//				"required.password", "密码不能为空");
		validateEmail(user.getEmail(), errors); // ③-3 校验Email格式
		validateName(user.getName(), errors);
	}

	private void validateEmail(String email, Errors errors) {// ④Email合法性校验
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email",
				"required.email", "Email不能为空");
		Matcher m = EMAIL_PATTERN.matcher(email); // ④-1 通过正则表达式校验Email格式
		if (!m.matches()) {
			errors.rejectValue("email", "invalid.email", "Email格式非法");
		}
	}
	private void validateName(String name, Errors errors) {// ④Email合法性校验
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name",
				"required.name", "请输入正确的用户名");
		if (name.trim().length()>20) {
			errors.rejectValue("name", "invalid.name", "用户名长度小于20");
		}
	}

}
