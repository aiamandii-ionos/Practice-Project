package com.ionos.project.mapper;

import com.ionos.project.dto.ServerDto;
import com.ionos.project.model.Server;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ServerMapper {


    ServerDto toDTO(Server server);

    Server toEntity(ServerDto serverDTO);

}
