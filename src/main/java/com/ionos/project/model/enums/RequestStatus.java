package com.ionos.project.model.enums;

public enum RequestStatus {
    TO_DO,
    IN_PROGRESS,
    DONE,
    FAILED;

    public static RequestStatus of(String status){
        if(status == null)
            return null;
        return valueOf(status);
    }
}