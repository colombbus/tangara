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

import org.colombbus.tangara.core.Version;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @version $Id: SoftwareUpdateInfoTest.java,v 1.1.2.2 2009-07-19 08:24:39 gwenael.le_roux Exp $
 * @author gwen
 */
public class SoftwareUpdateInfoTest {

    private static final Version VERSION = new Version("1.4");

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

    /**
     * Test method for {@link org.colombbus.updater.SoftwareUpdateInfo#SoftwareInfo()}.
     */
    @Test
    public void testSoftwareInfo() {
        SoftwareUpdateInfo info = new SoftwareUpdateInfo();
        assertNull(info.getVersion());
    }

    /**
     * Test method for {@link org.colombbus.updater.SoftwareUpdateInfo#getVersion()}.
     */
    @Test
    public void testGetVersion() {
        SoftwareUpdateInfo info = new SoftwareUpdateInfo();
        assertNull(info.getVersion());
    }

    /**
     * Test method for {@link org.colombbus.updater.SoftwareUpdateInfo#setVersion(java.lang.String)}.
     */
    @Test
    public void testSetNullVersion() {
        SoftwareUpdateInfo info = new SoftwareUpdateInfo();
        info.setVersion(null);
        assertNull(info.getVersion());
    }

    /**
     * Test method for {@link org.colombbus.updater.SoftwareUpdateInfo#setVersion(java.lang.String)}.
     */
    @Test
    public void testSetVersion() {
        SoftwareUpdateInfo info = new SoftwareUpdateInfo();
        info.setVersion(VERSION);
        assertEquals(VERSION, info.getVersion());
    }

    /**
     * Test method for
     * {@link org.colombbus.updater.SoftwareUpdateInfo#isDifferentVersion(java.lang.String)}.
     */
    @Test
    public void testMoreRecentThan() {
        SoftwareUpdateInfo info = new SoftwareUpdateInfo();
        info.setVersion(VERSION);

        assertFalse(info.moreRecentThan(VERSION));
        assertTrue(info.moreRecentThan(new Version("1.3")));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMoreRecentThanNullArg() {
        SoftwareUpdateInfo info = new SoftwareUpdateInfo();
        info.setVersion(VERSION);
        info.moreRecentThan(null);
    }

}
