# OSGL Bootstrap

[![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) 
[![Maven Central](https://img.shields.io/maven-central/v/org.osgl/osgl-bootstrap.svg)](http://search.maven.org/#search%7Cga%7C1%7Cosgl-bootstrap)
[![Build Status](https://travis-ci.org/osglworks/java-osgl-bootstrap.svg?branch=master)](https://travis-ci.org/osglworks/java-osgl-bootstrap)
[![codecov](https://codecov.io/gh/osglworks/java-osgl-bootstrap/branch/master/graph/badge.svg)](https://codecov.io/gh/osglworks/java-osgl-bootstrap)
[![Javadocs](http://www.javadoc.io/badge/org.osgl/osgl-bootstrap.svg?color=red)](http://www.javadoc.io/doc/org.osgl/osgl-bootstrap)

其他 OSGL Java 工具库公用的最小工具集

* 版本工具: 给其他 OSGL 库或者任何 Java 应用创建并访问运行时版本信息的工具

## 安装

将一下依赖加入到你的 `pom.xml` 文件:

```xml
<dependency>
    <groupId>org.osgl</groupId>
    <artifactId>osgl-bootstrap</artifactId>
    <version>${osgl-bootstrap-version}</version>
</dependency>
```

## [版本工具]准备版本信息

库和应用开发者需要准备版本信息, 这样可以在运行时被访问

我们假设你的库或者应用的包名是 `org.mrcool.swissknife`, 你需要将一个 `.version` 文件放入 `src/resources/org/mrcool/swissknife` 目录. 文件内容大致为:

```properties
# 可选, 如果没有提供则使用包名替代
artifact=<delivery-name>

# 必填, 如果未提供则使用 `unknown` 替代
version=<the project version>

# 可选, 如果未提供则使用空字串替代
build=<SCM build number, e.g. git hash>
``` 
通常来讲我们不希望每次发布新版本都去更新这个文件的内容,因此采用 maven 提供的资源过滤特性来帮助生成最终的 `.version` 文件. 而 `.version` 源代码则变成:

```properties
artifact=${project.artifactId}
version=${project.version}
build=${buildNumber}
```

其中 `${project.artifactId}` 和 `${project.version}` 来自 maven 提供的环境变量, 而 `${buildNumber}` 则可以通过 [buildnumber maven plugin](http://www.mojohaus.org/buildnumber-maven-plugin/) 插件获取.

**注意** 你需要打开资源过滤让 maven 将`.version` 文件中的变量做替换:

```xml
<resources>
  <resource>
    <directory>src/main/resources</directory>
    <filtering>true</filtering>
    <includes>
      <include>**/.version</include>
    </includes>
   </resource>
</resources>
```


## [版本工具]在运行时访问版本信息

如果你按照上面的步骤准备了 `.version` 文件, 在发布的时候该文件会被打包进 jar 文件, 你的产品用户可以通过 API 来访问你的产品的版本信息:

```java
Version version1 = Version.of(SwissKnife.class);
System.out.println(version1.getArtifactId()); // print `swissknife`
System.out.println(version1.getProjectVersion()); // print `1.0`
System.out.println(version1.getBuildNumber()); // print `ebf1`
System.out.println(version1.getVersion()); // print `r1.0-ebf1`
System.out.println(version1); // print `swissknife-r1.0-ebf1`

// Another method to get Version info
Version version2 = Version.of("org.mrcool.swissknife.db");

// If a certain library's version is SNAPSHOT, e.g. 1.0-SNAPSHOT, 
// then the version tag is decorated with `v` instead of `r`:
System.out.println(version2.getProjectVersion()); // print `1.0-SNAPSHOT`
System.out.println(version2.getBuildNumber()); // print `51b9`
System.out.println(version2.getVersion()); // print `v1.0-SNAPSHOT-51b9`
System.out.println(version2); // print `swissknife-v1.0-SNAPSHOT-ebf1`
```

**小贴士** 如果如果应用希望获得自己的版本,一个简单的办法是使用 `Version.get()`:

```java
package com.myproj;

public class Foo {
    public Version version() {
        // normal way to get Foo's version:
        return Version.of(Foo.class);
    }
    
    public Version version2() {
        // easy way (but more runtime cost) to get Foo's version
        return Version.get();
    }
}
```

关于性能: 版本工具会缓存从磁盘中加载的 .version 文件内容这样访问同一个软件的版本信息不会重复请求 IO 操作

