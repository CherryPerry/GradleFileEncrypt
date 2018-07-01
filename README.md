# File encryption plugin for Gradle

[![Build Status](https://travis-ci.com/CherryPerry/GradleFileEncrypt.svg?branch=master)](https://travis-ci.com/CherryPerry/GradleFileEncrypt)

This application is a plugin for encrypting and decrypting files of gradle project.

1. Create your configuration file with some secure data
1. Add this file to plugin configuration
    ```groovy
    buildscript {
      repositories {
        maven {
          url "https://plugins.gradle.org/m2/"
        }
      }
      dependencies {
        // check latest version on https://plugins.gradle.org/plugin/com.cherryperry.gradle-file-encrypt
        classpath "gradle.plugin.com.cherryperry.gfe:gradle-file-encrypt:1.0.40b2db7"
      }
    }
    apply plugin: 'com.cherryperry.gradle-file-encrypt'
    gradleFileEncrypt {
        files = [file('super_secret_info1.txt'), file('super_secret_info2.properties')]
    }
    ```
    or
    ```groovy
    plugins {
      // check latest version on https://plugins.gradle.org/plugin/com.cherryperry.gradle-file-encrypt
      id "com.cherryperry.gradle-file-encrypt" version "1.0.40b2db7"
    }
    gradleFileEncrypt {
        files = [file('super_secret_info1.txt'), file('super_secret_info2.properties')]
    }
    ```
1. Set password as ```gfe.password``` in ```local.properties``` file in project root
or as ```GFE_PASSWORD``` environment variable
1. Add your configuration file to ```.gitignore```
1. Use ```encryptFiles``` task to encrypt files
1. Push your project to unsecure repository
1. Decrypt files back with ```decryptFiles``` task (on CI or on your development PC)