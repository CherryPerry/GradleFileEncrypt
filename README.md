## File encryption plugin for Gradle

[![Build Status](https://travis-ci.com/CherryPerry/GradleFileEncrypt.svg?branch=master)](https://travis-ci.com/CherryPerry/GradleFileEncrypt)
[![Version](https://img.shields.io/github/release/CherryPerry/GradleFileEncrypt.svg)](https://github.com/CherryPerry/GradleFileEncrypt/releases)

Simply encrypt files of gradle project with password.
Plugin will create encrypted copy of file with `.encrypted` extension. For exampale: `secret_keys.properties` -> `secret_keys.properties.encrypted`. Add `secret_keys.properties` to `.gitignore` and add `secret_keys.properties.encrypted` to vcs.

### Add it to your project

#### Gradle plugin DSL

```groovy
plugins {
    id 'com.cherryperry.gradle-file-encrypt' version '1.1.0'
}
```

#### Old Gradle version or where dynamic configuration is required

```groovy
buildscript {
    repositories {
        maven { url 'https://plugins.gradle.org/m2/' }
    }
    dependencies {
        classpath 'gradle.plugin.com.cherryperry.gfe:gradle-file-encrypt:1.1.0'
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
    // (optional) setup password provider
    // if provided one is not secure enough for you
    passwordProvider { return 'YOUR LOGIC HERE'.toCharArray() }
}
```

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
