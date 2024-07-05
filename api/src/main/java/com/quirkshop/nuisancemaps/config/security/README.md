# Authentication

Set an `ADMIN_API_KEY` env variable to use on non public API routes.

Cheap "global" auth.

Whitelist additional routes in `SecurityConfig`.

## Helpful References

https://www.baeldung.com/spring-boot-api-key-secret


## Notes

Getting `SecurityConfig`, and `AuthenticationFilter` classes to play nicely is a
nightmare when trying to `@Autowire` services. For example, when attempting to
use Annotation as a means to access env variables.

This looks deceptively simple but it's a minefield. Avoid D.I. these classes,
keep them POJO.

### Flow

* `SecurityConfig.SecurityFilterChain`: setup
  * `SecurityMatcher`: checks routes, whether to bypass or use `AuthenticationFilter`.
* `AuthenticationFilter`:
  * runs `AuthenticationService` code - checks for token match
  * on match, return `ApiKeyAuthentication` (`AbstractAuthenticationToken`) with `setAuthenticated(true)`
  * else throw error, set http status.
  * continue with `doFilter` stack.

#### SecurityConfig - AuthenticationFilter

* The `SecurityMatcher` enables/disables whether the request is passed to the
  `AuthenticationFilter`, given specified `RequestMatcher` conditions.

* Default behavior of a filter is that all requests pass through.

* The `authorizeHttpRequests` block - I'm not sure what's going on there. But
  any `requestMatchers(<pattern>").permitAll()` doesn't seem to catch. Hence
  relying on `SecurityMatcher`.

    * almost every example relies on the existence of a header to verify the
      route, not the authorized request path itself.
