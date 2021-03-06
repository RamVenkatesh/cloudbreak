package com.sequenceiq.cloudbreak.service.credential;

import java.security.PublicKey;

import com.sequenceiq.cloudbreak.controller.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenSshPublicKeyValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSshPublicKeyValidator.class);

    public void validate(String publicKey) {
        try {
            PublicKey load = PublicKeyReaderUtil.loadOpenSsh(publicKey);
        } catch (Exception e) {
            String errorMessage = String.format("Could not validate publickey certificate [certificate: '%s'], detailed message: %s",
                    publicKey, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new BadRequestException(errorMessage, e);
        }
    }
}
