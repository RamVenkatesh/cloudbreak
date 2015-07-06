package com.sequenceiq.cloudbreak.service.stack.flow;

import static com.sequenceiq.cloudbreak.domain.Status.AVAILABLE;
import static com.sequenceiq.cloudbreak.domain.Status.DELETE_FAILED;
import static com.sequenceiq.cloudbreak.domain.Status.UPDATE_IN_PROGRESS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecwid.consul.v1.ConsulClient;
import com.google.common.collect.Sets;
import com.sequenceiq.cloudbreak.core.CloudbreakSecuritySetupException;
import com.sequenceiq.cloudbreak.core.flow.service.AmbariHostsRemover;
import com.sequenceiq.cloudbreak.domain.BillingStatus;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.HostMetadata;
import com.sequenceiq.cloudbreak.domain.HostMetadataState;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.InstanceGroupType;
import com.sequenceiq.cloudbreak.domain.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.InstanceStatus;
import com.sequenceiq.cloudbreak.domain.Resource;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.repository.HostMetadataRepository;
import com.sequenceiq.cloudbreak.repository.InstanceGroupRepository;
import com.sequenceiq.cloudbreak.repository.InstanceMetaDataRepository;
import com.sequenceiq.cloudbreak.service.PollingService;
import com.sequenceiq.cloudbreak.service.TlsSecurityService;
import com.sequenceiq.cloudbreak.service.cluster.flow.AmbariClusterConnector;
import com.sequenceiq.cloudbreak.service.events.CloudbreakEventService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.stack.connector.CloudPlatformConnector;
import com.sequenceiq.cloudbreak.service.stack.connector.UserDataBuilder;

