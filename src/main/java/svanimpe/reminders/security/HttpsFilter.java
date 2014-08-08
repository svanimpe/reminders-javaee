/*
 * Copyright (c) 2014, Steven Van Impe
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package svanimpe.reminders.security;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * This filter enforces SSL by forwarding all requests to HTTPS.
 */
@WebFilter(filterName = "HttpsFilter", urlPatterns = {"/*"})
public class HttpsFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Check to see if the request is already secure. If not, rebuild the URL using HTTPS and redirect.
        if (!httpRequest.isSecure() && (httpRequest.getHeader("X-Forwarded-Proto") == null || !httpRequest.getHeader("X-Forwarded-Proto").equals("https"))) {

            StringBuilder newUrl = new StringBuilder("https://");
            newUrl.append(httpRequest.getServerName());
            //newUrl.append(":8181"); // Uncomment this line for a local GlassFish server.

            if (httpRequest.getRequestURI() != null) {
                newUrl.append(httpRequest.getRequestURI());
            }

            if (httpRequest.getQueryString() != null) {
                newUrl.append("?").append(httpRequest.getQueryString());
            }

            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect(newUrl.toString());

        } else {

            // Simply go to the next filter in the chain.
            if (chain != null) {
                chain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy()
    {
    }
}
