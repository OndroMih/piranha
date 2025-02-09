/*
 * Copyright (c) 2002-2022 Manorrock.com. All Rights Reserved.
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
package cloud.piranha.extension.micro;

import static java.util.Arrays.asList;

import cloud.piranha.core.api.WebApplication;
import cloud.piranha.core.api.WebApplicationExtension;
import cloud.piranha.core.api.WebApplicationExtensionContext;
import cloud.piranha.extension.apache.fileupload.ApacheMultiPartExtension;
import cloud.piranha.extension.async.AsyncExtension;
import cloud.piranha.extension.eclipselink.EclipseLinkExtension;
import cloud.piranha.extension.exousia.AuthorizationPostInitializer;
import cloud.piranha.extension.localeencoding.LocaleEncodingExtension;
import cloud.piranha.extension.mimetype.MimeTypeExtension;
import cloud.piranha.extension.naming.NamingExtension;
import cloud.piranha.extension.policy.PolicyExtension;
import cloud.piranha.extension.scinitializer.ServletContainerInitializerExtension;
import cloud.piranha.extension.security.jakarta.JakartaSecurityExtension;
import cloud.piranha.extension.security.servlet.ServletSecurityManagerExtension;
import cloud.piranha.extension.servletannotations.ServletAnnotationsExtension;
import cloud.piranha.extension.tempdir.TempDirExtension;
import cloud.piranha.extension.transact.TransactExtension;
import cloud.piranha.extension.wasp.WaspExtension;
import cloud.piranha.extension.webxml.WebXmlExtension;
import cloud.piranha.extension.welcomefile.WelcomeFileExtension;

/**
 * The default {@link WebApplicationExtension} used to configure a web 
 * application for Piranha Micro.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 * @deprecated
 */
@Deprecated(since = "22.10.0", forRemoval = true)
public class MicroExtension implements WebApplicationExtension {

    @Override
    public void extend(WebApplicationExtensionContext context) {
        // Servlet
        context.add(AsyncExtension.class);
        context.add(LocaleEncodingExtension.class);
        context.add(MimeTypeExtension.class);
        context.add(PolicyExtension.class);
        context.add(TempDirExtension.class);
        context.add(WelcomeFileExtension.class);
        context.add(ServletSecurityManagerExtension.class);
        context.add(ApacheMultiPartExtension.class);
        context.add(WebXmlExtension.class);
        context.add(ServletAnnotationsExtension.class);

        // JNDI
        context.add(NamingExtension.class);

        // Jakarta Transactions
        context.add(TransactExtension.class);

        // Jakarta Security (includes Jakarta CDI)
        context.add(JakartaSecurityExtension.class);

        // Jakarta Pages
        context.add(WaspExtension.class);

        // Jakarta Persistence
        context.add(EclipseLinkExtension.class);
    }

    @Override
    public void configure(WebApplication webApplication) {
        new ServletContainerInitializerExtension(
            true, asList("org.glassfish.soteria.servlet.SamRegistrationInstaller"))
            .configure(webApplication);
        webApplication.addInitializer(new AuthorizationPostInitializer());
    }
}
