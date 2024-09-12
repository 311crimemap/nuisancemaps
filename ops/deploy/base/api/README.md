# Ingress notes

ingress is split into production / staging environments.

This is because different environments will have different challenge tokens, and
different certs.

Certs are now "production" level requests for both 311crimemap production and
staging environments; api and staging-api domains.
