package org.osgl.bootstrap;

/*-
 * #%L
 * Java Unit Test Tool
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

import net.evil.pkg.Kit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mrcool.swissknife.SwissKnife;
import org.mrcool.swissknife.db.DbUtil;
import org.mrcool.swissknife.internal.StringUtil;
import org.mrsuck.MyTool;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class VersionTest extends Assert {

    protected Logger logger;

    @Before
    public void prepare() throws Exception {
        Version.clearCache();
        logger = Mockito.mock(Logger.class);
        Field field = Version.class.getDeclaredField("logger");
        field.setAccessible(true);
        field.set(null, logger);
    }

    @Test
    public void itShallLoadVersionInfoFromResourceIfCacheNotHit() {
        Version version = Version.of(SwissKnife.class);
        assertEquals("swissknife", version.getArtifactId());
        assertEquals("1.0", version.getProjectVersion());
        assertEquals("3a77", version.getBuildNumber());
    }

    @Test
    public void itShallNotLoadVersionInfoFromResourceIfCacheHits() {
        Version version1 = Version.of(SwissKnife.class);
        Version version2 = Version.of(StringUtil.class);
        assertSame(version1, version2);
    }

    @Test
    public void itShallLoadVersionInfoFromSubPackageIfDefined() {
        // load parent package version
        Version.of(SwissKnife.class);

        // load subpackage version
        Version version = Version.of(DbUtil.class);
        assertEquals("swissknife-db", version.getArtifactId());
        assertEquals("0.8-SNAPSHOT", version.getProjectVersion());
        assertEquals("", version.getBuildNumber());
    }

    @Test
    public void itShallReturnUnknownIfVersionInfoNotProvided() {
        assertSame(Version.UNKNOWN, Version.of(MyTool.class.getPackage()));
    }

    @Test
    public void itShallRejectLoadingVersionInfoFromFirstLevelPackage() {
        assertSame(Version.UNKNOWN, Version.of(Kit.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalPackageNameCaseOne() {
        Version.of("org.mrsuck..proj");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalPackageNameCaseTwo() {
        Version.of("org.#abc.xyz");
    }

    @Test
    public void testDecoratedSnapshotProjectVersion() {
        String projectVersion = "1.0.0-SNAPSHOT";
        assertEquals("v" + projectVersion, Version.decoratedProjectVersion(projectVersion));
    }

    @Test
    public void testDecoratedProjectVersion() {
        String projectVersion = "1.0.0";
        assertEquals("R" + projectVersion, Version.decoratedProjectVersion(projectVersion));
    }

    @Test
    public void versionTagShallBeCombinationOfDecoratedProjectVersionAndDecoratedBuildNumberIfBuildNumberDefined() {
        Version version = Version.of(SwissKnife.class);
        String versionTag = version.getVersion();
        String expected = Version.decoratedProjectVersion(version.getProjectVersion())
                + "-" + version.getBuildNumber();
        assertEquals(expected, versionTag);
    }

    @Test
    public void versionTagShallBeDecoratedProjectVersionWhenBuildNumberIsNotDefined() {
        Version version = Version.of(DbUtil.class);
        assertEquals(Version.decoratedProjectVersion(version.getProjectVersion()), version.getVersion());
    }

    @Test
    public void itShallPrintArtifactsAndVersionTagInToString() {
        Version versionWithoutBuildNumber = Version.of(DbUtil.class);
        String expected = versionWithoutBuildNumber.getArtifactId() + "-" +versionWithoutBuildNumber.getVersion();
        assertEquals(expected, versionWithoutBuildNumber.toString());

        Version versionWithBuildNumber = Version.of(SwissKnife.class);
        expected = versionWithBuildNumber.getArtifactId() + "-" +versionWithBuildNumber.getVersion();
        assertEquals(expected, versionWithBuildNumber.toString());
    }

    @Test
    public void itShallReturnUnknownAndLogErrorMessageIfNoArtifactDefinedInVersionFile() {
        assertSame(Version.UNKNOWN, Version.of("org.demo.badversion.noart"));
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageArgCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(logger, Mockito.times(1)).error(messageCaptor.capture(), messageArgCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("artifact not defined in .version file"));
        assertTrue(messageArgCaptor.getValue().toString().equals("org.demo.badversion.noart"));
    }

    @Test
    public void itShallReturnUnknownAndLogErrorMessageIfNoVersionDefinedInVersionFile() {
        assertSame(Version.UNKNOWN, Version.of("org.demo.badversion.noversion"));
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageArgCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(logger, Mockito.times(1)).error(messageCaptor.capture(), messageArgCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("version not defined in .version file"));
        assertTrue(messageArgCaptor.getValue().toString().equals("org.demo.badversion.noversion"));
    }
}
