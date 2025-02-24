# Traefik Notes

Traefik primarily used for compression middleware (needed for large json
responses.)

Slight differences when deployed using k3s or in local dev env via
docker-compose.yml.

## Traefik: k3s

1. Add compression middleware resources:

```
apiVersion: traefik.containo.us/v1alpha1
kind: Middleware
metadata:
  name: compress-middleware
spec:
  compress: {}
```

2. add annotation to ingress controller:

`traefik.ingress.kubernetes.io/router.middlewares: default-compress-middleware@kubernetescrd`

Note the name of the middleware here is `compress-middleware`, but because its
an annotation we need to combine the `default` namespace to its identifier, to
make `default-compress-middleware`


## Docker Compose

## Traefik Routing Summary

Traefik mapping to Service:

* Create `entrypoint` and address port at `command`:
   * `--entrypoints.<entryPointName>.address = :<entryPointPort>.`
* Docker:  host port : docker container - the container port map to the `:<entryPointPort>` above


Service

*  labels, assign the router the `entryPointName`:
   * `traefik.http.routers.api.entrypoints=apiEntryPoint`
*  label point the loadbalancer to the backend port:
   * `traefik.http.services.api.loadbalancer.server.port=8080`


Service no longer needs any exposed ports, as traffic hits Traefik, and is
routed internally with command/label configuration.

* Service: labeled with `traefik.http.routers.api.entrypoints=apiEntryPoint` (name)


### Traefik Notes

Traefik commands and labels work internally, can bypass docker-compose port
settings, but also makes it kind of confusing.

Traefik commands:

* `traefik.command.entryPoints.apiEntryPoint.address=:8000`
  * Creates an entry point, `apiEntryPoint`. This configures traefik (container)
    to listen to port `8000`.
  * Docker port mapping: `8080:8000` -> maps host traffic to container port
    `8000`, where Traefik is now listening.
  * This traffic gets routed to the corresponding service with the same labeled
    router entrypoint.

The port address for `apiEntryPoint` is arbitrary and internal, as long as
Traefik container maps to it, and service is assigned the entryPoint, it will
flow through to service without needing to be exposed.

API Service Labels - assign an entrypoint / router

* api.labels:
  * `traefik.enable=true`
  * `traefik.http.routers.api.entrypoints=apiEntryPoint`: router entry point, `apiEntryPoint`
  * `traefik.http.services.api.loadbalancer.server.port=8080`: forward to
    backend api container running on `8080` (aka tomcat).

Note api service has no public exposed port mapping, traefik takes over the internal routing.


#### Dashboard

Dashboard defaults to port `8080`; configuration below moves it to `8081`.

However, dashboard is separate from default Traefik entrypoint, which is set to
`8080`, and can conflict with any container port assignments.

This can be re-assigned via `--entrypoints.traefik.address=:9999` or whatever
other port.

```
command:
  - "--entrypoints.dashboard.address=:8081"
labels:
  # need to enable dashboard on custom port
  - "traefik.enable=true"
  - "traefik.http.routers.traefik-dashboard.rule=Host(`localhost`)"  # Rule for accessing the dashboard
  - "traefik.http.routers.traefik-dashboard.entrypoints=dashboard"   # Use the "dashboard entry point
  - "traefik.http.routers.traefik-dashboard.service=api@internal"    # Service name for Traefik's internal API

```


#### Compression

* configure middleware on traefik service:
  `"traefik.http.middlewares.api-compression. ``compress=true"`

* attach to service:
  `"traefik.http.routers.api.middlewares=api-compression@docker"`

Note name in this case: `api-compression`

