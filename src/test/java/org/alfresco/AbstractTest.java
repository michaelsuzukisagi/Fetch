/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


/**
 * Tests the functions of parsing css and getting all the correct content.
 * @author Michael Suzuki
 * 
 */
public  class AbstractTest
{
    protected static WebDriver driver;
    protected final static String ALFRESCO_TEST_URL = "http://localhost:8080/alfresco";
    protected final static String SHARE_TEST_URL = "http://localhost:8080/share";
    @BeforeClass
    public static void setup()
    {
        driver = new FirefoxDriver();
        driver.navigate().to(SHARE_TEST_URL);
    }
    
    @AfterClass
    public static void clean()
    {
        driver.quit();
    }
}
