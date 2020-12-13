package com.nikhilm.hourglass.task.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorMap {
    private String status;
    private String message;
    private String exception;
    private String error;


}
