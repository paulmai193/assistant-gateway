package logia.assistant.gateway.web.rest;

import logia.assistant.gateway.config.DefaultProfileUtil;

import io.github.jhipster.config.JHipsterProperties;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Resource to return information about the currently running Spring profiles.
 *
 * @author Dai Mai
 */
@RestController
@RequestMapping("/api")
public class ProfileInfoResource {

    /** The env. */
    private final Environment env;

    /** The j hipster properties. */
    private final JHipsterProperties jHipsterProperties;

    /**
     * Instantiates a new profile info resource.
     *
     * @param env the env
     * @param jHipsterProperties the j hipster properties
     */
    public ProfileInfoResource(Environment env, JHipsterProperties jHipsterProperties) {
        this.env = env;
        this.jHipsterProperties = jHipsterProperties;
    }

    /**
     * Gets the active profiles.
     *
     * @return the active profiles
     */
    @GetMapping("/profile-info")
    public ProfileInfoVM getActiveProfiles() {
        String[] activeProfiles = DefaultProfileUtil.getActiveProfiles(env);
        return new ProfileInfoVM(activeProfiles, getRibbonEnv(activeProfiles));
    }

    /**
     * Gets the ribbon env.
     *
     * @param activeProfiles the active profiles
     * @return the ribbon env
     */
    private String getRibbonEnv(String[] activeProfiles) {
        String[] displayOnActiveProfiles = jHipsterProperties.getRibbon().getDisplayOnActiveProfiles();
        if (displayOnActiveProfiles == null) {
            return null;
        }
        List<String> ribbonProfiles = new ArrayList<>(Arrays.asList(displayOnActiveProfiles));
        List<String> springBootProfiles = Arrays.asList(activeProfiles);
        ribbonProfiles.retainAll(springBootProfiles);
        if (!ribbonProfiles.isEmpty()) {
            return ribbonProfiles.get(0);
        }
        return null;
    }

    /**
     * The Class ProfileInfoVM.
     *
     * @author Dai Mai
     */
    class ProfileInfoVM {

        /** The active profiles. */
        private String[] activeProfiles;

        /** The ribbon env. */
        private String ribbonEnv;

        /**
         * Instantiates a new profile info VM.
         *
         * @param activeProfiles the active profiles
         * @param ribbonEnv the ribbon env
         */
        ProfileInfoVM(String[] activeProfiles, String ribbonEnv) {
            this.activeProfiles = activeProfiles;
            this.ribbonEnv = ribbonEnv;
        }

        /**
         * Gets the active profiles.
         *
         * @return the active profiles
         */
        public String[] getActiveProfiles() {
            return activeProfiles;
        }

        /**
         * Gets the ribbon env.
         *
         * @return the ribbon env
         */
        public String getRibbonEnv() {
            return ribbonEnv;
        }
    }
}
