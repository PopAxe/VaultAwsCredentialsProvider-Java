package dev.popaxe.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import dev.popaxe.aws.credentials.VaultAwsCredentialsProvider;

import org.junit.jupiter.api.Test;
import org.springframework.vault.core.VaultTemplate;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

public class TestVaultAwsCredentialsProvider {

    @Test
    public void testInitializeSpringVaultCredsProvider() {
        VaultTemplate vaultTemplate = mock(VaultTemplate.class);
        AwsCredentialsProvider credentialsProvider =
                VaultAwsCredentialsProvider.withVaultTemplate(vaultTemplate, "/path", null);

        assertThrows(
                NullPointerException.class,
                () -> {
                    VaultAwsCredentialsProvider.withVaultTemplate(null, null, null);
                });

        assertNotNull(credentialsProvider);
    }
}
