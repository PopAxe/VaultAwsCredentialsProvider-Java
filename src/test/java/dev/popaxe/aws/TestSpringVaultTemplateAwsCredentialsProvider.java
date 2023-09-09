package dev.popaxe.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.popaxe.aws.credentials.VaultAwsCredentialsProvider;

import io.github.jopenlibs.vault.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.util.Map;

public class TestSpringVaultTemplateAwsCredentialsProvider {

    private VaultTemplate vaultTemplate;
    private AwsCredentialsProvider springVaultCredentialsProvider;

    private AwsCredentialsProvider springVaultCredentialsProviderWithBody;

    @BeforeEach
    public void beforeEach() {
        vaultTemplate = mock(VaultTemplate.class);

        springVaultCredentialsProvider =
                VaultAwsCredentialsProvider.withVaultTemplate(vaultTemplate, "/path", null);

        springVaultCredentialsProviderWithBody =
                VaultAwsCredentialsProvider.withVaultTemplate(
                        vaultTemplate, "/path", Map.of("key1", "value1"));
    }

    @Test
    public void testResolveRoleCredentials() throws VaultException, JsonProcessingException {
        JsonObject jsonCredentials = new JsonObject();
        jsonCredentials.add("access_key", "akey");
        jsonCredentials.add("secret_key", "skey");
        jsonCredentials.add("security_token", "token");

        VaultResponse response = mock(VaultResponse.class);
        when(vaultTemplate.read(anyString())).thenReturn(response);

        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(jsonCredentials.toString(), Map.class));

        AwsSessionCredentials credentials =
                (AwsSessionCredentials) springVaultCredentialsProvider.resolveCredentials();
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

        VaultResponse response = mock(VaultResponse.class);
        when(vaultTemplate.write(anyString(), any())).thenReturn(response);

        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(jsonCredentials.toString(), Map.class));

        AwsSessionCredentials credentials =
                (AwsSessionCredentials) springVaultCredentialsProviderWithBody.resolveCredentials();
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());
        assertEquals("token", credentials.sessionToken());
    }

    @Test
    public void testResolveCredentials_Cached() throws VaultException, JsonProcessingException {
        JsonObject jsonCredentials = new JsonObject();
        jsonCredentials.add("access_key", "akey");
        jsonCredentials.add("secret_key", "skey");
        jsonCredentials.add("security_token", "token");

        VaultResponse response = mock(VaultResponse.class);
        when(vaultTemplate.read(anyString())).thenReturn(response);

        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(jsonCredentials.toString(), Map.class));

        AwsSessionCredentials credentials =
                (AwsSessionCredentials) springVaultCredentialsProvider.resolveCredentials();
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());
        assertEquals("token", credentials.sessionToken());

        AwsSessionCredentials newCredentials =
                (AwsSessionCredentials) springVaultCredentialsProvider.resolveCredentials();
        assertEquals(credentials, newCredentials);
    }

    @Test
    public void testResolveRoleCredentials_Null() throws VaultException {
        when(vaultTemplate.read(anyString())).thenReturn(null);

        AwsCredentials credentials = springVaultCredentialsProvider.resolveCredentials();
        assertNull(credentials);
    }

    @Test
    public void testResolveRoleCredentials_NullData() throws VaultException {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(null);
        when(vaultTemplate.read(anyString())).thenReturn(response);

        AwsCredentials credentials = springVaultCredentialsProvider.resolveCredentials();
        assertNull(credentials);
    }

    @Test
    public void testResolveStaticCredentialsNoBody()
            throws VaultException, JsonProcessingException {
        JsonObject data = new JsonObject();
        data.add("access_key", "akey");
        data.add("secret_key", "skey");

        springVaultCredentialsProvider =
                VaultAwsCredentialsProvider.withVaultTemplate(vaultTemplate, "/static-creds", null);

        VaultResponse response = mock(VaultResponse.class);
        when(vaultTemplate.read(anyString())).thenReturn(response);

        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(data.toString(), Map.class));

        AwsCredentials credentials = springVaultCredentialsProvider.resolveCredentials();
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());
    }

    @Test
    public void testResolveStaticCredentials() throws VaultException, JsonProcessingException {
        JsonObject data = new JsonObject();
        data.add("access_key", "akey");
        data.add("secret_key", "skey");

        springVaultCredentialsProviderWithBody =
                VaultAwsCredentialsProvider.withVaultTemplate(
                        vaultTemplate, "/static-creds", Map.of("key1", "value1"));

        VaultResponse response = mock(VaultResponse.class);
        when(vaultTemplate.write(anyString(), any(Map.class))).thenReturn(response);

        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(data.toString(), Map.class));

        AwsCredentials credentials = springVaultCredentialsProviderWithBody.resolveCredentials();
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());
    }

    @Test
    public void testResolveStaticCredentials_Null() throws VaultException, JsonProcessingException {
        when(vaultTemplate.read(anyString())).thenReturn(null);

        AwsCredentials credentials = springVaultCredentialsProvider.resolveCredentials();
        assertNull(credentials);
    }

    @Test
    public void testResolveStaticCredentials_NullData()
            throws VaultException, JsonProcessingException {
        VaultResponse response = mock(VaultResponse.class);
        when(response.getData()).thenReturn(null);
        when(vaultTemplate.read(anyString())).thenReturn(response);

        springVaultCredentialsProvider =
                VaultAwsCredentialsProvider.withVaultTemplate(vaultTemplate, "/static-creds", null);

        AwsCredentials credentials = springVaultCredentialsProvider.resolveCredentials();
        assertNull(credentials);
    }

    @Test
    public void testResolveStaticCredentials_Cached()
            throws VaultException, JsonProcessingException {
        JsonObject data = new JsonObject();
        data.add("access_key", "akey");
        data.add("secret_key", "skey");

        springVaultCredentialsProviderWithBody =
                VaultAwsCredentialsProvider.withVaultTemplate(
                        vaultTemplate, "/static-creds", Map.of("key1", "value1"));

        VaultResponse response = mock(VaultResponse.class);
        when(vaultTemplate.write(anyString(), any(Map.class))).thenReturn(response);

        when(response.getData())
                .thenReturn(new ObjectMapper().readValue(data.toString(), Map.class));

        AwsCredentials credentials = springVaultCredentialsProviderWithBody.resolveCredentials();
        assertEquals("akey", credentials.accessKeyId());
        assertEquals("skey", credentials.secretAccessKey());

        AwsCredentials newCredentials = springVaultCredentialsProviderWithBody.resolveCredentials();
        assertEquals(credentials, newCredentials);
    }
}
