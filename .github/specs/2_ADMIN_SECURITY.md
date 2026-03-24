# Security for the Admin Dashboard

Currently the admin panel and dashboards are served under `/admin/**` without
any security. This document specifies the authentication and authorization
requirements to protect those paths using Spring Security.

---

## Functional Requirements

- Any request to `/admin/**` from an unauthenticated user must be redirected
  to the login page at `/admin/login`.
- Only users with the role `ADMIN` may access the admin dashboard. A
  successfully authenticated user without that role must be redirected to
  `/admin/login?error=forbidden` (do **not** return a raw `403` to the
  browser).
- The login page presents a form (username + password). On success, redirect
  the user to the page they originally requested, or to `/admin/galleries` as
  the default.
- A logout action must invalidate the session and any remember-me cookie, then
  redirect to `/admin/login?logout`.
- **30-day persistent session**: use Spring Security's `rememberMe()` with
  hash-based tokens (no extra DB table required). The login form must include
  a `remember-me` checkbox. Set `tokenValiditySeconds` to `2592000` (30 days).
  Standard HTTP session timeout alone is not sufficient because sessions do not
  survive browser restarts or server restarts.
- No user registration or password reset is needed at this time.

---

## UI Requirements

- The login page must use a **standalone Thymeleaf layout** (not the existing
  `admin/layout.html`, which has a navbar with protected links). Keep the same
  visual language: Bootstrap 5, light/dark theme support, responsive layout.
- The login form must have fields for `username` and `password`, a
  `remember-me` checkbox, a submit button, and a clear error message area for
  failed attempts.
- The existing `admin/layout.html` navbar must show a **Logout** button only
  when the user is authenticated. Use the Thymeleaf Spring Security extras
  dialect (`sec:authorize="isAuthenticated()"`) to conditionally render it.
  The logout button must submit a POST to `/admin/logout` (Spring Security
  requires POST for logout by default, which also handles CSRF).

---

## Technical Requirements

### New dependencies (`build.gradle`)

Add the following to the `dependencies` block:

```groovy
// Spring Security
implementation 'org.springframework.boot:spring-boot-starter-security'

// Thymeleaf Spring Security extras (for sec:authorize in templates)
implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'
```

> Spring Boot 4.x manages versions for both; no explicit version is needed.

---

### Security configuration scope

Create `AdminSecurityConfig` under
`me.giobyte8.galleries.security`. This config must:

- Protect only `/admin/**` (login page itself excluded).
- **Leave `/api/**` completely unaffected** — the REST API must remain
  accessible without authentication.
- **Leave `/actuator/**` unaffected** — keep existing actuator access
  behaviour.
- Use the **lambda-style DSL** (mandatory in Spring Security 7 / Spring Boot
  4).

Sketch of the security filter chain:

```java
@Bean
public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/admin/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/login").permitAll()
            .anyRequest().hasRole("ADMIN")
        )
        .formLogin(form -> form
            .loginPage("/admin/login")
            .loginProcessingUrl("/admin/login")
            .defaultSuccessUrl("/admin/galleries", false)
            .failureUrl("/admin/login?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/admin/logout")
            .logoutSuccessUrl("/admin/login?logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
        )
        .rememberMe(rm -> rm
                .key(adminSecurityProps.rememberMeKey())
                .tokenValiditySeconds(2592000)         // 30 days
                .rememberMeParameter("remember-me")
        )
        .exceptionHandling(ex -> ex
            .accessDeniedHandler((req, res, exc) ->
                res.sendRedirect("/admin/login?error=forbidden"))
        );
    return http.build();
}
```

### Remember-me key configuration (application.yml)
- Add a new property under galleries.security:
  `galleries.security.remember_me_key`
- Read it from environment for production readiness, example:
```yml
galleries:
  security:
    remember_me_key: ${GL_REMEMBER_ME_KEY:dev-unsafe-change-me}
```
- Bind it through a typed config class (e.g. AdminSecurityProps) and inject
  it into AdminSecurityConfig. Document that production must set a strong
  non-default value.

---

### CSRF and HTMX compatibility

Spring Security enables CSRF protection by default. The existing "Scan now"
button uses `hx-post`, and any future HTMX `POST`/`PUT`/`DELETE` requests
will receive a `403` without a valid CSRF token.

**Required fix:** inject the CSRF token into every outgoing HTMX request via
a meta-tag + JavaScript listener in `admin/layout.html`:

