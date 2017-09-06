# Changelog

## 1.0.0-BETA-7 (2017-09-07)
* Fix maven build warning about missing version of maven-source-plugin
* Customized javadoc style
* Deploy source package for snapshot version

## 1.0.0-BETA-6 (2017-09-07)
* Use package name as artifact Id if not provided

## 1.0.0-BETA-5 (2017-09-06)
* Version.get() returns caller's version
* Log warn message if .version file contains variable reference

## 1.0.0-BETA-4 (2017-09-05)
* Use lowercase `r` to decorate version tag

### 1.0.0-BETA-3 (2017-09-05)
* Introduce `artifactId` into Version data
* Add `Version.getVersion()` method which returns combination of projectVersion and buildNumber
* Add `Bootstrap` class with `Version` constant defined

### 1.0.0-BETA-2 (2017-09-04)
* Fix `Version.toString()` issue with `buildNumber` be empty string

### 1.0.0-BETA-1 (2017-09-04) 
* first version
