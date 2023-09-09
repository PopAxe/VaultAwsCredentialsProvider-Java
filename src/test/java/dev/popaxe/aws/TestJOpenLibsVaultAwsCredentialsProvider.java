package dev.popaxe.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.popaxe.aws.credentials.VaultAwsCredentialsProvider;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.api.Logical;
import io.github.jopenlibs.vault.json.JsonObject;
import io.github.jopenlibs.vault.response.LogicalResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.util.Map;

public class TestJOpenLibsVaultAwsCredentialsProvider {

    private Vault vault;

    private AwsCredentialsProvider vaultCredentialsProvider;

    private AwsCredentialsProvider vaultAwsCredentialsProviderWithBody;

    @BeforeEach
    public void beforeEach() {
        vault = mock(Vault.class);
        when(vault.logical()).thenReturn(mock(Logical.class));

        VaultConfig config = mock(VaultConfig.class);
        when(config.getGlobalEngineVersion()).thenReturn(2);

        vaultCredentialsProvider =
                VaultAwsCredentialsProvider.withJOpenLibsVault(vault, "/path", null);

        vaultAwsCredentialsProviderWithBody =
                VaultAwsCredentialsProvider.withJOpenLibsVault(
                        vault, "/path", Map.of("key1", "value1"));
    }

    @Test
    public void testResolveRoleCredentials() throws VaultException, JsonProcessingException {
        JsonObject jsonCredentials = new JsonObject();
        jsonCredentials.add("access_key", "akey");
        jsonCredentials.add("secret_key", "skey");
        jsonCredentials.add("security_token", "token");

        LogicalResponse response = mock(LogicalResponse.class);
        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(jsonCredentials.toString(), Map.class));

        when(vault.logical().read(anyString())).thenReturn(response);

        AwsSessionCredentials credentials =
                (AwsSessionCredentials) vaultCredentialsProvider.resolveCredentials();
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());
        assertEquals("token", credentials.sessionToken());
    }

    @Test
    public void testResolveRoleCredentialsWithBody()
            throws VaultException, JsonProcessingException {
        JsonObject jsonCredentials = new JsonObject();
        jsonCredentials.add("access_key", "akey");
        jsonCredentials.add("secret_key", "skey");
        jsonCredentials.add("security_token", "token");

        LogicalResponse response = mock(LogicalResponse.class);
        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(jsonCredentials.toString(), Map.class));

        when(vault.logical().write(anyString(), anyMap())).thenReturn((response));

        AwsSessionCredentials credentials =
                (AwsSessionCredentials) vaultAwsCredentialsProviderWithBody.resolveCredentials();
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());
        assertEquals("token", credentials.sessionToken());
    }

    @Test
    public void testResolveRoleCredentials_VaultException() throws VaultException {

        when(vault.logical().read(anyString())).thenThrow(VaultException.class);

        AwsSessionCredentials credentials =
                (AwsSessionCredentials) vaultCredentialsProvider.resolveCredentials();
        assertNull(credentials);
    }

    @Test
    public void testResolveStaticCredentials() throws VaultException, JsonProcessingException {
        JsonObject data = new JsonObject();
        data.add("access_key", "akey");
        data.add("secret_key", "skey");

        vaultCredentialsProvider =
                VaultAwsCredentialsProvider.withJOpenLibsVault(vault, "/static-creds", null);

        LogicalResponse response = mock(LogicalResponse.class);
        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(data.toString(), Map.class));

        when(vault.logical().read(anyString())).thenReturn((response));

        AwsBasicCredentials credentials =
                (AwsBasicCredentials) vaultCredentialsProvider.resolveCredentials();
        assertFalse(credentials.toString().contains("security_token"));
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());
    }
}
