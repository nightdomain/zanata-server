/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.util;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@AutoCreate
@Name("commonMarkRenderer")
@Scope(ScopeType.APPLICATION)
@Slf4j
public class CommonMarkRenderer {

    private static final String VER = Dependencies.getVersion(
            "org.webjars.bower:commonmark:jar");
    private static final String SCRIPT_NAME = "commonmark/" +
            VER + "/dist/commonmark.js";
    private static final String RESOURCE_NAME = "META-INF/resources/webjars/" +
            SCRIPT_NAME;

    public CommonMarkRenderer() {
        log.info("Using commonmark.js version {}", VER);
    }

    // ScriptEngine is expensive, but not really threadsafe (even in Rhino).
    // However, they are quite large, so it's probably not worth keeping
    // one for every thread.  Instead, we keep one around, and synchronise
    // access to it.
    // Please ensure any methods which use this Invocable are synchronized!
    private Invocable invocable = getInvocable();

    /**
     * Return the name of the implementation script for JSF h:outputScript.
     *
     * You can use it like this:
     * <pre>
     * {@code <h:outputScript target="body" library="webjars"
     *     name="${commonMarkRenderer.outputScriptName}"/>}
     * </pre>
     * @return
     */
    public String getOutputScriptName() {
        return SCRIPT_NAME;
    }

    /**
     * Render CommonMark text to HTML and sanitise it.
     * @param commonMark
     * @return
     */
    public String renderToHtmlSafe(String commonMark) {
        String unsafeHtml = renderToHtmlUnsafe(commonMark);
        return HtmlUtil.SANITIZER.sanitize(unsafeHtml);
    }

    public synchronized String renderToHtmlUnsafe(String commonMark) {
        try {
            return (String) invocable.invokeFunction("mdRender", commonMark);
        } catch (ScriptException | NoSuchMethodException e) {
            throw Throwables.propagate(e);
        }
    }

    private Invocable getInvocable() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getScriptAsStream(), StandardCharsets.UTF_8))) {

            ScriptEngine engine = newEngine();
            engine.eval("window = this;");
            engine.eval(reader);
            // Create a javascript function 'mdRender' which takes CommonMark
            // as a string and returns a rendered HTML string:
            String initScript =
                    "var reader = new commonmark.Parser();" +
                            "var writer = new commonmark.HtmlRenderer();" +
                            "function mdRender(src) {" +
                            "  return writer.render(reader.parse(src));" +
                            "};";
            engine.eval(initScript);
            return (Invocable) engine;
        } catch (IOException | ScriptException e) {
            throw Throwables.propagate(e);
        }
    }

    private InputStream getScriptAsStream() {
        InputStream stream =
                getClass().getClassLoader().getResourceAsStream(
                        RESOURCE_NAME);
        if (stream == null) {
            throw new RuntimeException("Script "+ RESOURCE_NAME + " not found");
        }
        return stream;
    }

    private ScriptEngine newEngine() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine =
                scriptEngineManager.getEngineByName("rhino17R5");
        if (engine == null) {
            log.warn("Falling back on generic JavaScript engine");
            engine = scriptEngineManager.getEngineByName("JavaScript");
        }
        return engine;
    }

}
