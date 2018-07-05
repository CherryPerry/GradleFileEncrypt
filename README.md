# File encryption plugin for Gradle

[![Build Status](https://travis-ci.com/CherryPerry/GradleFileEncrypt.svg?branch=master)](https://travis-ci.com/CherryPerry/GradleFileEncrypt)
[![Version](https://img.shields.io/github/release/CherryPerry/GradleFileEncrypt.svg)](https://github.com/CherryPerry/GradleFileEncrypt/releases)

This application is a plugin for encrypting and decrypting files of gradle project.

1. Create your configuration file with some secure data
1. Set password as `gfe.password` in `local.properties` file in project root
or as `GFE_PASSWORD` environment variable (or skip this step)
1. Add this file to plugin configuration
    ```groovy
    buildscript {
      repositories {
        maven { url 'https://plugins.gradle.org/m2/' }
      }
      dependencies {
        // check latest version on https://plugins.gradle.org/plugin/com.cherryperry.gradle-file-encrypt
        classpath 'gradle.plugin.com.cherryperry.gfe:gradle-file-encrypt:1.1.0'
      }
    }
    apply plugin: 'com.cherryperry.gradle-file-encrypt'
    gradleFileEncrypt {
        files 'signing.properties', 'app/google-services.json'
        // optional setup password provider if previous step was't suitable
        passwordProvider { return 'YOUR LOGIC HERE'.toCharArray() }
    }
    ```
    or
    ```groovy
    plugins {
      // check latest version on https://plugins.gradle.org/plugin/com.cherryperry.gradle-file-encrypt
      id 'com.cherryperry.gradle-file-encrypt' version '1.1.0'
    }
    gradleFileEncrypt {
        files 'signing.properties', 'app/google-services.json'
        // optional setup password provider if previous step was't suitable
        passwordProvider { return 'YOUR LOGIC HERE'.toCharArray() }
    }
    ```
1. Add your configuration file to `.gitignore`
1. Use `encryptFiles` task to encrypt files
1. Add encrypted versions of files (`signing.properties.encrypted`) to your version control
1. Push your project to unsecure repository
1. Decrypt files back with `decryptFiles` task (on CI or on your development PC, don't forget to setup password)