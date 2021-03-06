package com.sequenceiq.it.cloudbreak.mock;

import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.model.NetworkJson;
import com.sequenceiq.it.cloudbreak.AbstractCloudbreakIntegrationTest;
import com.sequenceiq.it.cloudbreak.CloudbreakITContextConstants;

public class MockNetworkCreationTest extends AbstractCloudbreakIntegrationTest {
    @Test
    @Parameters({ "networkName", "subnetCIDR" })
    public void testGcpTemplateCreation(@Optional("it-mock-network") String networkName, @Optional("10.0.36.0/24") String subnetCIDR)
            throws Exception {
        // GIVEN
        // WHEN
        // TODO: publicInAccount
        NetworkJson networkJson = new NetworkJson();
        networkJson.setDescription("Mock network for integration testing");
        networkJson.setName(networkName);
        networkJson.setSubnetCIDR(subnetCIDR);
        networkJson.setCloudPlatform("MOCK");

        String id = getCloudbreakClient().networkEndpoint().postPrivate(networkJson).getId().toString();
        // THEN
        Assert.assertNotNull(id);
        getItContext().putContextParam(CloudbreakITContextConstants.NETWORK_ID, id, true);
    }
}
