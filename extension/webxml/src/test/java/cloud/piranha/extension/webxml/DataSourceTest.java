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
package cloud.piranha.extension.webxml;

import cloud.piranha.core.impl.DefaultWebApplication;
import cloud.piranha.core.impl.DefaultWebApplicationExtensionContext;
import cloud.piranha.extension.herring.HerringExtension;
import cloud.piranha.resource.impl.DirectoryResource;
import java.io.File;
import javax.naming.InitialContext;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests testing web.xml &lt;data-source&gt;.
 * 
 * @author Manfred Riem (mriem@manorrock.com)
 */
class DataSourceTest {

    /**
     * Test data-source.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    void testDataSource() throws Exception {
        DefaultWebApplication webApplication = new DefaultWebApplication();
        DefaultWebApplicationExtensionContext extensionContext = new DefaultWebApplicationExtensionContext();
        extensionContext.add(HerringExtension.class);
        extensionContext.configure(webApplication);
        webApplication.addResource(new DirectoryResource(new File("src/test/webxml/dataSource")));
        webApplication.addInitializer(new WebXmlInitializer());
        webApplication.initialize();
        InitialContext context = new InitialContext();
        assertNotNull(context.lookup("jdbc/demo"));
    }
}
