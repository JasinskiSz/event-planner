package com.szymon_jasinski.eventplanner.exceptions;

public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(Long id, String objectName) {
        super(String.format("%s not found with id: %d", objectName, id));
    }
}
