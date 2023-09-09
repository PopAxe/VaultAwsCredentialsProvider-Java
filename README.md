![](https://github.com/PopAxe/VaultAwsCredentialsProvider-Java/actions/workflows/build.yml/badge.svg) ![](https://img.shields.io/codecov/c/github/PopAxe/VaultAwsCredentialsProvider-Java
)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.libktx/ktx-module.svg)](https://search.maven.org/artifact/io.github.libktx/ktx-module)

![](https://img.shields.io/github/license/PopAxe/VaultAwsCredentialsProvider-Java)

## Description
VaultAwsCredentialsProvider is a package that allows you to pass refreshable or static credentials to the AWS v2 SDK.

We currently only support the following classes and libraries:
* [VaultTemplate](https://github.com/spring-projects/spring-vault)
* [Vault](https://github.com/BetterCloud/vault-java-driver)

If you would like support for another library, please submit a PR or open an issue [here](https://github.com/PopAxe/VaultAwsCredentialsProvider-Java/issues/new?assignees=&labels=enhancement%2C+library+support+request&projects=&template=add-support-for-vault-library.md&title=) 

## Usage
1. Add the package to your dependencies. See [here](https://central.sonatype.com/artifact/dev.popaxe.aws.credentials/vault-aws-credentials-provider) for instructions specific to your build system.

### Usage Notes
The argument `credentialsRequestData` may be null if you do not need to pass a request body. If you are unsure if this is needed,
please refer to the [Vault HTTP API documentation](https://developer.hashicorp.com/vault/api-docs/secret/aws#generate-credentials).

### Spring Vault
1. Have an available `VaultTemplate` object from Spring Vault
1. Create the credentials provider using the example code below:

```java
import dev.popaxe.aws.credentials.VaultAwsCredentialsProvider;
import org.springframework.vault.core.VaultTemplate;

import java.util.Map;

class ExampleClass {

    @Autowired
    private final VaultTemplate vaultTemplate;

    public static void main(String[] args) {
        VaultAwsCredentialsProvider credentialsProvider =
            VaultAwsCredentialsProvider.withVaultTemplate(
                vaultTemplate,
                "/path/to/creds", // The path to credentials in Vault
                Map.of("key1", "value") // Map of request data to pass to request, if applicable
            );
    }
}
```

### JOpenLibs Vault
1. Have an available `VaultTemplate` object from Spring Vault
1. Create the credentials provider using the example code below:

```java
import dev.popaxe.aws.credentials.VaultAwsCredentialsProvider;
import io.github.jopenlibs.vault.Vault;

import java.util.Map;

class ExampleClass {

    @Autowired
    private final Vault vault;

    public static void main(String[] args) {
        VaultAwsCredentialsProvider credentialsProvider =
            VaultAwsCredentialsProvider.withJOpenLibsVault(
                vault,
                "/path/to/creds", // The path to credentials in Vault
                Map.of("key1", "value") // Map of request data to pass to request, if applicable
            );
    }
}
```

## Development
This library uses a number of things to standardize development across developers:
1. Spotless - This is used to ensure consistent, readable code formatting. We use Google's AOSP settings.
1. Spotbugs - This is used to ensure that there are no obvious bugs in the code.
1. Jacoco - We use this to make sure that code coverage in our tests remains at 95% or greater.

These packages help development and ensure proper build procedure before a new version is released.

### Publishing
A new version of this library is only pushed when a new release is created. Only the build automation system
should be used for publishing a new version, however if a manual publish must be done, use the following instructions:

#### MacOS
1. Create a GPG key (the easiest way to do this is [with this tool](https://gpgtools.org/))
1. Export this to a file: `gpg --export-secret-keys > $HOME/secring.gpg`
1. Create a `gradle.properties` file in your `$HOME/.gradle` folder with the following contents:
```properties
signing.keyId=<YOUR_KEY_ID_HERE>
signing.password=<YOUR_KEY_PASSWORD_HERE>
signing.secretKeyRingFile=$HOME/secring.gpg
```
1. Set your credential variables:
```shell
export MAVEN_USERNAME=<USERNAME_HERE>
export MAVEN_PASSWORD=<PASSWORD_HERE>
```
1. Ensure you've bumped the version in `build.gradle` to a non-conflicting version
1. Run `gradle publishToSonatype closeAndReleaseSonatypeStagingRepository`
