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
/**
 * Thrown when page capture is not able to save the page to a file.
 *  
 * @author Michael Suzuki
 */
public class PageCaptureException extends RuntimeException
{
    private static final long serialVersionUID = 850985590207217019L;
    private static final String DEFAULT_MESSAGE = "Unable to capture page";
    
    public PageCaptureException(String reason)
    {
        super(reason);
    }

    public PageCaptureException(String reason, Throwable cause)
    {
        super(reason, cause);
    }
    
    public PageCaptureException()
    {
        super(DEFAULT_MESSAGE);
    }

}