```html
<!-- In <head> — inject CSRF token for HTMX -->
<meta name="_csrf"        th:content="${_csrf.token}">
<meta name="_csrf_header" th:content="${_csrf.headerName}">

<!-- In <script> block — attach token to all HTMX requests -->
document.addEventListener('htmx:configRequest', (evt) => {
    const csrf       = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    if (csrf && csrfHeader) {
        evt.detail.headers[csrfHeader.content] = csrf.content;
    }
});
```

Do **not** disable CSRF globally.

---

### User entity and repository

Add a `User` entity to `me.giobyte8.galleries.persistence.models` (same
package as `Directory`, `Image`, `ScanStats`) to stay consistent with the
existing model layer:

```
@Node("AppUser")
public class User {
    @Id @GeneratedValue  UUID id;
    String username;          // unique
    String password;          // BCrypt-hashed
    List<String> roles;       // e.g. ["ROLE_ADMIN"]
    boolean enabled;
    Long version;             // @Version for optimistic locking
}
```

> Use `@Node("AppUser")` to avoid a label name collision with Neo4j's own
> internal `User` label.

Add `UserRepository` to
`me.giobyte8.galleries.persistence.repositories`, extending
`CrudRepository<User, UUID>` with a `findByUsername(String username)`
query method.

---

### UserDetailsService

Create `AdminUserDetailsService` under
`me.giobyte8.galleries.security`. It must implement
`org.springframework.security.core.userdetails.UserDetailsService` and load
a `User` from `UserRepository` by username, converting it to a Spring
Security `UserDetails` object. Wire it into `AdminSecurityConfig` via a
`DaoAuthenticationProvider` configured with `BCryptPasswordEncoder`.

---

### Login page template

Create a **standalone** Thymeleaf template at
`src/main/resources/templates/admin/login.html`. It must **not** use the
existing `admin/layout.html` fragment (that layout assumes an authenticated
user). The login page should:

- Load Bootstrap 5 and Bootstrap Icons from CDN (same versions as
  `admin/layout.html`).
- Include Alpine.js for the light/dark theme toggle (same `themeManager()`
  logic as `admin/layout.html` — copy the script block).
- Display a card-centered form with username, password, remember-me checkbox,
  and a submit button.
- Show an error message when param.error is present. 
- If param.error == 'forbidden', show a specific "You are not authorized to
  access admin resources" message.
- Show a success/logout message when param.logout is present.

Add a `LoginController` under `me.giobyte8.galleries.admin.controllers`
that maps `GET /admin/login` and passes any `error` / `logout`
query params to the model. Spring Security handles the `POST /admin/login`
processing itself.

---

### Code structure summary

| Artefact | Package / Location |
|---|---|
| `AdminSecurityConfig` | `me.giobyte8.galleries.security` |
| `AdminUserDetailsService` | `me.giobyte8.galleries.security` |
| `User` (entity) | `me.giobyte8.galleries.persistence.models` |
| `UserRepository` | `me.giobyte8.galleries.persistence.repositories` |
| `LoginController` | `me.giobyte8.galleries.admin.controllers` |
| `login.html` (template) | `src/main/resources/templates/admin/` |

---

### Development utilities

**`scripts/encrypt_password.sh`**

Create this script that accepts a plaintext password as `$1` and prints the
BCrypt hash. Use Python's `bcrypt` library (pip-installable) since standard
bash has no BCrypt support:

```bash
#!/usr/bin/env bash
# Usage: ./encrypt_password.sh <plaintext-password>
python -c "import bcrypt, sys; \
    print(bcrypt.hashpw(sys.argv[1].encode(), bcrypt.gensalt()).decode())" "$1"
```

Set `+x` permission: `chmod +x scripts/encrypt_password.sh`.

**`scripts/dev_data_reset.cypher`**

Add a `MERGE` for a development admin user at the end of the existing reset
script. Use the label `AppUser` to match the entity's `@Node` annotation.
Use `'dev_admin'` as the username and a placeholder BCrypt hash for
`'password'` as the initial value (to be replaced by running
`encrypt_password.sh`):

```cypher
MERGE (u:AppUser {
    id: randomUUID(),
    username: 'dev_admin',
    password: '$2a$10$REPLACE_WITH_OUTPUT_OF_encrypt_password_sh',
    roles: ['ROLE_ADMIN'],
    enabled: true,
    version: 0
});
```

---

## Documentation update (`docs/DEVELOPMENT.md`)

Add a new section **"Setup development users"** inside _Local Development_,
immediately after the existing _"Setup application properties"_ section.

The section should cover two steps:

1. Run `scripts/encrypt_password.sh password` to generate a BCrypt hash for
   the placeholder password.
2. Replace the `password` field value in `scripts/dev_data_reset.cypher` with
   the generated hash, then run the Cypher script against the dev database to
   create the `dev_admin` user.
