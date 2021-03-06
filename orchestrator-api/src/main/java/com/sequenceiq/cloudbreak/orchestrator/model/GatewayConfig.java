package com.sequenceiq.cloudbreak.orchestrator.model;

public class GatewayConfig {

    private final String publicAddress;
    private final String privateAddress;
    private final String hostname;
    private final String certificateDir;
    private final String serverCert;
    private final String clientCert;
    private final String clientKey;
    private Integer gatewayPort;

    public GatewayConfig(String publicAddress, String privateAddress, Integer gatewayPort, String certificateDir) {
        this(publicAddress, privateAddress, null, gatewayPort, certificateDir, null, null, null);
    }

    public GatewayConfig(String publicAddress, String privateAddress, String hostname,
            Integer gatewayPort, String certificateDir, String serverCert, String clientCert, String clientKey) {
        this.publicAddress = publicAddress;
        this.privateAddress = privateAddress;
        this.hostname = hostname;
        this.certificateDir = certificateDir;
        this.gatewayPort = gatewayPort;
        this.serverCert = serverCert;
        this.clientCert = clientCert;
        this.clientKey = clientKey;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public String getPrivateAddress() {
        return privateAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public String getCertificateDir() {
        return certificateDir;
    }

    public Integer getGatewayPort() {
        return gatewayPort;
    }

    public String getGatewayUrl() {
        return String.format("https://%s:%d", publicAddress, gatewayPort);
    }

    public String getServerCert() {
        return serverCert;
    }

    public String getClientCert() {
        return clientCert;
    }

    public String getClientKey() {
        return clientKey;
    }

    @Override
    public String toString() {
        return "GatewayConfig{"
                + "publicAddress='" + publicAddress + '\''
                + ", privateAddress='" + privateAddress + '\''
                + ", certificateDir='" + certificateDir + '\''
                + ", serverCert='" + serverCert + '\''
                + ", clientCert='" + clientCert + '\''
                + ", clientKey='" + clientKey + '\''
                + '}';
    }
}
