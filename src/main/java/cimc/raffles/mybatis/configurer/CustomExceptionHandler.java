package cimc.raffles.mybatis.configurer;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import cimc.raffles.mybatis.exception.CustomMessageException;

@ControllerAdvice
public class CustomExceptionHandler {
	@ResponseStatus
	@ResponseBody
	@ExceptionHandler(CustomMessageException.class)
	public Map<String, Object> handleCustomException(CustomMessageException e) {
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("timestamp", new Date());
		errorAttributes.put("exception", e.getClass().getName());
		errorAttributes.put("message", e.getLocalizedMessage());
        return errorAttributes;
	}
}
