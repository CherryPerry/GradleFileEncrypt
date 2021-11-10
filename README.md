## File encryption plugin for Gradle

[![Build Status](https://github.com/CherryPerry/GradleFileEncrypt/actions/workflows/build.yml/badge.svg)](https://github.com/CherryPerry/GradleFileEncrypt/actions/workflows/build.yml)
[![Version](https://img.shields.io/github/release/CherryPerry/GradleFileEncrypt.svg)](https://github.com/CherryPerry/GradleFileEncrypt/releases)

Simply encrypt files of gradle project with password.
Plugin will create encrypted copy of file with `.encrypted` extension. For example: `secret_keys.properties` -> `secret_keys.properties.encrypted`. Add `secret_keys.properties` to `.gitignore` and add `secret_keys.properties.encrypted` to vcs.

### Add it to your project

#### Gradle plugin DSL

```groovy
plugins {
    id 'com.cherryperry.gradle-file-encrypt' version '1.4.0'
}
```

#### Old Gradle version or where dynamic configuration is required

```groovy
buildscript {
    repositories {
        maven { url 'https://plugins.gradle.org/m2/' }
    }
    dependencies {
        classpath 'gradle.plugin.com.cherryperry.gfe:gradle-file-encrypt:1.4.0'
    }
}

apply plugin: 'com.cherryperry.gradle-file-encrypt'
```

### Password setup

Set password `gfe.password` in `local.properties` file in project root
or `GFE_PASSWORD` environment variable.

You can create your own password provider via `passwordProvider`.

### Configuration

```groovy
gradleFileEncrypt {
    // files to encrypt
    files 'signing.properties', 'app/google-services.json'
    // (optional) setup file mapping
    mapping = [ 'signing.properties' : 'secret/signing.properties' ]
    // (optional) setup password provider
    // if provided one is not secure enough for you
    passwordProvider { return 'YOUR LOGIC HERE'.toCharArray() }
}
```

#### File mapping
Sometimes you need to save your encrypted files in another directory. 
You can configure that behavior with `mapping` configuration. 
It is simple `Map<Object, Object>`, where key is original file
and value is target file without encrypted extension.
```groovy
gradleFileEncrypt {
    files 'src/main/resources/secure.properties'
    mapping = ['src/main/resources/secure.properties':'secure/keys']
}
```
Encrypted file `secure.properties.encrypted` will be bundled with app without `mapping`,
because it is inside resources folder. To avoid this behavior `mapping` was provided,
so `secure/keys.encrypted` file will be encrypted version of `src/main/resources/secure.properties`.

### Encryption and decryption

You **must** setup password before invoking this tasks.

Create encrypted files from plain files:
```bash
./gradlew encryptFiles
```

Create plain files from encrypted files (if files already exist, they will be **overwritten**):
```bash
./gradlew decryptFiles
```

### Git ignore check

You can check, if your plain unencrypted files were ignored by your ```.gitignore``` files in project,
so they won't appear in version control history.
```bash
./gradlew checkFilesGitIgnored
```
If any is not ignored, task will fail and print which file is not ignored.

### Gradle

Minimal recommended gradle version is 4.0.2.
Check supported versions [here](https://github.com/CherryPerry/GradleFileEncrypt/blob/master/src/test/kotlin/com/cherryperry/gfe/FileEncryptPluginFunctionalTest.kt#L34).

### Samples

You can also see sample usage in my other projects:

1. [CherryPerry/Amiami-kotlin-backend](https://github.com/CherryPerry/Amiami-kotlin-backend)
1. [CherryPerry/Amiami-android-app](https://github.com/CherryPerry/Amiami-android-app)

Both projects are connected to Travis continuous integration service. Encryption password was set in
[settings tab](https://docs.travis-ci.com/user/environment-variables/#defining-variables-in-repository-settings)
of each repository. ```./gradlew decryptFiles``` command was added to pre-build script, so all files,
that contains private settings required for build, are decrypted before build. Not encrypted files were added
to ```.gitignore```, so there is no decrypted versions of them in repository, only encrypted ones.
For local development I add password to ```local.properties``` file.
