/*
 * Copyright (c) 2002-2019 Manorrock.com. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its 
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.manorrock.piranha;

import com.manorrock.piranha.api.HttpServerRequest;

import static java.lang.Long.parseLong;

import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.http.Cookie;

/**
 * The default WebApplicationServerRequest.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class DefaultWebApplicationServerRequest extends DefaultWebApplicationRequest {

    /**
     * Stores the request URI.
     */
    private String requestUri;

    /**
     * Constructor.
     *
     * @param request the request.
     */
    public DefaultWebApplicationServerRequest(HttpServerRequest request) {
        
        // Method
        
        if (request.getMethod() != null) {
            method = request.getMethod();
        }
        
        // Query String
        
        if (request.getQueryString() != null) {
            requestUri = request.getRequestTarget();
            queryString = request.getQueryString();
        } else {
            requestUri = request.getRequestTarget();
        }
        
        // Headers
        
        Iterator<String> headerNames = request.getHeaderNames();
        while (headerNames.hasNext()) {
            
            // General headers
            
            String name = headerNames.next();
            String unparsedValue = request.getHeader(name);
            headerManager.setHeader(name, unparsedValue);
            
            // Special purpose headers
            
            if (name.equalsIgnoreCase("Content-Type")) {
                contentType = unparsedValue;
            }
            
            if (name.equalsIgnoreCase("Content-Length")) {
                contentLength = parseLong(unparsedValue);
            }
            
            if (name.equalsIgnoreCase("COOKIE")) {
                ArrayList<Cookie> cookieList = new ArrayList<>();
                String[] cookieCandidates = unparsedValue.split(";");
                if (cookieCandidates.length > 0) {
                    for (String cookieCandidate : cookieCandidates) {
                        String[] cookieString = cookieCandidate.split("=");
                        Cookie cookie = new Cookie(cookieString[0].trim(), cookieString[1].trim());
                        if (cookie.getName().equals("JSESSIONID")) {
                            requestedSessionIdFromCookie = true;
                            requestedSessionId = cookie.getValue();
                        } else {
                            cookieList.add(cookie);
                        }
                    }
                }
                cookies = cookieList.toArray(new Cookie[0]);
            }
        }
    }
    
    /**
     * Get the request URI.
     *
     * @return the request URI.
     */
    @Override
    public String getRequestURI() {
        return requestUri;
    }
}
