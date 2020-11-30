package com.nikhilm.hourglass.task.resource;


import com.nikhilm.hourglass.task.models.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskValidatorTest {


    @Test
    public void testValidate()  {
        Errors errors = mock(Errors.class);
        TaskValidator taskValidator = new TaskValidator();
        Task task = new Task();
        task.setName("");
        taskValidator.validate(task, errors);
        verify(errors, times(1)).rejectValue(anyString(), anyString());
    }
    @Test
    public void testHasErrors() {
        TaskValidator taskValidator = new TaskValidator();
        Task task = new Task();
        task.setName("name");
        task.setDescription("");
        assertTrue(taskValidator.hasErrors(task));

    }
    @Test
    public void testSupports()  {
        assertTrue(new TaskValidator().supports(Task.class));
    }

}