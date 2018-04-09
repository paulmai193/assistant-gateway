package logia.assistant.gateway.web.rest.vm;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;

/**
 * View Model that stores a route managed by the Gateway.
 *
 * @author Dai Mai
 */
public class RouteVM {

    /** The path. */
    private String path;

    /** The service id. */
    private String serviceId;

    /** The service instances. */
    private List<ServiceInstance> serviceInstances;

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     *
     * @param path the new path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the service id.
     *
     * @return the service id
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the service id.
     *
     * @param serviceId the new service id
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Gets the service instances.
     *
     * @return the service instances
     */
    public List<ServiceInstance> getServiceInstances() {
        return serviceInstances;
    }

    /**
     * Sets the service instances.
     *
     * @param serviceInstances the new service instances
     */
    public void setServiceInstances(List<ServiceInstance> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }
}
