package dev.popaxe.aws.credentials;

import io.github.jopenlibs.vault.json.JsonObject;

import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Map;
import java.util.Objects;

class SpringVaultTemplateAwsCredentialsProvider extends AbstractVaultAwsCredentialsProvider {

    private final VaultTemplate datasource;

    private final String path;
    private final Map<String, Object> credsRequestData;

    /**
     * Returns an implementation of an {@link AwsCredentialsProvider AwsCredentialsProvider} that
     * will utilize Hashicorp Vault for fetching of credentials. This class, once instantiated, can
     * be passed to any AWS SDK V2 client to handle credentials. See <a href=
     * "https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-specify.html">here</a>
     * for an example on how to pass this to a client.
     *
     * @param vaultTemplate - The {@link VaultTemplate VaultTemplate} that will be used to interact
     *     with Vault. May not be null.
     * @param path - The path to where to call Vault to obtain credentials. May not be null.
     * @param credsRequestData - The data to pass to Vault when obtaining credentials. May be null.
     */
    public SpringVaultTemplateAwsCredentialsProvider(
            VaultTemplate vaultTemplate, String path, Map<String, Object> credsRequestData) {
        super(path);
        this.datasource = Objects.requireNonNull(vaultTemplate);
        this.path = Objects.requireNonNull(path);
        this.credsRequestData = credsRequestData;
    }

    /**
     * @return {@link JsonObject response from Vault}. May be null.
     */
    JsonObject getDataFromVault() {
        VaultResponse response;
        if (credsRequestData != null) {
            response = datasource.write(path, credsRequestData);
        } else {
            response = datasource.read(path);
        }

        if (response == null) {
            return null;
        }

        Map<String, Object> responseData = response.getData();

        if (responseData == null) {
            return null;
        }

        JsonObject data = new JsonObject();
        responseData.forEach((key, value) -> data.add(key, value.toString()));

        return data;
    }
}
