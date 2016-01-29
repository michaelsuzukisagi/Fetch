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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.JavascriptExecutor;
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
 * SavePageUtil.save(driver, name.html)
 * <pre>
 *
 * boolean saved = SavePageUtil.save(driver);
 * </pre>
 *
 * @author Michael Suzuki
 */
public class SavePageUtil
{
    private final static Log logger = LogFactory.getLog(SavePageUtil.class);
    private static final String GET_BASE_URL_JS_COMMAND = "return document.location.origin;";
    private static final String URL_PATH_SEPARATOR = "/";
    private static final String OUTPUT_DIR = "./target/public/";
    private static final String ASSET_FOLDER =  "content/";
    private static final String ASSET_DIR = OUTPUT_DIR + ASSET_FOLDER;
    /* regex to locate and extract source of all assets */
    private static final Pattern SRC_PATTERN = Pattern.compile("(?<=src=\")[^\"]*(?<!\")");
    private static final Pattern CSS_PATTERN = Pattern.compile("(?<=url\\(\").*?(?=\"\\))");
    private static final Pattern CSS_LINK_PATTERN = Pattern.compile("<link.*?\\>");
    private static final Pattern HREF_PATTERN = Pattern.compile("(?<=href=\").*?(?=\")");
    //Required to handle strange characters on the page.
    private static final String UTF8_HTML = "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">";
    /**
     * Saves the current page as seen by the WebDriver
     * @param driver {@link WebDriver}
     * @param filename name of the HTML file output
     * @throws PageCaptureException if error
     * @throws IOException
     */
    public static void save(final WebDriver driver,final String filename) throws IOException 
    {
        String sourceHtml = driver.getPageSource();
        String currentUrl = driver.getCurrentUrl();
        String host = (String)((JavascriptExecutor) driver).executeScript(GET_BASE_URL_JS_COMMAND);
        //download all assets: js,img and stylesheet.
        List<String> files = extractFiles(sourceHtml);
        List<URL> urls = parseURL(files, host, currentUrl); 
        getFiles(urls, ASSET_DIR);
        String html = parseHtml(sourceHtml, files);
        System.out.println(html);
        File file = new File(OUTPUT_DIR + filename);
        file.delete();
        FileUtils.writeStringToFile(file, html);
    }
    
    /**
     * Extract source location of all files related to HTML.
     * @param html
     * @return List of all source locations relating to js, css and images
     */
    public static List<String> extractFiles(final String html)
    {
        List<String> list = new ArrayList<String>();
        //Find all src=
        Matcher matchSrc = SRC_PATTERN.matcher(html);
        while (matchSrc.find()) 
        {
            String i = matchSrc.group(0);
            System.out.println(i);
            list.add(i);
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
    public static List<URL> parseURL(List<String> files,final String baseUrl, final String currentURL)
    {
        if(files == null || files.isEmpty())
        {
            return Collections.emptyList();
        }
        //Strip tail "/"
        String base = StringUtils.removeEnd(baseUrl, URL_PATH_SEPARATOR);
        List<URL> urls = new ArrayList<URL>();
        for(String url : files)
        {
            try
            {
                if(!url.startsWith("http"))
                {
                    if(url.startsWith("."))
                    {
                        String relativePath = url.startsWith("..") ? url.substring(2) : url.substring(1);
                        urls.add(new URL(currentURL + relativePath));
                    }
                    else
                    {
                        //Check if / is needed as we striped / from the base url.
                        String p = url.startsWith(URL_PATH_SEPARATOR) ? base + url : base + URL_PATH_SEPARATOR + url;
                        urls.add(new URL(p));
                    }
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
            System.out.println(source);
            int index  = source.toString().lastIndexOf(URL_PATH_SEPARATOR);
            String name = source.toString().substring(index + 1);
            File destination = new File(ASSET_DIR + URL_PATH_SEPARATOR + name);
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
    /**
     * Updates the HTML with the new locations of assets.
     * @param html
     * @param files
     * @return
     */
    public static String parseHtml(String html, List<String> files)
    {
        if(html == null || html.isEmpty())
        {
            throw new RuntimeException("Html source code is required");
        }
        String value = html.substring(0);
        value = value.replaceFirst("<head>", "<head>\n" + UTF8_HTML);
        for(String file : files)
        {
            //Get the name of the asset.
            int index = file.lastIndexOf(URL_PATH_SEPARATOR);
            String name = file.substring(index + 1);
            value = value.replaceFirst(file, "./" + ASSET_FOLDER + name);
        }
        return value;
    }
}