@Service
public class StackScalingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackScalingService.class);
    private static final int POLLING_INTERVAL = 5000;
    private static final int MAX_POLLING_ATTEMPTS = 100;

    @Inject
    private StackService stackService;
    @Inject
    private UserDataBuilder userDataBuilder;
    @Inject
    private PollingService<ConsulContext> consulPollingService;
    @Inject
    private ConsulAgentLeaveCheckerTask consulAgentLeaveCheckerTask;
    @Inject
    private CloudbreakEventService eventService;
    @Inject
    private AmbariHostsRemover ambariHostsRemover;
    @Inject
    private InstanceGroupRepository instanceGroupRepository;
    @Inject
    private InstanceMetaDataRepository instanceMetaDataRepository;
    @Inject
    private HostMetadataRepository hostMetadataRepository;
    @Inject
    private TlsSecurityService tlsSecurityService;
    @Inject
    private AmbariClusterConnector ambariClusterConnector;
    @javax.annotation.Resource
    private Map<CloudPlatform, CloudPlatformConnector> cloudPlatformConnectors;

    public Set<Resource> addInstances(Long stackId, String instanceGroupName, Integer scalingAdjustment) throws Exception {
        Stack stack = stackService.getById(stackId);
        CloudPlatformConnector connector = cloudPlatformConnectors.get(stack.cloudPlatform());
        Map<InstanceGroupType, String> userdata = userDataBuilder.buildUserData(stack.cloudPlatform(),
                tlsSecurityService.readPublicSshKey(stack.getId()), connector.getSSHUser());
        return connector.addInstances(stack, userdata.get(InstanceGroupType.GATEWAY),
                userdata.get(InstanceGroupType.CORE), scalingAdjustment, instanceGroupName);
    }

    public void removeInstance(Long stackId, String instanceId) throws Exception {
        Stack stack = stackService.getById(stackId);
        InstanceMetaData instanceMetaData = instanceMetaDataRepository.findByInstanceId(stackId, instanceId);
        String instanceGroupName = instanceMetaData.getInstanceGroup().getGroupName();
        String hostName = instanceMetaData.getDiscoveryFQDN();
        eventService.fireCloudbreakInstanceGroupEvent(stack.getId(), UPDATE_IN_PROGRESS.name(),
                String.format("Start to terminate host '%s' from '%s'.", hostName, instanceGroupName), instanceGroupName);
        if (stack.getCluster() != null) {
            HostMetadata hostMetadata = hostMetadataRepository.findHostsInClusterByName(stack.getCluster().getId(), hostName);
            if (HostMetadataState.HEALTHY.equals(hostMetadata.getHostMetadataState())) {
                throw new ScalingFailedException(String.format("Host (%s) is in HEALTHY state. Cannot be removed.", hostName));
            }
            removeInstance(stack, instanceId, instanceGroupName);
            try {
                ambariClusterConnector.deleteHostFromAmbari(stack, hostMetadata);
                hostMetadataRepository.delete(hostMetadata);
                eventService.fireCloudbreakEvent(stack.getId(), AVAILABLE.name(),
                        String.format("Host '%s' successfully deleted.", instanceMetaData.getInstanceId()));
            } catch (Exception e) {
                LOGGER.error("Host cannot be deleted from cluster: ", e);
                eventService.fireCloudbreakEvent(stack.getId(), DELETE_FAILED.name(),
                        String.format("Could not delete host '%s' from ambari.", instanceMetaData.getInstanceId()));
            }
        } else {
            removeInstance(stack, instanceId, instanceGroupName);
        }
    }

    private void removeInstance(Stack stack, String instanceId, String instanceGroupName) throws CloudbreakSecuritySetupException {
        Set<String> instanceIds = Sets.newHashSet(instanceId);
        instanceIds = cloudPlatformConnectors.get(stack.cloudPlatform()).removeInstances(stack, instanceIds, instanceGroupName);
        updateRemovedResourcesState(stack, instanceIds, stack.getInstanceGroupByInstanceGroupName(instanceGroupName));
    }

    public void downscaleStack(Long stackId, String instanceGroupName, Integer scalingAdjustment) throws Exception {
        Stack stack = stackService.getById(stackId);
        Map<String, String> unregisteredHostNamesByInstanceId = getUnregisteredInstanceIds(instanceGroupName, scalingAdjustment, stack);
        Set<String> instanceIds = new HashSet<>(unregisteredHostNamesByInstanceId.keySet());
        deleteHostsFromAmbari(stack, unregisteredHostNamesByInstanceId);
        instanceIds = cloudPlatformConnectors.get(stack.cloudPlatform()).removeInstances(stack, instanceIds, instanceGroupName);
        updateRemovedResourcesState(stack, instanceIds, stack.getInstanceGroupByInstanceGroupName(instanceGroupName));
    }

    private Map<String, String> getUnregisteredInstanceIds(String instanceGroupName, Integer scalingAdjustment, Stack stack) {
        Map<String, String> instanceIds = new HashMap<>();

        int i = 0;
        for (InstanceMetaData metaData : stack.getInstanceGroupByInstanceGroupName(instanceGroupName).getInstanceMetaData()) {
            if (!metaData.getAmbariServer() && !metaData.getConsulServer() && (metaData.isDecommissioned() || metaData.isUnRegistered())) {
                instanceIds.put(metaData.getInstanceId(), metaData.getDiscoveryFQDN());
                if (++i >= scalingAdjustment * -1) {
                    break;
                }
            }
        }
        return instanceIds;
    }

    private void deleteHostsFromAmbari(Stack stack, Map<String, String> unregisteredHostNamesByInstanceId) throws CloudbreakSecuritySetupException {
        if (stack.getCluster() == null) {
            List<String> hostList = new ArrayList<>(unregisteredHostNamesByInstanceId.values());
            ambariHostsRemover.deleteHosts(stack, hostList, new ArrayList<String>());
        }
    }

    private void updateRemovedResourcesState(Stack stack, Set<String> instanceIds, InstanceGroup instanceGroup) throws CloudbreakSecuritySetupException {
        int nodeCount = instanceGroup.getNodeCount() - instanceIds.size();
        instanceGroup.setNodeCount(nodeCount);
        instanceGroupRepository.save(instanceGroup);

        InstanceGroup gateway = stack.getGatewayInstanceGroup();
        InstanceMetaData gatewayInstance = gateway.getInstanceMetaData().iterator().next();
        TLSClientConfig clientConfig = tlsSecurityService.buildTLSClientConfig(stack.getId(), gatewayInstance.getPublicIp());
        ConsulClient client = ConsulUtils.createClient(clientConfig);

        for (InstanceMetaData instanceMetaData : instanceGroup.getInstanceMetaData()) {
            if (instanceIds.contains(instanceMetaData.getInstanceId())) {
                long timeInMillis = Calendar.getInstance().getTimeInMillis();
                instanceMetaData.setTerminationDate(timeInMillis);
                instanceMetaData.setInstanceStatus(InstanceStatus.TERMINATED);
                removeAgentFromConsul(stack, client, instanceMetaData);
                instanceMetaDataRepository.save(instanceMetaData);
            }
        }
        LOGGER.info("Successfully terminated metadata of instances '{}' in stack.", instanceIds);
        eventService.fireCloudbreakEvent(stack.getId(), BillingStatus.BILLING_CHANGED.name(),
                "Billing changed due to downscaling of cluster infrastructure.");
    }

    private void removeAgentFromConsul(Stack stack, ConsulClient client, InstanceMetaData metaData) {
        String nodeName = metaData.getDiscoveryFQDN().replace(ConsulUtils.CONSUL_DOMAIN, "");
        consulPollingService.pollWithTimeout(
                consulAgentLeaveCheckerTask,
                new ConsulContext(stack, client, Collections.singletonList(nodeName)),
                POLLING_INTERVAL,
                MAX_POLLING_ATTEMPTS);
    }
}