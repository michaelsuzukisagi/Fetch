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
package org.alfresco.selenium;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openqa.selenium.WebDriver;

/**
 * Utility that parses the css file and collects all the external images to download
 * to local directory.
 * 
 *
 * @author Michael Suzuki
 */
public class FetchCSS extends FetchUtil
{
    private static final String URL_PATH_SEPARATOR = "/";
    /* regex to locate and extract source of all assets */
    private static final Pattern CSS_BACKGOUND_IMG_PATTERN = Pattern.compile("(?<=background-image:url\\().*?(?=\\))");
    //Required to handle strange characters on the page.
    /**
     * Extract additional content found in css.
     * @param source css 
     * @return collection of files to get 
     * @throws IOException if error
     */
    public static void getCSSFiles(File file, WebDriver driver) throws IOException
    {
        if(file == null)
        {
            throw new RuntimeException("CSS source is required");
        }
        String source = FileUtils.readFileToString(file);
        source = source.replaceAll("}", "}\n");
        String domain = getHost(driver);
        CloseableHttpClient client = FetchHttpClient.getHttpClient(driver);
        StringBuffer sb = new StringBuffer();
        try
        {
            //Extract all source files
            Matcher matchSrc = CSS_BACKGOUND_IMG_PATTERN.matcher(source);
            while (matchSrc.find()) 
            {
                //Change extracted source file to absolute urls 
                String fileSourceOri = matchSrc.group(0);
                String fileSource = fileSourceOri.replaceAll("\"", "");
                if(!fileSource.startsWith("data:image"))
                {
                    if(fileSource.startsWith("//"))
                    {
                        fileSource = fileSource.replace("//", "http://");
                    }
                    else if(fileSource.startsWith("/"))
                    {
                        fileSource = fileSource.replaceFirst("/", domain + "/");
                    }
                    //Add to collection of paths for downloading
                    getCssFile(fileSource,fileSourceOri, client);
                }
                //Amend path to point to file in the same directory
                if(!fileSourceOri.startsWith("//") || fileSourceOri.startsWith("\"/"))
                {
                    String t = fileSourceOri.replaceFirst("\"/", "\"");
                    matchSrc.appendReplacement(sb, t);
                }
                else
                {
                    
                    matchSrc.appendReplacement(sb, fileSourceOri);
                }
            } 
            if(sb.length() < 1) 
            {
                FileUtils.writeStringToFile(file, source);
            }
            else
            {
                FileUtils.writeStringToFile(file, sb.toString());
            }
            
        
        }
        finally
        {
            HttpClientUtils.closeQuietly(client);
        }
    }
    
    /**
     * Gets the external file using http client and stores the css and 
     * directory layout to content.
     * @param source file name
     * @param client {@link CloseableHttpClient}
     * @return {@link File} output.
     * @throws IOException if error
     */
    private static File getCssFile(final String sourceURL, final String filePath,
            final CloseableHttpClient client) throws IOException
    {
        String source = filePath.replaceAll("\"", "");
        int index  = source.lastIndexOf(URL_PATH_SEPARATOR);
        String path = source.substring(0, index);
        String name = source.substring(index);
        File target = new File(ASSET_DIR + path);
        if(!target.exists()) 
        {
            target.mkdirs();
        }
        File out = new File(ASSET_DIR + path + name);
        return retrieveFile(sourceURL, client, out);
    }
}
