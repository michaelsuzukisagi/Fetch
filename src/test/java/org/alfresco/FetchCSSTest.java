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

import java.io.File;
import java.io.IOException;

import org.alfresco.selenium.FetchCSS;
import org.alfresco.selenium.FetchUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the functions of parsing css and getting all the correct content.
 * @author Michael Suzuki
 * 
 */
public  class FetchCSSTest extends AbstractTest
{

    String css = "html.js input.form-autocomplete{background-image:url(//cdn-www.alfresco.com/misc/throbber-inactive.png);background-position:100% center;background-repeat:no-repeat;}html.js input.throbbing{background-image:url(//cdn-www.alfresco.com/misc/throbber-active.gif);background-position:100% center;";
    @Test
    public void parseCSS() throws IOException
    {
        File assetDir = new File(FetchUtil.ASSET_DIR);
        FileUtils.deleteDirectory(assetDir);
        assetDir.mkdirs();
        Assert.assertEquals(0, assetDir.listFiles().length);
        File file = new File(FetchUtil.ASSET_DIR + "/my.css");
        FileUtils.write(file, css);
        FetchCSS.getCSSFiles(file,driver);
        Assert.assertEquals(2, assetDir.listFiles().length);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void parseCSSNull() throws IOException
    {
        FetchCSS.getCSSFiles(null,driver);
    }
    
    @Test
    public void stripBaseTag()
    {
        String html = "<html><head><base href=\"http://docs.alfresco.com/5.0/concepts/install-singleinstance.html\" /></head><body>base</body></html>";
        String expected = "<html><head></head><body>base</body></html>";
        String result = FetchUtil.stripBaseTag(html);
        Assert.assertNotNull(result);
        Assert.assertNotEquals(result,html);
        Assert.assertFalse(result.contains("<base"));
        Assert.assertEquals(expected, result);
    }
    @Test
    public void handleNonExistentBaseTag()
    {
        String html = " <html><head></head><body>base</body></html>";
        String result = FetchUtil.stripBaseTag(html);
        Assert.assertNotNull(result);
        Assert.assertEquals(result,html);
    }
}
