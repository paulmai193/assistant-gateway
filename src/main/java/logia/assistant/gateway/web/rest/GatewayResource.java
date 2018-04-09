package logia.assistant.gateway.web.rest;

import logia.assistant.gateway.web.rest.vm.RouteVM;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing Gateway configuration.
 *
 * @author Dai Mai
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayResource {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(GatewayResource.class);

    /** The route locator. */
    private final RouteLocator routeLocator;

    /** The discovery client. */
    private final DiscoveryClient discoveryClient;

    /**
     * Instantiates a new gateway resource.
     *
     * @param routeLocator the route locator
     * @param discoveryClient the discovery client
     */
    public GatewayResource(RouteLocator routeLocator, DiscoveryClient discoveryClient) {
        this.routeLocator = routeLocator;
        this.discoveryClient = discoveryClient;
    }

    /**
     * GET  /routes : get the active routes.
     *
     * @return the ResponseEntity with status 200 (OK) and with body the list of routes
     */
    @GetMapping("/routes")
    @Timed
    public ResponseEntity<List<RouteVM>> activeRoutes() {
        List<Route> routes = routeLocator.getRoutes();
        List<RouteVM> routeVMs = new ArrayList<>();
        routes.forEach(route -> {
            RouteVM routeVM = new RouteVM();
            routeVM.setPath(route.getFullPath());
            routeVM.setServiceId(route.getId());
            routeVM.setServiceInstances(discoveryClient.getInstances(route.getLocation()));
            routeVMs.add(routeVM);
        });
        return new ResponseEntity<>(routeVMs, HttpStatus.OK);
    }
}
