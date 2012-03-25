/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2009 Colombbus (http://www.colombbus.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara.update;


import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.colombbus.tangara.core.Version;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class UpdateRequesterTest {

    private static final Version VERSION = new Version("1.5"); //$NON-NLS-1$
    private static final String LINK = "http://tangara.colombbus.org/"; //$NON-NLS-1$
    private static final String DESCRIPTION = "A new version is available. It contains new great features"; //$NON-NLS-1$
    private static final String SITE_URL="http://localhost/~gwen/"; //$NON-NLS-1$

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetUpdateSite() throws Exception {
        UpdateRequester updater = new UpdateRequester();
        updater.setUpdateSite(SITE_URL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullUpdateSite() throws MalformedURLException {
        UpdateRequester updater = new UpdateRequester();
        updater.setUpdateSite(null);
    }

    @Ignore
    @Test
    public void testRequestSoftwareInfo() throws IOException {
        UpdateRequester updater = new UpdateRequester();
        updater.setUpdateSite(SITE_URL);
        SoftwareUpdateInfo info = updater.requestSoftwareInfo();
        Version version = info.getVersion();
        assertEquals(VERSION, version);
        String link = info.getLink();
        assertEquals(LINK, link);
        String description = info.getDescription();
        assertEquals(DESCRIPTION, description);
    }

}
