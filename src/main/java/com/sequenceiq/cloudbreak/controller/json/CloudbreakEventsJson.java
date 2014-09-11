package com.sequenceiq.cloudbreak.controller.json;

public class CloudbreakEventsJson implements JsonEntity {

    private String eventType;
    private long eventTimestamp;
    private String eventMessage;
    private String userName;
    private long userId;
    private String accountName;
    private long accountId;
    private String cloud;
    private String region;
    private String vmType;
    private String blueprintName;
    private long blueprintId;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public String getEventMessage() {
        return eventMessage;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getVmType() {
        return vmType;
    }

    public void setVmType(String vmType) {
        this.vmType = vmType;
    }

    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public long getBlueprintId() {
        return blueprintId;
    }

    public void setBlueprintId(long blueprintId) {
        this.blueprintId = blueprintId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CloudbreakEventsJson{");
        sb.append("eventType='").append(eventType).append('\'');
        sb.append(", eventTimestamp=").append(eventTimestamp);
        sb.append(", eventMessage='").append(eventMessage).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", userId=").append(userId);
        sb.append(", accountName='").append(accountName).append('\'');
        sb.append(", accountId=").append(accountId);
        sb.append(", cloud='").append(cloud).append('\'');
        sb.append(", region='").append(region).append('\'');
        sb.append(", vmType='").append(vmType).append('\'');
        sb.append(", blueprintName='").append(blueprintName).append('\'');
        sb.append(", blueprintId=").append(blueprintId);
        sb.append('}');
        return sb.toString();
    }
}