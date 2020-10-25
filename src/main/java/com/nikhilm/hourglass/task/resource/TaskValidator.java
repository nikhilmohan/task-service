package com.nikhilm.hourglass.task.resource;

import com.nikhilm.hourglass.task.models.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Slf4j
public class TaskValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Task.class.isAssignableFrom(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {


        Task request = (Task) target;
        if (request.getName() == null || request.getName().isEmpty()) {
            log.info("Does it evaluate" + request.getName());
          errors.rejectValue("name","Name is missing");
        }
    }
    public boolean hasErrors(Object target)  {
        Task task = (Task) target;
        if (task.getName() == null || task.getName().trim().isEmpty()
            || (task.getDescription() == null || task.getDescription().trim().isEmpty()))   {
            log.info("validation failed " + task.getName() + task.getDescription());
            return true;
        }
        return false;

    }
}