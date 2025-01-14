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

/**
 * The lite extension module.
 *
 * <p>
 *  This module delivers the Lite extension which in turn enables the following
 *  extensions:
 * </p>
 * <ul>
 *  <li>Annotation Scanning</li>
 *  <li>Manorrock Herring (JNDI)</li>
 *  <li>Locale Encoding</li>
 *  <li>Mime-type</li>
 *  <li>Java Policy</li>
 *  <li>ServletContainerInitializer</li>
 *  <li>Servlet Security</li>
 *  <li>Servlet Annotations</li>
 *  <li>TEMPDIR</li>
 *  <li>web.xml</li>
 * </ul>
 * <p>
 *  This module is deprecated and will be removed in a future release. Depending
 *  on your use case you can either replace it with the CoreProfileExtension or
 *  the WebProfileExtension.
 * </p>
 * 
 * @deprecated
 */
@Deprecated(since = "22.9.0", forRemoval = true)
module cloud.piranha.extension.lite {
    
    exports cloud.piranha.extension.lite;
    opens cloud.piranha.extension.lite;
    requires cloud.piranha.core.api;
    requires transitive cloud.piranha.extension.annotationscan;
    requires transitive cloud.piranha.extension.herring;
    requires transitive cloud.piranha.extension.localeencoding;
    requires transitive cloud.piranha.extension.mimetype;
    requires transitive cloud.piranha.extension.policy;
    requires transitive cloud.piranha.extension.scinitializer;
    requires transitive cloud.piranha.extension.security.servlet;
    requires transitive cloud.piranha.extension.servletannotations;
    requires transitive cloud.piranha.extension.tempdir;
    requires transitive cloud.piranha.extension.webxml;
}
