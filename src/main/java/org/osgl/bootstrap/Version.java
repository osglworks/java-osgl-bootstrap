package org.osgl.bootstrap;

/*-
 * #%L
 * OSGL Bootstrap
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represent **version** of a specific Java library.
 *
 * The version contains two piece of information:
 *
 * * maven project version, which is defined by `${project.version}`
 * * scm build version, usually provided by
 * [buildnumber-maven-plugin](http://www.mojohaus.org/buildnumber-maven-plugin/)
 *
 * # Usage
 *
 * ## 1. Prepare version file
 *
 * Application or library developer put a file named `.version` into a dir corresponding
 * to app/lib's package inside the `resources` dir. For example if a library package
 * name is `org.mrcool.swissknife`, then put the `.version` file under
 *
 * ```
 * src/main/resources/org/mrcool/swissknife`
 * ```
 *
 * The content of the `.version` file should be
 *
 * ```
 * version=${project.version}
 * build=${buildNumber} # optional
 * ```
 *
 * **Note** don't forget to set resource filter in the library's `pom.xml` file:
 *
 * ```
 * <resources>
 * <resource>
 * <directory>src/main/resources</directory>
 * <filtering>true</filtering>
 * </resource>
 * </resources>
 * ```
 *
 * **Note** as a general rule, do **NOT** put the `.version` file for the first level
 * package, e.g. `org`, `com`, `net` etc. Version tool will never read the `.version`
 * file for a package name that does not contain a `.` inside
 *
 * ## 2. Retrieve the version info
 *
 * In order to obtain the version information about a library or app's class, one can use
 * the API provided by this `Version` class:
 *
 * ```java
 * Version swissKnifeVersion = Version.of(org.mrcool.siwssknife.SwissKnife.class);
 * ```
 */
public final class Version {

    public static final String UNKNOWN_STR = "unknown";

    public static final Version UNKNOWN = new Version(UNKNOWN_STR, UNKNOWN_STR);

    private static final ConcurrentMap<String, Version> cache = new ConcurrentHashMap<String, Version>();

    private final String projectVersion;
    private final String buildNumber;

    private Version(String projectVersion, String buildNumber) {
        if (null == projectVersion || "".equals(projectVersion.trim())) {
            throw new IllegalArgumentException("project version is empty");
        }
        this.projectVersion = projectVersion.trim();
        this.buildNumber = isBlank(buildNumber) ? "" : buildNumber.trim();
    }

    /**
     * Returns project version which is defined in `${project.version}` maven environment variable
     *
     * @return the project version
     */
    public String getProjectVersion() {
        return projectVersion;
    }

    /**
     * Returns the build number which is defined in `${buildNumber}` maven environment variable when
     * [buildnumber-maven-plugin](http://www.mojohaus.org/buildnumber-maven-plugin/) is provided
     *
     * @return the SCM build number
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;

        if (!projectVersion.equals(version.projectVersion)) {
            return false;
        }
        return buildNumber != null ? buildNumber.equals(version.buildNumber) : version.buildNumber == null;
    }

    @Override
    public int hashCode() {
        int result = projectVersion.hashCode();
        result = 31 * result + (buildNumber != null ? buildNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(projectVersion);
        if (!isBlank(buildNumber)) {
            sb.append("-").append(buildNumber);
        }
        return sb.toString();
    }

    /**
     * Returns a `Version` corresponding to the package name specified.
     *
     * This method will tried to check if {@link #cache local cache} contains
     * the package name first, and return the version instance mapped to the
     * package name in local cache, or if not found in local cache try to load
     * the `.version` file as a resource corresponding to the package name.
     *
     * If the `.version` file not found, this method will try to get version
     * of parent package until the package name is empty, in which case
     * the {@link #UNKNOWN} will be returned
     *
     * @param packageName the package name
     * @return a `Version` instance for that package
     */
    public static Version of(String packageName) {
        if (!isValidPackageName(packageName)) {
            throw new IllegalArgumentException("package name is not valid: " + packageName);
        }
        return of_(packageName);
    }

    /**
     * Returns a `Version` of the library contains the class of the object
     * specified.
     *
     * @param object the object
     * @return a `Version` for that class if provided or
     * {@link #UNKNOWN} if not provided
     * @throws NullPointerException if the object specified is `null`
     * @see #of(String)
     */
    public static Version of(Object object) {
        return object instanceof Class ? of((Class) object) : of(object.getClass());
    }

    /**
     * Returns a `Version` of the library contains the class specified.
     *
     * @param clazz the class
     * @return a `Version` for that class if provided or
     * {@link #UNKNOWN} if not provided
     * @throws NullPointerException if the class specified is `null`
     * @see #of(String)
     */
    public static Version of(Class<?> clazz) {
        String className = clazz.getName();
        int pos = className.lastIndexOf('.');
        if (pos < 0) {
            return UNKNOWN;
        }
        String packageName = className.substring(0, pos);
        return of_(packageName);
    }

    /**
     * Returns a `Version` of the library contains the package specified.
     *
     * @param pkg the package
     * @return a `Version` for the package if provided or
     * {@link #UNKNOWN} if not provided
     * @throws NullPointerException if the class specified is `null`
     * @see #of(String)
     */
    public static Version of(Package pkg) {
        return of_(pkg.getName());
    }

    private static Version of_(String packageName) {
        if (!packageName.contains(".")) {
            return UNKNOWN;
        }
        Version version = cache.get(packageName);
        if (null != version) {
            return version;
        }
        version = loadFromResource(packageName);
        if (null == version) {
            int pos = packageName.lastIndexOf('.');
            if (pos < 0) {
                return UNKNOWN;
            }
            return of(packageName.substring(0, pos));
        }
        cache.put(packageName, version);
        return version;
    }

    private static boolean isBlank(String s) {
        return null == s || "".equals(s.trim());
    }

    private static Version loadFromResource(String packageName) {
        Properties properties = PropertyLoader.INSTANCE.loadFromResource(packageName);
        return null == properties ? null : loadFrom(properties);
    }

    private static Version loadFrom(Properties properties) {
        String projectVersion = properties.getProperty("version");
        String buildNumber = properties.getProperty("build");
        return new Version(projectVersion, buildNumber);
    }

    private static boolean isValidPackageName(String s) {
        if (null == s) {
            return false;
        }

        int len = s.length();
        if (0 == len) {
            return false;
        }

        char[] ca = s.toCharArray();
        if (!Character.isJavaIdentifierStart(ca[0])) {
            return false;
        }

        boolean lastTimeIsDot = false;
        for (int i = 1; i < len; i++) {
            char c = ca[i];
            if ('.' == c) {
                if (lastTimeIsDot) {
                    return false;
                } else {
                    lastTimeIsDot = true;
                }
                continue;
            }
            lastTimeIsDot = false;
            if (!Character.isJavaIdentifierPart(ca[i])) {
                return false;
            }
        }

        return true;
    }

    static void clearCache() {
        cache.clear();
    }

}
