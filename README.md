# File encryption plugin for Gradle

[![Build Status](https://travis-ci.com/CherryPerry/GradleFileSimpleEncrypt.svg?branch=master)](https://travis-ci.com/CherryPerry/GradleFileSimpleEncrypt)

This application is a plugin for encrypting and decrypting files of gradle project.

1. Create your configuration file with some secure data
1. Add this file to plugin configuration
1. Encrypt it with password and salt
1. Add your configuration file to ```.gitignore```
1. Push your project to unsecure repository
1. Add password and salt to your CI
1. Before build plugin will decrypt all encrypted files

## Encryption modes

You can set your password via following methods within priority.
1. ```local.properties``` gradle file
2. Environment variables
3. Console input