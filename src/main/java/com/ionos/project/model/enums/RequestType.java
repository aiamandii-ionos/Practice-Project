package com.ionos.project.model.enums;

public enum RequestType {
    CREATE_SERVER,
    UPDATE_SERVER,
    DELETE_SERVER;

    public static RequestType of(String type){
        if(type == null)
            return null;
        return valueOf(type);
    }
}
