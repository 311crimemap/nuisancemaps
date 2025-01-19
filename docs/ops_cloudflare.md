# Cloudflare Notes

Cloudflare manages DNS; hosts and builds the web frontend, and operate several
redirect operations and page block rules.

### Domains

Since web assets are served by pages, our DNS settings deal with the API
servers, and CNAMEs.

A: to servers

CNAME:

* `basemaps`: Pages build serving fork of protomaps assets, with additional
  font-awesome gyphs for icon categories.
* `staging`, `/`: branch-specific deploys to Pages builds.


### Pages Deploy

On deploy, pages generates a hash prefixed project on `<project-name>-pages.dev`
domain.

There are also branch aliases where slashes are replaced with hyphens:
  * `topic/web/thisthat` -> `topic-web-thisthat.projectName-pages.dev`

Current build setup maps staging and production deploys to branch:
  * `deploy/production` -> `311crimemap.com`
  * `deploy/staging` -> `staging.311crimemap.com`


Build Setup: Pages runs `npm run build` - ensure that `tsc` compiles
* `/www` sub-directory for npm project
* `/(www)/dist`: contains build artifacts


### Page Redirects

Using some template redirect rules:
  * http -> https
  * www -> root
    * Decided to serve frontend on root domain. Large reason is easier cookie
      and cert management.

Pages.dev redirect via "Bulk Redirect" - This is account root level, not
project.

* set `<project>.pages.dev` to redirect project root domain


### Security -> WAF - firewall rules

These apply to entire domain (so both staging and prod) - unless hostname
indicates otherwise

Common rules:

`IP Gate`: Pre-deploy / mass block except local:
  * local ip, but block everything else

* Common allows:
  * allow ACME cert-manager http-01 validation path: URI path contains
    `/.well-known/acme-challenge/`.
  * allow user-agent: Stripe/1.0 (+https://stripe.com/docs/webhooks)

e.g block rule: ip source != local ip AND uri_path does not contain cert path

* `block garbage` rule: URI contains OR: .php, wlwmanifest.xml, .env, wp-admin,
  wp-content, wp-includes


#### SSL/TLS Terraform Whitelist IP

Server firewalls need to whitelist cloudflare IP - e.g CF <-> Origin servers as
permitted ips.

See [Terraform firewall](infra/terraform/modules/firewall/main.tf), that pulls
and updates whitelist.
