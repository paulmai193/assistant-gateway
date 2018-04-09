package logia.assistant.gateway.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.MetricsServlet;
import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.config.JHipsterProperties;
import io.github.jhipster.web.filter.CachingHttpHeadersFilter;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.xnio.OptionMap;

import javax.servlet.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the WebConfigurer class.
 *
 * @see WebConfigurer
 */
public class WebConfigurerTest {

    /** The web configurer. */
    private WebConfigurer webConfigurer;

    /** The servlet context. */
    private MockServletContext servletContext;

    /** The env. */
    private MockEnvironment env;

    /** The props. */
    private JHipsterProperties props;

    /** The metric registry. */
    private MetricRegistry metricRegistry;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        servletContext = spy(new MockServletContext());
        doReturn(new MockFilterRegistration())
            .when(servletContext).addFilter(anyString(), any(Filter.class));
        doReturn(new MockServletRegistration())
            .when(servletContext).addServlet(anyString(), any(Servlet.class));

        env = new MockEnvironment();
        props = new JHipsterProperties();

        webConfigurer = new WebConfigurer(env, props, null);
        metricRegistry = new MetricRegistry();
        webConfigurer.setMetricRegistry(metricRegistry);
    }

    /**
     * Test start up prod servlet context.
     *
     * @throws ServletException the servlet exception
     */
    @Test
    public void testStartUpProdServletContext() throws ServletException {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION);
        webConfigurer.onStartup(servletContext);

        assertThat(servletContext.getAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE)).isEqualTo(metricRegistry);
        assertThat(servletContext.getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry);
        verify(servletContext).addFilter(eq("webappMetricsFilter"), any(InstrumentedFilter.class));
        verify(servletContext).addServlet(eq("metricsServlet"), any(MetricsServlet.class));
        verify(servletContext).addFilter(eq("cachingHttpHeadersFilter"), any(CachingHttpHeadersFilter.class));
    }

    /**
     * Test start up dev servlet context.
     *
     * @throws ServletException the servlet exception
     */
    @Test
    public void testStartUpDevServletContext() throws ServletException {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT);
        webConfigurer.onStartup(servletContext);

        assertThat(servletContext.getAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE)).isEqualTo(metricRegistry);
        assertThat(servletContext.getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry);
        verify(servletContext).addFilter(eq("webappMetricsFilter"), any(InstrumentedFilter.class));
        verify(servletContext).addServlet(eq("metricsServlet"), any(MetricsServlet.class));
        verify(servletContext, never()).addFilter(eq("cachingHttpHeadersFilter"), any(CachingHttpHeadersFilter.class));
    }

    /**
     * Test customize servlet container.
     */
    @Test
    public void testCustomizeServletContainer() {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION);
        UndertowEmbeddedServletContainerFactory container = new UndertowEmbeddedServletContainerFactory();
        webConfigurer.customize(container);
        assertThat(container.getMimeMappings().get("abs")).isEqualTo("audio/x-mpeg");
        assertThat(container.getMimeMappings().get("html")).isEqualTo("text/html;charset=utf-8");
        assertThat(container.getMimeMappings().get("json")).isEqualTo("text/html;charset=utf-8");
        if (container.getDocumentRoot() != null) {
            assertThat(container.getDocumentRoot().getPath()).isEqualTo(FilenameUtils.separatorsToSystem("target/www"));
        }

        Builder builder = Undertow.builder();
        container.getBuilderCustomizers().forEach(c -> c.customize(builder));
        OptionMap.Builder serverOptions = (OptionMap.Builder) ReflectionTestUtils.getField(builder, "serverOptions");
        assertThat(serverOptions.getMap().get(UndertowOptions.ENABLE_HTTP2)).isNull();
    }

    /**
     * Test undertow http 2 enabled.
     */
    @Test
    public void testUndertowHttp2Enabled() {
        props.getHttp().setVersion(JHipsterProperties.Http.Version.V_2_0);
        UndertowEmbeddedServletContainerFactory container = new UndertowEmbeddedServletContainerFactory();
        webConfigurer.customize(container);
        Builder builder = Undertow.builder();
        container.getBuilderCustomizers().forEach(c -> c.customize(builder));
        OptionMap.Builder serverOptions = (OptionMap.Builder) ReflectionTestUtils.getField(builder, "serverOptions");
        assertThat(serverOptions.getMap().get(UndertowOptions.ENABLE_HTTP2)).isTrue();
    }

    /**
     * Test cors filter on api path.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCorsFilterOnApiPath() throws Exception {
        props.getCors().setAllowedOrigins(Collections.singletonList("*"));
        props.getCors().setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        props.getCors().setAllowedHeaders(Collections.singletonList("*"));
        props.getCors().setMaxAge(1800L);
        props.getCors().setAllowCredentials(true);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            options("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"))
            .andExpect(header().string(HttpHeaders.VARY, "Origin"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "1800"));

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "other.domain.com"));
    }

    /**
     * Test cors filter on other path.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCorsFilterOnOtherPath() throws Exception {
        props.getCors().setAllowedOrigins(Collections.singletonList("*"));
        props.getCors().setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        props.getCors().setAllowedHeaders(Collections.singletonList("*"));
        props.getCors().setMaxAge(1800L);
        props.getCors().setAllowCredentials(true);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            get("/test/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * Test cors filter deactivated.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCorsFilterDeactivated() throws Exception {
        props.getCors().setAllowedOrigins(null);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * Test cors filter deactivated 2.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCorsFilterDeactivated2() throws Exception {
        props.getCors().setAllowedOrigins(new ArrayList<>());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebConfigurerTestController())
            .addFilters(webConfigurer.corsFilter())
            .build();

        mockMvc.perform(
            get("/api/test-cors")
                .header(HttpHeaders.ORIGIN, "other.domain.com"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * The Class MockFilterRegistration.
     *
     * @author Dai Mai
     */
    static class MockFilterRegistration implements FilterRegistration, FilterRegistration.Dynamic {

        /* (non-Javadoc)
         * @see javax.servlet.FilterRegistration#addMappingForServletNames(java.util.EnumSet, boolean, java.lang.String[])
         */
        @Override
        public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.FilterRegistration#getServletNameMappings()
         */
        @Override
        public Collection<String> getServletNameMappings() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.FilterRegistration#addMappingForUrlPatterns(java.util.EnumSet, boolean, java.lang.String[])
         */
        @Override
        public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.FilterRegistration#getUrlPatternMappings()
         */
        @Override
        public Collection<String> getUrlPatternMappings() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration.Dynamic#setAsyncSupported(boolean)
         */
        @Override
        public void setAsyncSupported(boolean isAsyncSupported) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getName()
         */
        @Override
        public String getName() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getClassName()
         */
        @Override
        public String getClassName() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#setInitParameter(java.lang.String, java.lang.String)
         */
        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getInitParameter(java.lang.String)
         */
        @Override
        public String getInitParameter(String name) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#setInitParameters(java.util.Map)
         */
        @Override
        public Set<String> setInitParameters(Map<String, String> initParameters) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getInitParameters()
         */
        @Override
        public Map<String, String> getInitParameters() {
            return null;
        }
    }

    /**
     * The Class MockServletRegistration.
     *
     * @author Dai Mai
     */
    static class MockServletRegistration implements ServletRegistration, ServletRegistration.Dynamic {

        /* (non-Javadoc)
         * @see javax.servlet.ServletRegistration.Dynamic#setLoadOnStartup(int)
         */
        @Override
        public void setLoadOnStartup(int loadOnStartup) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRegistration.Dynamic#setServletSecurity(javax.servlet.ServletSecurityElement)
         */
        @Override
        public Set<String> setServletSecurity(ServletSecurityElement constraint) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRegistration.Dynamic#setMultipartConfig(javax.servlet.MultipartConfigElement)
         */
        @Override
        public void setMultipartConfig(MultipartConfigElement multipartConfig) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRegistration.Dynamic#setRunAsRole(java.lang.String)
         */
        @Override
        public void setRunAsRole(String roleName) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration.Dynamic#setAsyncSupported(boolean)
         */
        @Override
        public void setAsyncSupported(boolean isAsyncSupported) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRegistration#addMapping(java.lang.String[])
         */
        @Override
        public Set<String> addMapping(String... urlPatterns) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRegistration#getMappings()
         */
        @Override
        public Collection<String> getMappings() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRegistration#getRunAsRole()
         */
        @Override
        public String getRunAsRole() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getName()
         */
        @Override
        public String getName() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getClassName()
         */
        @Override
        public String getClassName() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#setInitParameter(java.lang.String, java.lang.String)
         */
        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getInitParameter(java.lang.String)
         */
        @Override
        public String getInitParameter(String name) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#setInitParameters(java.util.Map)
         */
        @Override
        public Set<String> setInitParameters(Map<String, String> initParameters) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.Registration#getInitParameters()
         */
        @Override
        public Map<String, String> getInitParameters() {
            return null;
        }
    }
}
