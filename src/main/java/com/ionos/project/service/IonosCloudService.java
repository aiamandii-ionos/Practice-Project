package com.ionos.project.service;

import com.ionoscloud.*;
import com.ionoscloud.api.*;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IonosCloudService {
    ServerApi serverApi;
    DataCenterApi dataCenterApi;
    ApiClient defaultClient = Configuration.getDefaultApiClient();


}
