package com.ionos.project.mapper;

import com.ionos.project.dto.*;
import com.ionos.project.model.Request;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface RequestMapper {

    Request toEntity(RequestDto requestDto);

    RequestDto toDTO(Request request);
    GetAllRequestsDto toGetAllRequestsDto(Request request);
    CreateRequestDto toCreateRequestDto(Request request);
    DeleteRequestDto toDeleteRequestDto(Request request);
}
