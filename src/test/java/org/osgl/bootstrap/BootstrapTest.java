package org.osgl.bootstrap;

import org.junit.Assert;
import org.junit.Test;

public class BootstrapTest extends Assert {

    @Test
    public void bootstrapVersionShallContainsBootstrapArtifactId() {
        Version version = Bootstrap.VERSION;
        assertEquals("osgl-bootstrap", version.getArtifactId());
    }

}
