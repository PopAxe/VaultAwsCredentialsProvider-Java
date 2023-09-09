package dev.popaxe.aws.credentials;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.json.JsonObject;

import org.springframework.vault.core.VaultTemplate;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

abstract class AbstractVaultAwsCredentialsProvider implements AwsCredentialsProvider {

    private static final int MINIMUM_DURATION_SECONDS = 900;

    private AwsCredentials awsCredentials;

    private final String path;

    private Instant expiresAt;

    private boolean hasCached;

    public AbstractVaultAwsCredentialsProvider(String path) {
        this.path = Objects.requireNonNull(path);
        this.expiresAt = Instant.now().plus(MINIMUM_DURATION_SECONDS, ChronoUnit.SECONDS);
    }

    private AwsCredentials getCredentials() {
        // As per
        // https://developer.hashicorp.com/vault/api-docs/secret/aws#get-static-credentials,
        // the URI for static credentials contains that path
        if (path.contains("static-creds")) {
            return getStaticCredentials();
        } else {
            return getRoleCredentials();
        }
    }

    // https://developer.hashicorp.com/vault/api-docs/secret/aws#generate-credentials
    private AwsCredentials getRoleCredentials() {
        if (Instant.now().isBefore(expiresAt) && hasCached) {
            return awsCredentials;
        }

        JsonObject dataFromVault = getDataFromVault();
        if (dataFromVault == null) {
            return null;
        }

        // This gives us a 15-second buffer between when we re-fetch and when it
        // expires,
        // allowing us a bit of safety from usage of expired credentials
        expiresAt = Instant.now().plus(MINIMUM_DURATION_SECONDS - 15, ChronoUnit.SECONDS);
        hasCached = true;

        awsCredentials =
                AwsSessionCredentials.builder()
                        .accessKeyId(dataFromVault.getString("access_key"))
                        .secretAccessKey(dataFromVault.getString("secret_key"))
                        .sessionToken(dataFromVault.getString("security_token"))
                        .build();

        return awsCredentials;
    }

    // https://developer.hashicorp.com/vault/api-docs/secret/aws#get-static-credentials
    private AwsCredentials getStaticCredentials() {
        if (awsCredentials != null) {
            return awsCredentials;
        }

        JsonObject dataFromVault = getDataFromVault();
        if (dataFromVault == null) {
            return null;
        }

        awsCredentials =
                AwsBasicCredentials.create(
                        dataFromVault.getString("access_key"),
                        dataFromVault.getString("secret_key"));

        return awsCredentials;
    }

    /**
     * Use your implementation object (such as {@link Vault} or {@link VaultTemplate}) to return a
     * JsonObject for processing and returning during credential resolution.
     *
     * @return {@link JsonObject response from Vault}. Must match the schema found in <a href=
     *     "https://developer.hashicorp.com/vault/api-docs/secret/aws#generate-credentials">here</a>,
     *     or <a href=
     *     "https://developer.hashicorp.com/vault/api-docs/secret/aws#get-static-credentials">here</a>.
     */
    abstract JsonObject getDataFromVault();

    @Override
    public AwsCredentials resolveCredentials() {
        return this.getCredentials();
    }
}
