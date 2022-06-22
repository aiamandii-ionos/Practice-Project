package com.ionos.project.mapper;

import com.ionos.project.dto.RequestDto;
import com.ionos.project.model.Request;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface RequestMapper {

    Request toEntity(RequestDto serverDTO);

    RequestDto toDTO(Request request);
}
