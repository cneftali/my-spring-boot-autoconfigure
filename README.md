# Spring Boot Autoconfigure

[![Build Status](https://api.travis-ci.org/cneftali/spring-boot-autoconfigure.svg?branch=master)](https://github.com/cneftali/spring-boot-autoconfigure)
[![Coverage Status](https://coveralls.io/repos/github/cneftali/spring-boot-autoconfigure/badge.svg?branch=master)](https://coveralls.io/github/cneftali/spring-boot-autoconfigure?branch=master)

## Requirements

1. [Oracle Sun JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. [Maven 3.2.5 or more](https://maven.apache.org/)
3. [Lombok](https://projectlombok.org/download.html)

## Building from Source

```bash
$ git clone git@github.com:cneftali/spring-boot-autoconfigure.git
$ cd spring-boot-autoconfigure
$ mvn clean package
```

## Added Autoconfigure:

- [Java SMTP Server](https://github.com/voodoodyne/subethasmtp)
- [InWebo](https://www.myinwebo.com/)

## Make Release :

```bash
$ mvn --batch-mode -Prelease,make-release clean -Dresume=false build-helper:parse-version release:prepare release:perform -DdevelopmentVersion=1.2.0-SNAPSHOT
$ git push --all && git push --tags
```
## License

This project is licensed under the [Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0), which allows you to include modified versions of the code in your distributed software, without having to release your source code.
