package com.sequenceiq.cloudbreak.cloud.event.instance;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.event.CloudPlatformRequest;
import com.sequenceiq.cloudbreak.cloud.event.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;

import reactor.rx.Promise;

public class StopInstancesRequest<T> extends CloudPlatformRequest<T> {

    private List<CloudInstance> cloudInstances;

    public StopInstancesRequest(CloudContext cloudContext, CloudCredential cloudCredential, List<CloudInstance> cloudInstances, Promise<T> result) {
        super(cloudContext, cloudCredential, result);
        this.cloudInstances = cloudInstances;
    }

    public List<CloudInstance> getCloudInstances() {
        return cloudInstances;
    }

    //BEGIN GENERATED CODE

    @Override
    public String toString() {
        return "StopInstancesRequest{" +
                "cloudInstances=" + cloudInstances +
                '}';
    }


    //END GENERATED CODE


}
