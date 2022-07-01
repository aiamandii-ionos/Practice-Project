package com.ionos.project.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ionos.project.dto.*;
import com.ionos.project.exception.*;
import com.ionos.project.model.Request;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.mapstruct.*;

@Mapper(componentModel = "cdi")
public interface RequestMapper {

    Request toEntity(RequestDto requestDto);

    RequestDto toDTO(Request request);
    GetAllRequestsDto toGetAllRequestsDto(Request request);

    @Mapping(target = "properties", expression = "java(deserialize(request.getProperties()))")
    CreateRequestDto toCreateRequestDto(Request request);
    DeleteRequestDto toDeleteRequestDto(Request request);

    default JSONObject deserialize(String properties) {
        try {
            return new ObjectMapper().readValue(properties, JSONObject.class);
        } catch (Exception e){
            throw new InternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
