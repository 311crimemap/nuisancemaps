# Ops

## Cloudflare Notes

Cloudflare is where we manage DNS; host and build the web frontend, and operate
several redirect operations.

### Domains

Since web assets are served by pages, our DNS settings deal with the API servers, and CNAMEs.

A: to servers

CNAME:

* `basemaps`: Pages build serving fork of protomaps assets, with additional font-awesome gyphs for icon categories.
* `staging`, `/`: branch-specific deploys to Pages builds.


### Pages Deploy

On deploy, pages generates a hash prefixed project on `<project-name>-pages.dev` domain.

There are also branch aliases where slashes are replaced with hyphens:
  * `topic/web/thisthat` -> `topic-web-thisthat.projectName-pages.dev`

Current build setup maps staging and production deploys to branch:
  * `deploy/production` -> `311crimemap.com`
  * `deploy/staging` -> `staging.311crimemap.com`


Build Setup: Pages runs `npm run build` - so tsc needs to compile properly
* `/www` subdirectory for npm project
* `/(www)/dist`: contains build artifacts


### Page Redirects

Using some template redirect rules:
  * http -> https
  * www -> root
    * Decided to serve frontend on root domain. Large reason is easier cookie and
      cert management.

Pages.dev redirect via "Bulk Redirect" - This is account root level, not project.

* set `<project>.pages.dev` to redirect project root domain


### Security -> WAF - firewall rules

These apply to entire domain (so both staging and prod) - unless hostname
indicates otherwise

Common rules:

`IP Gate`: Pre-deploy / mass block except local:
  * local ip, UT allow, but block everything else
  * TODO: - need to add hostname staging.*.311crimemap.com

* Common allows:
  * allow ACME cert-manager http-01 validation path: URI path contains `/.well-known/acme-challenge/`.
  * allow user-agent: Stripe/1.0 (+https://stripe.com/docs/webhooks)

e.g block rule: ip source != local ip AND uri_path does not contain cert path

* `block garbage` rule: URI contains OR: .php, wlwmanifest.xml, .env, wp-admin, wp-content, wp-includes


#### SSL/TLS Terraform Whitelist IP

Server firewalls need to whitelist cloudflare IP - e.g CF <-> Origin servers as
permitted ips.

See [Terraform firewall](infra/terraform/modules/firewall/main.tf), that pulls
and updates whitelist.



---

## Traefik: docker-compose / k3s

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
