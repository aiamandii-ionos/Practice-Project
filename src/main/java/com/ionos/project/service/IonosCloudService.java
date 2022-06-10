package com.ionos.project.service;

import com.ionos.project.exception.InternalServerError;
import com.ionoscloud.*;
import com.ionoscloud.api.*;
import com.ionoscloud.auth.HttpBasicAuth;
import com.ionoscloud.model.*;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class IonosCloudService {

    Integer contractNumber;
    ApiClient defaultClient;
    ServerApi serverApi;
    DataCenterApi dataCenterApi;
    RequestApi requestApi;
    IpBlocksApi ipBlockApi;
    VolumeApi volumeApi;
    NicApi nicApi;
    LanApi lanApi;

    @Inject
    Logger logger;

    public IonosCloudService() {
        defaultClient = Configuration.getDefaultApiClient();
        serverApi = new ServerApi(defaultClient);
        dataCenterApi = new DataCenterApi(defaultClient);
        requestApi = new RequestApi(defaultClient);
        ipBlockApi = new IpBlocksApi(defaultClient);
        volumeApi = new VolumeApi(defaultClient);
        nicApi = new NicApi(defaultClient);
        lanApi = new LanApi(defaultClient);
        prepareCredentials();
    }

    public void prepareCredentials() {
        HttpBasicAuth basicAuthentication = (HttpBasicAuth) defaultClient.getAuthentication("Basic Authentication");
        basicAuthentication.setUsername(ConfigProvider
                .getConfig().getValue("usernameIonos", String.class));
        basicAuthentication.setPassword(ConfigProvider
                .getConfig().getValue("passwordIonos", String.class));
        contractNumber = ConfigProvider.getConfig().getValue("contractNumberIonos", Integer.class);
    }

    public ApiResponse<Datacenter> createDatacenter() {
        Datacenter datacenter = new Datacenter();
        DatacenterProperties datacenterProperties = new DatacenterProperties();
        datacenterProperties.setLocation("us/las");
        datacenterProperties.setName("Datacenter");
        datacenterProperties.setSecAuthProtection(true);
        datacenter.setProperties(datacenterProperties);

        try {
            return dataCenterApi.datacentersPostWithHttpInfo(datacenter,
                    true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public Datacenters findAllDatacenters() {
        try {
            return dataCenterApi.datacentersGet(true, 0, contractNumber, 0, 1000);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public IpBlocks findAllIpBlocks() {
        try {
            return ipBlockApi.ipblocksGet(true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Server> createServer(String datacenterId, com.ionos.project.model.Server server) {
        Server serverIonos = new Server();
        ServerProperties serverProperties = new ServerProperties();
        serverProperties.setName(server.getName());
        serverProperties.setCores(server.getCores());
        serverProperties.setRam(server.getRam());
        serverIonos.setProperties(serverProperties);

        try {
            return serverApi.datacentersServersPostWithHttpInfo(datacenterId, serverIonos, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public String getRequestId(Map<String, List<String>> headers) {
        String[] split = headers.get("location").toString().split("/");
        return split[split.length - 2];
    }

    public void checkRequestStatusIsDone(String requestId) {
        logger.info("Check request status");
        try {
            RequestStatus requestStatus = requestApi.requestsStatusGet(requestId, true, 0, contractNumber);
            while (!Objects.equals(Objects.requireNonNull(requestStatus.getMetadata()).getStatus(), RequestStatusMetadata.StatusEnum.DONE)) {
                if (Objects.requireNonNull(requestStatus.getMetadata().getStatus()).equals(RequestStatusMetadata.StatusEnum.FAILED)) {
                    throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
                }
                requestStatus = requestApi.requestsStatusGet(requestId, true, 0, contractNumber);
            }
            logger.info("Final status" + requestStatus);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Object> deleteDatacenter(String datacenterId) {
        try {
            return dataCenterApi.datacentersDeleteWithHttpInfo(datacenterId, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Server> updateServer(String datacenterId, String serverId, com.ionos.project.model.Server server) {
        Server newServer = new Server();
        ServerProperties serverProperties = new ServerProperties();
        serverProperties.setName(server.getName());
        serverProperties.setCores(server.getCores());
        serverProperties.setRam(server.getRam());
        newServer.setProperties(serverProperties);

        try {
            return serverApi.datacentersServersPutWithHttpInfo(datacenterId, serverId, newServer, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<IpBlock> createIpBlock() {
        IpBlock ipBlock = new IpBlock();
        IpBlockProperties ipBlockProperties = new IpBlockProperties();
        ipBlockProperties.setLocation("us/las");
        ipBlockProperties.setSize(1);
        ipBlockProperties.setName("Ip block");
        ipBlock.setProperties(ipBlockProperties);

        try {
            return ipBlockApi.ipblocksPostWithHttpInfo(ipBlock, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Object> deleteIpBlock(String ipBlockId) {
        try {
            return ipBlockApi.ipblocksDeleteWithHttpInfo(ipBlockId, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Volume> createVolume(String datacenterId, Integer storage) {
        Volume volume = new Volume();
        VolumeProperties volumeProperties = new VolumeProperties();
        volumeProperties.setName("Volume");
        volumeProperties.setSize(BigDecimal.valueOf(storage));
        volumeProperties.setImageAlias("ubuntu:latest");
        volumeProperties.setType(VolumeProperties.TypeEnum.HDD);
        volume.setProperties(volumeProperties);

        try {
            return volumeApi.datacentersVolumesPostWithHttpInfo(datacenterId, volume, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Object> deleteVolume(String dataCenterId, String volumeId) {
        try {
            return volumeApi.datacentersVolumesDeleteWithHttpInfo(dataCenterId, volumeId, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<LanPost> createLan(String dataCenterId) {
        LanPost lan = new LanPost();
        LanPropertiesPost lanPropertiesPost = new LanPropertiesPost();
        lanPropertiesPost.setName("Lan");
        lanPropertiesPost.setPublic(true);
        lan.setProperties(lanPropertiesPost);

        try {
            return lanApi.datacentersLansPostWithHttpInfo(dataCenterId, lan, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Object> deleteLan(String dataCenterId, String lanId) {
        try {
            return lanApi.datacentersLansDeleteWithHttpInfo(dataCenterId, lanId, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Nic> createNic(IpBlock ipBlock, LanPost lanPost, String dataCenterId, String serverId) {
        Nic nic = new Nic();
        NicProperties nicProperties = new NicProperties();
        nicProperties.setName("Nic");
        nicProperties.setIps(ipBlock.getProperties().getIps());
        nicProperties.setLan(Integer.valueOf(Objects.requireNonNull(lanPost.getId())));
        nic.setProperties(nicProperties);

        try {
            return nicApi.datacentersServersNicsPostWithHttpInfo(dataCenterId, serverId, nic, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponse<Object> deleteNic(String dataCenterId, String serverId, String nicId) {
        try {
            return nicApi.datacentersServersNicsDeleteWithHttpInfo(dataCenterId, serverId, nicId, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
            throw new InternalServerError(com.ionos.project.exception.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
