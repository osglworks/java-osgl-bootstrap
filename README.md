# OSGL Bootstrap

[![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) 
[![Maven Central](https://img.shields.io/maven-central/v/org.osgl/osgl-bootstrap.svg)](http://search.maven.org/#search%7Cga%7C1%7Cosgl-bootstrap)
[![Build Status](https://travis-ci.org/osglworks/java-osgl-bootstrap.svg?branch=master)](https://travis-ci.org/osglworks/java-osgl-bootstrap)
[![codecov](https://codecov.io/gh/osglworks/java-osgl-bootstrap/branch/master/graph/badge.svg)](https://codecov.io/gh/osglworks/java-osgl-bootstrap)
[![Javadocs](http://www.javadoc.io/badge/org.osgl/osgl-bootstrap.svg?color=red)](http://www.javadoc.io/doc/org.osgl/osgl-bootstrap)

A minimum set of utilities required by all other OSGL Java libraries

* Version utility: allow OSGL library and any other Java app to create a runtime version info based on their maven build

## Installation

Add the following dependency into your `pom.xml` file:

```xml
<dependency>
    <groupId>org.osgl</groupId>
    <artifactId>osgl-bootstrap</artifactId>
    <version>${osgl-bootstrap-version}</version>
</dependency>
```

## Prepare version info

For library/app author, you need to prepare your version info so that Version tool can generate version at runtime. 

Let's say your library/app package name is `org.mrcool.swissknife`, you need to add a file named `.version` into `src/resources/org/mrcool/swissknife` dir, the file content should be:

```properties
version=<the version>
# build number is optional
build=<build number>
``` 

As a good practice you can rely on maven's resource filtering to automatically generate the version and build number for you, in which case your `.version` would look like:

```properties
version=${project.version}
build=${buildNumber}
```

Where the `${project.version}` comes from the `<version>` tag in your `pom.xml` file while `${buildNumber}` comes from the [buildnumber maven plugin](http://www.mojohaus.org/buildnumber-maven-plugin/).

## Get version info at runtime

Once you have prepared your `.version` file and packaged it into the jar file, the user of the jar file can always access the version information through simple API call:

```java
Version version1 = Version.of(SwissKnife.class);
Version version2 = Version.of(new SwissKnife());
Version version3 = Version.of("org.mrcool.swissknife");
```

Initially Version tool will hit the resource file to load the version info, once it is loaded, the tool will cache the loaded version instance with the package name so that next time it won't hit any I/O operation for the same package name.

