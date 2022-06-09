package com.ionos.project.service;

import com.ionos.project.exception.InternalServerError;
import com.ionoscloud.*;
import com.ionoscloud.api.*;
import com.ionoscloud.auth.HttpBasicAuth;
import com.ionoscloud.model.*;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

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

    @Inject
    Logger logger;

    public IonosCloudService() {
        defaultClient = Configuration.getDefaultApiClient();
        serverApi = new ServerApi(defaultClient);
        dataCenterApi = new DataCenterApi(defaultClient);
        requestApi = new RequestApi(defaultClient);
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

        ApiResponse<Datacenter> result = null;
        try {
            result = dataCenterApi.datacentersPostWithHttpInfo(datacenter,
                    true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
        }
        return result;
    }

    public Datacenters findAllDatacenters() {
        Datacenters result = null;
        try {
            result = dataCenterApi.datacentersGet(true, 0, contractNumber, 0, 1000);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
        }
        return result;
    }

    public ApiResponse<Server> createServer(Datacenter datacenter, com.ionos.project.model.Server server) {
        Server serverIonos = new Server();
        ServerProperties serverProperties = new ServerProperties();
        serverProperties.setName(server.getName());
        serverProperties.setCores(server.getCores());
        serverProperties.setRam(server.getRam());
        serverIonos.setProperties(serverProperties);

        ApiResponse<Server> result = null;
        try {
            result = serverApi.datacentersServersPostWithHttpInfo(datacenter.getId(), serverIonos, true, 0, contractNumber);
        } catch (ApiException e) {
            logger.error(e.getStackTrace());
            logger.error("Status code " + e.getCode());
            logger.error("Response body " + e.getResponseBody());
            logger.error("Response headers " + e.getResponseHeaders());
        }
        return result;
    }

    public String getRequestId(Map<String, List<String>> headers) {
        String[] split = headers.get("location").toString().split("/");
        return split[split.length - 2];
    }

    public void checkRequestStatusIsDone(String requestId) {
        try {
            RequestStatus requestStatus = requestApi.requestsStatusGet(requestId, true, 0, contractNumber);
            while (!Objects.equals(Objects.requireNonNull(requestStatus.getMetadata()).getStatus(), RequestStatusMetadata.StatusEnum.DONE)) {
                logger.info(requestStatus);
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
        }
    }
}