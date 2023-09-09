package dev.popaxe.aws.credentials;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.json.JsonObject;
import io.github.jopenlibs.vault.response.LogicalResponse;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Map;
import java.util.Objects;

class JOpenLibsVaultAwsCredentialsProvider extends AbstractVaultAwsCredentialsProvider {

    private final Vault datasource;

    private final String path;
    private final Map<String, Object> credsRequestData;

    /**
     * Returns an implementation of an {@link AwsCredentialsProvider AwsCredentialsProvider} that
     * will utilize Hashicorp Vault for fetching of credentials. This class, once instantiated, can
     * be passed to any AWS SDK V2 client to handle credentials. See <a href=
     * "https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-specify.html">here</a>
     * for an example on how to pass this to a client.
     *
     * @param vault - An instance of {@link Vault Vault}. May not be null.
     * @param path - The path to where to call Vault to obtain credentials. May not be null.
     * @param credsRequestData - The data to pass to Vault when obtaining credentials. May be null.
     */
    public JOpenLibsVaultAwsCredentialsProvider(
            Vault vault, String path, Map<String, Object> credsRequestData) {
        super(path);
        this.datasource = Objects.requireNonNull(vault);
        this.path = Objects.requireNonNull(path);
        this.credsRequestData = credsRequestData;
    }

    JsonObject getDataFromVault() {
        try {
            // If there is request data, we know they're calling the sts endpoint,
            // so we can do implicit conversion on the map
            JsonObject data = new JsonObject();
            LogicalResponse response;
            if (credsRequestData != null) {
                response = datasource.logical().write(path, credsRequestData);
            } else {
                response = datasource.logical().read(path);
            }

            for (Map.Entry<String, String> entry : response.getData().entrySet()) {
                data.add(entry.getKey(), entry.getValue());
            }

            return data;
        } catch (VaultException exception) {
            return null;
        }
    }
}
