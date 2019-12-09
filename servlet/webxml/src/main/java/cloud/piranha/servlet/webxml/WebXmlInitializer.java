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
package cloud.piranha.servlet.webxml;

import cloud.piranha.DefaultWebXml;
import cloud.piranha.DefaultWebXmlLoginConfig;
import static java.util.logging.Level.WARNING;
import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.STRING;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cloud.piranha.api.WebApplication;
import cloud.piranha.api.WebXml;
import cloud.piranha.api.WebXml.ErrorPage;
import cloud.piranha.api.WebXmlMimeMapping;
import cloud.piranha.api.WebXmlServletMapping;

/**
 * The web.xml initializer.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class WebXmlInitializer implements ServletContainerInitializer {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(WebXmlInitializer.class.getName());

    /**
     * Stores the WebXML context-param name.
     */
    private static final String WEB_FRAGMENTS = "cloud.piranha.servlet.webxml.WebFragments";

    /**
     * On startup.
     *
     * @param classes the classes.
     * @param servletContext the servlet context.
     * @throws ServletException when a servlet error occurs.
     */
    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        try {
            WebApplication webApp = (WebApplication) servletContext;
            InputStream inputStream = servletContext.getResourceAsStream("WEB-INF/web.xml");
            if (inputStream != null) {
                DefaultWebXml webXml = parseWebXml(servletContext.getResourceAsStream("WEB-INF/web.xml"));
                webApp.getWebXmlManager().setWebXml(webXml);
            }

            Enumeration<URL> webFragments = servletContext.getClassLoader().getResources("/META-INF/web-fragment.xml");
            ArrayList<DefaultWebXml> webXmls = new ArrayList<>();
            while (webFragments.hasMoreElements()) {
                URL url = webFragments.nextElement();
                webXmls.add(parseWebXml(url.openStream()));
            }
            if (!webXmls.isEmpty()) {
                webApp.setAttribute(WEB_FRAGMENTS, webXmls);
            }

            if (webApp.getWebXmlManager().getWebXml() == null) {
                List<DefaultWebXml> fragments = (List<DefaultWebXml>) webApp.getAttribute(WEB_FRAGMENTS);
                if (fragments != null && !fragments.isEmpty()) {
                    webApp.getWebXmlManager().setWebXml(fragments.get(0));
                }
            }

            if (webApp.getWebXmlManager().getWebXml() != null) {
                DefaultWebXml webXml = (DefaultWebXml) webApp.getWebXmlManager().getWebXml();
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(inputStream);
                XPath xPath = XPathFactory.newInstance().newXPath();

                /*
                 * Process <display-name> content.
                 */
                String displayName = (String) xPath.evaluate("//display-name/text()", document, XPathConstants.STRING);
                webApp.setServletContextName(displayName);

                /*
                 * Process <listener> entries
                 */
                NodeList list = (NodeList) xPath.evaluate("//listener", document, NODESET);
                if (list != null) {
                    for (int i = 0; i < list.getLength(); i++) {
                        String className = (String) xPath.evaluate("listener-class/text()", list.item(i), XPathConstants.STRING);
                        webXml.addListener(className);
                        webApp.addListener(className);
                    }
                }

                /*
                 * Process
                 */
                processServlets(webApp);
                processServletMappings(webApp);

                /*
                 * Process <security-constraint> entries
                 */
                list = (NodeList) xPath.evaluate("//security-constraint", document, NODESET);
                if (list != null) {
                    processSecurityConstraints(webXml, list);
                }

                /*
                 * Process <deny-uncovered-http-methods> entry
                 */
                Node node = (Node) xPath.evaluate("//deny-uncovered-http-methods", document, NODE);
                if (node != null) {
                    webXml.denyUncoveredHttpMethods = true;
                }

                processMimeMappings(webApp);

                /*
                 * Process <error-page> entries
                 */
                list = (NodeList) xPath.evaluate("//error-page", document, NODESET);
                if (list != null) {
                    processErrorPages(xPath, webXml, list);
                    for (ErrorPage errorPage : webXml.getErrorPages()) {
                        if (errorPage.getErrorCode() != null && !errorPage.getErrorCode().isEmpty()) {
                            webApp.addErrorPage(Integer.parseInt(errorPage.getErrorCode()), errorPage.getLocation());
                        } else if (errorPage.getExceptionType() != null && !errorPage.getExceptionType().isEmpty()) {
                            webApp.addErrorPage(errorPage.getExceptionType(), errorPage.getLocation());
                        }
                    }
                }

                /*
                 * Process <context-param> entries
                 */
                list = (NodeList) xPath.evaluate("//context-param", document, NODESET);
                if (list != null) {
                    processContextParameters(webXml, list);
                    Iterator<WebXml.ContextParam> iterator = webXml.getContextParams().iterator();
                    while (iterator.hasNext()) {
                        WebXml.ContextParam contextParam = iterator.next();
                        webApp.setInitParameter(contextParam.getName(), contextParam.getValue());
                    }
                }
            } else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.info("No web.xml found!");
                }
            }
        } catch (SAXException | XPathExpressionException | IOException
                | ParserConfigurationException e) {
            LOGGER.log(WARNING, "Unable to parse web.xml", e);
        }
    }

    /**
     * Parse DefaultWebXml.
     *
     * @param inputStream the input stream.
     * @return the DefaultWebXml.
     */
    public DefaultWebXml parseWebXml(InputStream inputStream) {
        DefaultWebXml result = new DefaultWebXml();
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            XPath xPath = XPathFactory.newInstance().newXPath();
            result.servlets.addAll((List<DefaultWebXml.Servlet>) parseList(
                    xPath, document, "//servlet", WebXmlInitializer::parseServlet));

            parseLoginConfig(result, xPath, document);
            parseMimeMappings(result, xPath, document);
            parseServletMappings(result, xPath, document);

        } catch (Throwable t) {
            LOGGER.log(WARNING, "Unable to parse web.xml", t);
        }

        return result;
    }

    // ### Private methods
    /**
     * Parse a servlet.
     *
     * @param xPath the XPath to use.
     * @param node the DOM node to parse.
     * @return the servlet, or null if an error occurred.
     */
    private static DefaultWebXml.Servlet parseServlet(XPath xPath, Node node) {
        DefaultWebXml.Servlet result = new DefaultWebXml.Servlet();
        try {
            result.asyncSupported = (boolean) applyOrDefault(parseBoolean(xPath, node, "async-supported/text()"), false);
            result.name = (String) xPath.evaluate("servlet-name/text()", node, XPathConstants.STRING);
            result.className = (String) xPath.evaluate("servlet-class/text()", node, XPathConstants.STRING);
            Double loadOnStartupDouble = (Double) xPath.evaluate("load-on-startup/text()", node, XPathConstants.NUMBER);
            if (loadOnStartupDouble != null) {
                result.loadOnStartup = loadOnStartupDouble.intValue();
            } else {
                result.loadOnStartup = -1;
            }
            result.initParams.addAll((List<DefaultWebXml.Servlet.InitParam>) parseList(
                    xPath, node, "init-param", WebXmlInitializer::parseServletInitParam));
            return result;
        } catch (XPathExpressionException xee) {
            LOGGER.log(WARNING, "Unable to parse <servlet>", xee);
            result = null;
        }

        return result;
    }

    /**
     * Parse a servlet init-param.
     *
     * @param xPath the XPath to use.
     * @param node the DOM node to parse.
     * @return the init-param, or null if an error occurred.
     */
    private static DefaultWebXml.Servlet.InitParam parseServletInitParam(XPath xPath, Node node) {
        DefaultWebXml.Servlet.InitParam result = new DefaultWebXml.Servlet.InitParam();
        try {
            result.name = (String) xPath.evaluate("param-name/text()", node, XPathConstants.STRING);
            result.value = (String) xPath.evaluate("param-value/text()", node, XPathConstants.STRING);
        } catch (XPathExpressionException xee) {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "Unable to parse <init-param>", xee);
            }
            result = null;
        }
        return result;
    }

    /**
     * Process the DefaultWebXml.Servlet list.
     *
     * @param webApplication the web application.
     */
    private void processServlets(WebApplication webApplication) {
        DefaultWebXml webXml = (DefaultWebXml) webApplication.getWebXmlManager().getWebXml();
        Iterator<DefaultWebXml.Servlet> iterator = webXml.servlets.iterator();
        while (iterator.hasNext()) {
            DefaultWebXml.Servlet servlet = iterator.next();
            Dynamic registration = webApplication.addServlet(servlet.name, servlet.className);
            if (servlet.asyncSupported) {
                registration.setAsyncSupported(true);
            }
            if (!servlet.initParams.isEmpty()) {
                servlet.initParams.forEach((initParam) -> {
                    registration.setInitParameter(initParam.name, initParam.value);
                });
            }
        }
    }

    /**
     * Process the context-param entries.
     *
     * @param webXml the web.xml to add to.
     * @param nodeList the node list.
     * @return the web.xml.
     */
    private void processContextParameters(DefaultWebXml webXml, NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            processContextParameter(webXml, nodeList.item(i));
        }
    }

    /**
     * Process the context-param section.
     *
     * @param webXml the web.xml to add to.
     * @param node the DOM node.
     * @return the web.xml.
     */
    private void processContextParameter(DefaultWebXml webXml, Node node) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String name = (String) xPath.evaluate("//param-name/text()", node, XPathConstants.STRING);
            String value = (String) xPath.evaluate("//param-value/text()", node, XPathConstants.STRING);
            webXml.addContextParam(name, value);
        } catch (XPathException xpe) {
            LOGGER.log(WARNING, "Unable to parse <context-param> section", xpe);
        }
    }

    private void processSecurityConstraints(DefaultWebXml webXml, NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            processSecurityConstraint(webXml, nodeList.item(i));
        }
    }

    private void processSecurityConstraint(DefaultWebXml webXml, Node node) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            DefaultWebXml.SecurityConstraint securityConstraint = new DefaultWebXml.SecurityConstraint();

            forEachNode(xPath, node, "//web-resource-collection", webResourceCollectionNode -> {
                DefaultWebXml.SecurityConstraint.WebResourceCollection webResourceCollection = new DefaultWebXml.SecurityConstraint.WebResourceCollection();

                forEachString(xPath, webResourceCollectionNode, "//url-pattern",
                        urlPattern -> webResourceCollection.urlPatterns.add(urlPattern)
                );

                forEachString(xPath, webResourceCollectionNode, "//http-method",
                        httpMethod -> webResourceCollection.httpMethods.add(httpMethod)
                );

                forEachString(xPath, webResourceCollectionNode, "//http-method-omission",
                        httpMethodOmission -> webResourceCollection.httpMethodOmissions.add(httpMethodOmission)
                );

                securityConstraint.webResourceCollections.add(webResourceCollection);
            });

            forEachString(xPath, getNodes(xPath, node, "//auth-constraint"), "//role-name/text()",
                    roleName -> securityConstraint.roleNames.add(roleName)
            );

            securityConstraint.transportGuarantee = getString(xPath, node, "//user-data-constraint/transport-guarantee/text()");

            webXml.securityConstraints.add(securityConstraint);

        } catch (Exception xpe) {
            LOGGER.log(WARNING, "Unable to parse <servlet> section", xpe);
        }
    }

    private void processErrorPages(XPath xPath, DefaultWebXml webXml, NodeList nodeList) throws XPathExpressionException {
        for (int i = 0; i < nodeList.getLength(); i++) {
            processErrorPage(xPath, webXml, nodeList.item(i));
        }
    }

    private void processErrorPage(XPath xPath, DefaultWebXml webXml, Node node) throws XPathExpressionException {
        String errorCode = getString(xPath, node, "error-code/text()");
        String exceptionType = getString(xPath, node, "exception-type/text()");
        String location = getString(xPath, node, "location/text()");
        webXml.addErrorPage(errorCode, exceptionType, location);
    }

    // ### Utility methods
    /**
     * Short-cut method for forEachNode - forEachString, when only one node's
     * string value is needed
     *
     */
    private void forEachString(XPath xPath, NodeList nodes, String expression, ThrowingConsumer<String> consumer) throws XPathExpressionException {
        for (int i = 0; i < nodes.getLength(); i++) {
            consumer.accept((String) xPath.evaluate(expression, nodes.item(i), XPathConstants.STRING));
        }
    }

    private void forEachString(XPath xPath, Node parent, String expression, ThrowingConsumer<String> consumer) throws XPathExpressionException {
        forEachNode(xPath, parent, expression, node -> consumer.accept(getString(xPath, node, "child::text()")));
    }

    private void forEachNode(XPath xPath, Node node, String expression, ThrowingConsumer<Node> consumer) throws XPathExpressionException {
        NodeList nodes = (NodeList) xPath.evaluate(expression, node, NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            consumer.accept(nodes.item(i));
        }
    }

    private String getString(XPath xPath, Node node, String expression) throws XPathExpressionException {
        return (String) xPath.evaluate(expression, node, STRING);
    }

    private NodeList getNodes(XPath xPath, Node node, String expression) throws XPathExpressionException {
        return (NodeList) xPath.evaluate(expression, node, NODESET);
    }

    private interface ThrowingConsumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         */
        void accept(T t) throws XPathExpressionException;
    }

    /**
     * Apply non null value or default value.
     *
     * @param object the object.
     * @param defaultValue the default value.
     * @return the non null value (either the object or the default value).
     */
    private static Object applyOrDefault(Object object, Object defaultValue) {
        Object result = object;
        if (object == null) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Parse a boolean.
     *
     * @param xPath the XPath to use.
     * @param node the node to use.
     * @param expression the expression to use.
     * @param defaultValue the default value.
     * @return the boolean, or the default value if an error occurred.
     */
    private static Boolean parseBoolean(XPath xPath, Node node, String expression) {
        Boolean result = null;
        try {
            result = (Boolean) xPath.evaluate(expression, node, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException xee) {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "Unable to parse boolean", xee);
            }
        }
        return result;
    }

    /**
     * Parse a list.
     *
     * <pre>
     *  1. Get the node list based on the expression.
     *  2. For each element in the node list
     *      a. Call the bi-function
     *      b. Add the result of the bi-function to the result list.
     *  3. Return the result list.
     * </pre>
     *
     * @param xPath the XPath to use.
     * @param node the DOM node to parse.
     * @param expression the XPath expression.
     * @param biFunction the bi-function.
     * @return the list, or null if an error occurred.
     */
    private static List parseList(XPath xPath, Node node, String expression, BiFunction<XPath, Node, Object> biFunction) {
        ArrayList result = new ArrayList();
        try {
            NodeList nodeList = (NodeList) xPath.evaluate(expression, node, NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Object functionResult = biFunction.apply(xPath, nodeList.item(i));
                if (functionResult != null) {
                    result.add(functionResult);
                }
            }
        } catch (XPathExpressionException xee) {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "Unable to parse <" + expression + ">", xee);
            }
            result = null;
        }
        return result;
    }

    // -------------------------------------------------------------------------
    //  Parsing
    // -------------------------------------------------------------------------
    /**
     * Parse the login-config.
     *
     * @param webXml the web.xml to add to.
     * @param xPath the XPath to use.
     * @param node the DOM node.
     */
    private void parseLoginConfig(WebXml webXml, XPath xPath, Node node) {
        try {
            Node configNode = (Node) xPath.evaluate("//login-config", node, NODE);
            if (configNode != null) {
                String authMethod = parseString(xPath, 
                        "//auth-method/text()", configNode);
                String realmName = parseString(xPath,
                        "//realm-name/text()", configNode);
                String formLoginPage = parseString(xPath,
                        "//form-login-config/form-login-page/text()", configNode);
                String formErrorPage = parseString(xPath,
                        "//form-login-config/form-error-page/text()", configNode);
                DefaultWebXmlLoginConfig config = new DefaultWebXmlLoginConfig(
                        authMethod, realmName, formLoginPage, formErrorPage);
                webXml.setLoginConfig(config);
            }
        } catch (XPathException xpe) {
            LOGGER.log(WARNING, "Unable to parse login config", xpe);
        }
    }

    /**
     * Parse the mime-mapping entries.
     *
     * @param webXml the web.xml to add to.
     * @param XPath the XPath to use.
     * @param node the DOM node.
     * @return the web.xml.
     */
    private void parseMimeMapping(WebXml webXml, XPath xPath, Node node) {
        try {
            String extension = parseString(xPath, "//extension/text()", node);
            String mimeType = parseString(xPath, "//mime-type/text()", node);
            webXml.addMimeMapping(extension, mimeType);
        } catch (XPathException xpe) {
            LOGGER.log(WARNING, "Unable to parse mime-mapping", xpe);
        }
    }

    /**
     * Parse a string.
     *
     * @param xPath the XPath to use.
     * @param expression the expression.
     * @param node the node.
     * @return the string.
     * @throws XPathExpressionException when the expression was invalid.
     */
    private String parseString(XPath xPath, String expression, Node node)
            throws XPathExpressionException {
        return (String) xPath.evaluate(expression, node, XPathConstants.STRING);
    }

    /**
     * Parse the mime-mapping section.
     *
     * @param webXml the web.xml to add to.
     * @param xPath the XPath to use.
     * @param nodeList the node list.
     * @return the web.xml.
     */
    private void parseMimeMappings(WebXml webXml, XPath xPath, Node node) {
        try {
            NodeList nodeList = (NodeList) xPath.evaluate("//mime-mapping", node, NODESET);
            if (nodeList != null) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    parseMimeMapping(webXml, xPath, nodeList.item(i));
                }
            }
        } catch (XPathException xpe) {
            LOGGER.log(WARNING, "Unable to parse mime mappings", xpe);
        }
    }

    /**
     * Parse a servlet-mapping.
     *
     * @param xPath the XPath to use.
     * @param node the DOM node to parse.
     * @return the servlet-mapping, or null if an error occurred.
     */
    private void parseServletMapping(WebXml webXml, XPath xPath, Node node) {
        try {
            String servletName = parseString(xPath, "servlet-name/text()", node);
            String urlPattern = parseString(xPath, "url-pattern/text()", node);
            webXml.addServletMapping(servletName, urlPattern);
        } catch (XPathExpressionException xee) {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "Unable to parse servlet mapping", xee);
            }
        }
    }

    /**
     * Parse the servlet-mapping(s).
     *
     * @param webXml the web.xml to use.
     * @param xPath the XPath to use.
     * @param nodeList the Node list to parse.
     */
    private void parseServletMappings(WebXml webXml, XPath xPath, Node node) {
        try {
            NodeList nodeList = (NodeList) xPath.evaluate("//servlet-mapping", node, NODESET);
            if (nodeList != null) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    parseServletMapping(webXml, xPath, nodeList.item(i));
                }
            }
        } catch (XPathExpressionException xee) {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "Unable to parse servlet mappings", xee);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Processing
    // -------------------------------------------------------------------------
    /**
     * Process the mime mappings.
     *
     * @param webApplication the web application.
     */
    private void processMimeMappings(WebApplication webApplication) {
        Iterator<WebXmlMimeMapping> mappingIterator = webApplication.
                getWebXmlManager().getWebXml().getMimeMappings().iterator();
        while (mappingIterator.hasNext()) {
            WebXmlMimeMapping mapping = mappingIterator.next();
            webApplication.getMimeTypeManager()
                    .addMimeType(mapping.getExtension(), mapping.getMimeType());
        }
    }

    /**
     * Process the servlet mappings.
     *
     * @param webApplication the web application.
     */
    private void processServletMappings(WebApplication webApplication) {
        WebXml webXml = webApplication.getWebXmlManager().getWebXml();
        Iterator<WebXmlServletMapping> iterator = webXml.getServletMappings().iterator();
        while (iterator.hasNext()) {
            WebXmlServletMapping mapping = iterator.next();
            webApplication.addServletMapping(
                    mapping.getServletName(), mapping.getUrlPattern());
        }
    }
}
