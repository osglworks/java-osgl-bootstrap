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
import org.mrcool.swissknife.SwissKnife;
import org.mrcool.swissknife.db.DbUtil;
import org.mrcool.swissknife.internal.StringUtil;
import org.mrsuck.MyTool;

public class VersionTest extends Assert {

    @Before
    public void prepare() {
        Version.clearCache();
    }

    @Test
    public void itShallLoadVersionInfoFromResourceIfCacheNotHit() {
        Version version = Version.of(SwissKnife.class);
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
        Version version = Version.of(new DbUtil());
        assertEquals("0.8", version.getProjectVersion());
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
}
