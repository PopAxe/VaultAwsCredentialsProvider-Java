package dev.popaxe.aws.credentials;

import io.github.jopenlibs.vault.Vault;

import org.springframework.vault.core.VaultTemplate;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Map;

/**
 * A wrapper to select credential provider via Vault implementation. See static methods for options
 */
public class VaultAwsCredentialsProvider {

    // Never used, just here to get Jacoco to exclude the class itself from coverage
    // testing
    private VaultAwsCredentialsProvider() {}

    /**
     * If you are using Spring Boot's {@link VaultTemplate VaultTemplate} to interact with Vault,
     * use this method.
     *
     * @param vaultTemplate - An instance of {@link VaultTemplate VaultTemplate} from Spring to
     *     perform Vault operations. May not be null.
     * @param path - The path to where the credentials are. May not be null.
     * @param credentialsRequestData - The data to pass into the request for credentials. May be
     *     null.
     * @return {@link AwsCredentialsProvider AwsCredentialsProvider} - An AWS credentials provider
     *     for usage with the AWS Java SDK V2.
     */
    public static AwsCredentialsProvider withVaultTemplate(
            VaultTemplate vaultTemplate, String path, Map<String, Object> credentialsRequestData) {
        return new SpringVaultTemplateAwsCredentialsProvider(
                vaultTemplate, path, credentialsRequestData);
    }

    /**
     * If you are using JOpenLib's {@link Vault Java Vault Driver} to interact with vault, use this
     * method.
     *
     * @param vault An instance of {@link Vault Vault} from JOpenLibs to perform Vault operations.
     *     May not be null.
     * @param path The path to where the credentials are. May not be null.
     * @param credentialsRequestData The data to pass into the request for credentials. May be null.
     * @return {@link AwsCredentialsProvider AwsCredentialsProvider} An AWS credentials provider for
     *     usage with the AWS Java SDK V2.
     */
    public static AwsCredentialsProvider withJOpenLibsVault(
            Vault vault, String path, Map<String, Object> credentialsRequestData) {
        return new JOpenLibsVaultAwsCredentialsProvider(vault, path, credentialsRequestData);
    }
}
