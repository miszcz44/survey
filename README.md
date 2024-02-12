# Clean repo

---
# Swagger
http://localhost:8085/swagger-ui/index.html

---
# Postman
- path: resources/postman/..
- postman client: cookies -> Domains Allowlist -> localhost
- pre-request script:

```js
pm.cookies.jar().get('localhost', 'jwt', (err, cookie) => {
    if (cookie) {
        pm.request.headers.add({
            key: 'Authorization',
            value: 'Bearer ' + cookie
        });
    } else console.log('Jwt cookie not found');
});
```

---
# Files to update when creating new project
- CleanRepoApplication
- cleanrepo package name
- application.properties
- pom.xml
- Dockerfile
- docker-compose.yml

---
# Migrate to PostgreSQL/MySQL
- update application.properties (for local)
- update docker-compose.yml (for container/prod)
- update migrations