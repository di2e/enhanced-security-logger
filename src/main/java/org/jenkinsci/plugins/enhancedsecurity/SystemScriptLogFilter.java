package org.jenkinsci.plugins.enhancedsecurity;

import com.google.inject.Injector;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import jenkins.model.Jenkins;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Logs calls to the Jenkins script endpoint.
 * Structure was inspired by the CORS Filter Plugin - https://github.com/jenkinsci/cors-filter-plugin/blob/master/src/main/java/org/jenkinsci/plugins/corsfilter/AccessControlsFilter.java
 */
@Extension
public class SystemScriptLogFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(SystemScriptLogFilter.class.getName());

    private static final String SCRIPT_PATH = "/script";
    // script content is sent via POST form
    private static final String SCRIPT_METHOD = "POST";
    private static final String SCRIPT_PARAM = "script";

    @Initializer(after = InitMilestone.JOB_LOADED)
    public static void init() throws ServletException {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) {
            Injector injector = jenkins.getInjector();
            if (injector != null) {
                LOGGER.fine("Adding System Script Log filter to system filter chain.");
                PluginServletFilter.addFilter(injector.getInstance(SystemScriptLogFilter.class));
                LOGGER.fine("System Script Log filter was added to system filter chain.");
            } else {
                LOGGER.warning("Injector was null, cannot add script log filter to system filter chain.");
            }
        } else {
            LOGGER.warning("Could not add script log filter to system due to issues with getting Jenkins instance.");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
            if (httpServletRequest.getPathInfo().endsWith(SCRIPT_PATH) && httpServletRequest.getMethod().equals(SCRIPT_METHOD)) {
                LOGGER.warning("--START SCRIPT BEING RUN ON ADMIN SCRIPT CONSOLE--\n"
                        + httpServletRequest.getParameter(SCRIPT_PARAM)
                        + "\n--END SCRIPT BEING RUN ON ADMIN SCRIPT CONSOLE--");
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}
