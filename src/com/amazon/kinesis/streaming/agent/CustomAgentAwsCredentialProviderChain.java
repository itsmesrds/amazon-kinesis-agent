package com.amazon.kinesis.streaming.agent;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAgentAwsCredentialProviderChain {
    @Getter
    private final AWSCredentials cred;

    @Getter
    private final AWSCredentialsProvider provider;
    public CustomAgentAwsCredentialProviderChain() {
        provider = new AWSCredentialsProviderChain(
            WebIdentityTokenCredentialsProvider.create(),
            new EnvironmentVariableCredentialsProvider(),
            new SystemPropertiesCredentialsProvider(),
            new ProfileCredentialsProvider()
        );

        cred = provider.getCredentials();
        try {
            debugLog();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }

        if (isValid()) {
            log.info("Credential is OK. At least one match found in CredentialProviders.");
        } else {
            log.error("Credential is empty. Upstream needs to throw error");
        }
    }

    public void debugLog() {
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClient.builder().build();
        GetCallerIdentityResult identity = sts.getCallerIdentity(new GetCallerIdentityRequest());
        log.warn("Account={} ; ARN={} ; UserId={}", identity.getAccount(), identity.getArn(), identity.getUserId());
    }

    public boolean isValid() {
        return !cred.getAWSAccessKeyId().isEmpty() && !cred.getAWSSecretKey().isEmpty();
    }
}
