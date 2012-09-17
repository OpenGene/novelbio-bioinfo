package com.novelbio.web.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.novelbio.web.annotation.go.GoParam;
import com.novelbio.web.model.User;
@Component
public class ValidGoParam {

	private static final Pattern EMAIL_PATTERN = Pattern // �Ϸ�Email������ʽ
			.compile("(?:\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,3}$)");

	public boolean supports(Class clazz) { // ��У����֧�ֵ�Ŀ����
		return clazz.equals(GoParam.class);
	}

	public void validate(Object target, Errors errors) { // ��Ŀ����������У�飬�����¼��errors��
		GoParam user = (GoParam) target; // 1 ����ΪUser����
		// ��-2 ͨ��Spring�ṩ��У�鹤������м򵥵Ĺ���У��
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name",
				"required.name", "�û���������д");
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password",
//				"required.password", "���벻��Ϊ��");
		validateEmail(user.getEmail(), errors); // ��-3 У��Email��ʽ
		validateName(user.getName(), errors);
	}

	private void validateEmail(String email, Errors errors) {// ��Email�Ϸ���У��
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email",
				"required.email", "Email����Ϊ��");
		Matcher m = EMAIL_PATTERN.matcher(email); // ��-1 ͨ��������ʽУ��Email��ʽ
		if (!m.matches()) {
			errors.rejectValue("email", "invalid.email", "Email��ʽ�Ƿ�");
		}
	}
	private void validateName(String name, Errors errors) {// ��Email�Ϸ���У��
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name",
				"required.name", "��������ȷ���û���");
		if (name.trim().length()>20) {
			errors.rejectValue("name", "invalid.name", "�û�������С��20");
		}
	}

}
