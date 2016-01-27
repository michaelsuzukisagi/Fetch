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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;

/**
 * Utility that captures the page for offline use.
 * An alternative to screen shot which allows further inspection of code changes.
 * The implementation class is responsible for storing the html along with 
 * images, js and css.
 * 
 * The proposed default storage structure is:
 * page-nth.html
 * img directory for all images.
 * css style sheets.
 * js  all javascripts imported by the page.
 * <p>
 * Example usage:
 * CapturePageUtil.save(driver)
 * <pre>
 *
 * boolean saved = PageCapture.save(driver);
 * </pre>
 *
 * @see OutputType
 */
public class SavePageUtil
{
    private final static Log logger = LogFactory.getLog(SavePageUtil.class);
    private final static String OUTPUT_DIR = "./target/";
    private final static String ASSET_DIR = OUTPUT_DIR + "content/";
    private final static Pattern SRC_PATTERN = Pattern.compile("(?<=src=\")[^\"]*(?<!\")");
    private final static Pattern CSS_PATTERN = Pattern.compile("(?<=url\\(\").*?(?=\"\\))");
    private final static Pattern CSS_LINK_PATTERN = Pattern.compile("<link.*?\\>");
    private final static Pattern HREF_PATTERN = Pattern.compile("(?<=href=\").*?(?=\")");
    
    public static void save(WebDriver driver, String filename) throws PageCaptureException, IOException
    {
        String html = driver.getPageSource();
        //download all js files
        List<String> files = extractFiles(html);
        List<URL> urls = parseURL(files, "http://localhost:8080");//TODO remove hard coded values
        getFiles(urls, OUTPUT_DIR);
        String newhtml = parseHtml(html, files);
        File file = new File(OUTPUT_DIR + filename);
        FileUtils.writeStringToFile(file, newhtml);
        
    }
    public static String parseHtml(String html, List<String> files)
    {
        String value = html.substring(0);
        for(String file : files)
        {
            //Get the name of the asset.
            int index = file.lastIndexOf("/");
            String name = file.substring(index + 1);
            value = value.replaceFirst(file, ASSET_DIR + name);
        }
        return value;
    }
    
    /**
     * Extract source location of all files related to HTML.
     * @param html
     * @return List of all source locations relating to js, css and images
     */
    public static List<String> extractFiles(String html)
    {
        List<String> list = new ArrayList<String>();
        //Find all src=
        Matcher matchSrc = SRC_PATTERN.matcher(html);
        while (matchSrc.find()) 
        {
            list.add(matchSrc.group(0));
        } 
        //find all url('')
        Matcher css = CSS_PATTERN.matcher(html);
        while (css.find()) 
        {
            list.add(css.group(0));
        } 
        //Find all <link href="">
        Matcher css2 = CSS_LINK_PATTERN.matcher(html);
        while (css2.find()) 
        {
            Matcher href = HREF_PATTERN.matcher(css2.group(0));
            if (href.find())
            {
                list.add(href.group(0));
            }
        } 
        return list;
    }
    /**
     * Parse collection of file paths to URL.
     * @param files collection of paths
     * @param baseUrl the url prefix for relative path
     * @return Collection of URL
     */
    public static List<URL> parseURL(List<String> files,final String baseUrl)
    {
        if(files == null || files.isEmpty())
        {
            return Collections.emptyList();
        }
        //Strip tail "/"
        String base = StringUtils.removeEnd(baseUrl, "/");
        List<URL> urls = new ArrayList<URL>();
        for(String url : files)
        {
            try
            {
                if(!url.startsWith("http"))
                {
                    //Check if / is needed as we striped / from the base url.
                    String p = url.startsWith("/") ? base + url : base + "/" + url;
                    urls.add(new URL(p));
                }
                else
                {
                    urls.add(new URL(url));
                }
            }
            catch(MalformedURLException e)
            {
                logger.error("Unable to parse url", e);
            }
        } 
        return urls;
    }
    /**
     * Download all external files to local directory.
     * @param files collection to download
     * @param destination to store the files
     */
    public static void getFiles(List<URL> files, String pathname)
    {
        for(URL source: files)
        {
            int index  = source.toString().lastIndexOf("/");
            String name = source.toString().substring(index + 1);
            File destination = new File(pathname + "/" + name);
            try
            {
                FileUtils.copyURLToFile(source, destination);
            } 
            catch (Exception e)
            {
                logger.error(e);
            } 
        }
    }

}
