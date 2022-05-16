package org.acme.mapper;

import org.acme.dto.ServerDto;
import org.acme.model.Server;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ServerMapper {


    ServerDto toDTO(Server server);

    Server toEntity(ServerDto serverDTO);

}
