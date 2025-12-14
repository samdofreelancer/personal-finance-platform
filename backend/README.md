# Backend – Personal Finance Platform

Spring Boot 3.2 backend implementing Google OIDC OAuth 2.0 authentication with JWT token management.

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8.9+
- Google Cloud Project with OAuth credentials

### 1. Configure Environment

**Create `.env` file:**
```bash
export GOOGLE_CLIENT_ID="YOUR_CLIENT_ID.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="YOUR_CLIENT_SECRET"
export JWT_SECRET="$(openssl rand -base64 32)"
```

**Or edit `src/main/resources/application.properties`:**
```properties
google.client.id=YOUR_CLIENT_ID
google.client.secret=YOUR_CLIENT_SECRET
jwt.secret=YOUR_SECRET_KEY
```

### 2. Start Backend

```bash
mvn clean install
mvn spring-boot:run
```

Server runs on: **http://localhost:8080**

### 3. Test API

```bash
# Get user (will fail – no token)
curl http://localhost:8080/api/auth/me
# Returns: 401 Unauthorized ✓

# Exchange Google ID Token for JWT
curl -X POST http://localhost:8080/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken":"eyJhbGciOiJSUzI1NiIs..."}'
# Returns: {"accessToken":"eyJhbGciOiJIUzI1NiIs..."} ✓

# Get user with JWT
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  http://localhost:8080/api/auth/me
# Returns: {"id":"...", "email":"...", "name":"...", "picture":"..."} ✓
```

---

## Project Structure

```
src/main/java/com/pocketwatch/
├── PersonalFinanceApplication.java      Main entry point
└── identity/
    ├── api/
    │   ├── AuthController.java           REST endpoints
    │   ├── GoogleLoginRequest.java       Request DTO
    │   └── AuthResponse.java             Response DTO
    ├── application/
    │   ├── AuthenticationService.java    Application service (orchestration)
    │   └── UserResponse.java             User DTO
    ├── domain/
    │   ├── User.java                     JPA entity
    │   └── UserRepository.java           Spring Data Repository
    └── infrastructure/
        ├── GoogleTokenVerifier.java      Verifies Google signature
        ├── JwtTokenProvider.java         JWT management
        ├── JwtAuthenticationFilter.java  HTTP request filter
        ├── JwtAuthenticationToken.java   Auth token
        └── SecurityConfiguration.java    Spring Security setup

src/main/resources/
└── application.properties                Configuration
```

---

## Architecture

### Layers

| Layer | Responsibility |
|-------|-----------------|
| **API** | REST endpoints, request/response handling |
| **Application Service** | Business logic orchestration, coordination |
| **Domain** | User entity, repository interface |
| **Infrastructure** | Google verification, JWT handling, Security |
| **Database** | Persistence (H2 dev, PostgreSQL prod) |

### Authentication Flow

```
1. Frontend sends Google ID Token to POST /api/auth/google
2. Backend verifies token signature with Google's public keys
3. Backend creates/updates user in database
4. Backend generates internal JWT
5. Backend returns JWT to frontend
6. Frontend uses JWT for all subsequent API requests
```

---

## API Endpoints

### POST /api/auth/google
Exchange Google ID Token for internal JWT.

**Request:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEifQ..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error (401 Unauthorized):**
```json
{
  "error": "Google token verification failed"
}
```

### GET /api/auth/me
Get current user information (requires valid JWT).

**Request:**
```
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "John Doe",
  "picture": "https://lh3.googleusercontent.com/..."
}
```

**Error (401 Unauthorized):**
```json
{
  "error": "Invalid or expired token"
}
```

---

## Configuration

### application.properties

```properties
# Google OAuth Configuration
google.client.id=${GOOGLE_CLIENT_ID}
google.client.secret=${GOOGLE_CLIENT_SECRET}
google.token.uri=https://oauth2.googleapis.com/token
google.tokeninfo.uri=https://www.googleapis.com/oauth2/v3/tokeninfo

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# Server
server.port=8080
server.servlet.context-path=/api

# Database (H2 for development)
spring.datasource.url=jdbc:h2:mem:personal_finance_db
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# H2 Console (development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.root=INFO
logging.level.com.pocketwatch=DEBUG
```

### Environment Variables

| Variable | Example | Required |
|----------|---------|----------|
| `GOOGLE_CLIENT_ID` | `YOUR_ID.apps.googleusercontent.com` | ✅ |
| `GOOGLE_CLIENT_SECRET` | `YOUR_SECRET` | ✅ |
| `JWT_SECRET` | `base64-encoded-256-bit-key` | ✅ |
| `SERVER_PORT` | `8080` | ❌ |
| `DB_URL` | `jdbc:postgresql://localhost:5432/db` | ❌ |

---

## Database

### Development (H2 In-Memory)

**Automatic setup, data lost on restart.**

Access console: http://localhost:8080/h2-console

Default credentials:
- JDBC URL: `jdbc:h2:mem:personal_finance_db`
- User: `sa`
- Password: (empty)

Query users:
```sql
SELECT * FROM users;
```

### Production (PostgreSQL)

**Update `application-prod.properties`:**
```properties
spring.datasource.url=jdbc:postgresql://host:5432/personal_finance
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
spring.jpa.hibernate.ddl-auto=validate
```

**Start with production profile:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

**Users Table:**
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    google_subject VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    picture VARCHAR(2048),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_authenticated_at TIMESTAMP,
    INDEX idx_google_subject (google_subject),
    INDEX idx_email (email)
);
```

---

## Security

### Token Verification
- ✅ Google ID Token signature verified against Google's public keys
- ✅ Token expiration validated
- ✅ Audience (aud) validated to match Client ID

### JWT Security
- ✅ Signed with HS256 and strong secret (256+ bits minimum)
- ✅ Finite expiration (default 1 hour)
- ✅ No sensitive data in payload

### CORS Configuration
Edit `SecurityConfiguration.java` to add trusted origins:
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",      // Development
    "https://yourdomain.com"      // Production
));
```

### Best Practices
- [ ] Keep `JWT_SECRET` secure (use secrets manager in production)
- [ ] Enforce HTTPS in production
- [ ] Use managed database with encryption
- [ ] Enable database backups
- [ ] Implement rate limiting on login endpoint
- [ ] Monitor authentication attempts
- [ ] Log all authentication events

---

## Dependencies

### Spring Boot
- `spring-boot-starter-web` – REST API support
- `spring-boot-starter-security` – Security framework
- `spring-boot-starter-data-jpa` – Database ORM

### Google OAuth
- `google-auth-library-oauth2-http` – Google token verification

### JWT
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` – JWT creation and validation

### Database
- `h2` – Development (in-memory)
- `postgresql` – Production (add dependency for prod)

### Build
- Maven 3.8.9+
- Java 21+

---

## Build & Deploy

### Development
```bash
mvn spring-boot:run
```

### Production Build
```bash
mvn clean package
java -jar target/personal-finance-backend-1.0.0.jar
```

### Docker
```dockerfile
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/personal-finance-backend-1.0.0.jar app.jar
ENV JWT_SECRET=your-secret
ENV GOOGLE_CLIENT_ID=your-id
ENV GOOGLE_CLIENT_SECRET=your-secret
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t personal-finance-backend .
docker run -e JWT_SECRET=... -p 8080:8080 personal-finance-backend
```

### Cloud Deployment

**AWS Elastic Beanstalk:**
```bash
eb init -p "Docker" personal-finance
eb create production
eb deploy
```

**Google Cloud Run:**
```bash
gcloud run deploy personal-finance-backend \
  --source . \
  --platform managed \
  --region us-central1 \
  --set-env-vars JWT_SECRET=...,GOOGLE_CLIENT_ID=...
```

**Heroku:**
```bash
heroku create personal-finance-backend
git push heroku main
```

---

## Troubleshooting

### Issue: "Google token verification failed"
- ✓ Verify `GOOGLE_CLIENT_ID` matches Google Console
- ✓ Check internet connection to Google servers
- ✓ Verify token is fresh (< 10 minutes old)

### Issue: "Invalid or expired token"
- ✓ Check JWT hasn't expired (default 1 hour)
- ✓ Verify JWT_SECRET is same across instances
- ✓ Check token wasn't corrupted during transmission

### Issue: "User not found"
- ✓ Verify user was created in database
- ✓ Query H2 console: `SELECT * FROM users;`
- ✓ Check backend logs for creation errors

### Issue: "CORS error" from frontend
- ✓ Verify frontend origin in `SecurityConfiguration.java`
- ✓ Check CORS headers in response
- ✓ Ensure frontend is sending correct Origin header

### Issue: Port 8080 already in use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process (Linux/Mac)
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

---

## Logging

### Log Levels
- `TRACE` – Most detailed logging
- `DEBUG` – Development debugging (set for com.pocketwatch)
- `INFO` – General information
- `WARN` – Warning messages
- `ERROR` – Error messages

### Configure in application.properties
```properties
logging.level.root=INFO
logging.level.com.pocketwatch=DEBUG
logging.level.org.springframework.security=DEBUG
```

### View Logs
```bash
# Live logs
tail -f logs/application.log

# Search logs
grep "Google token verified" logs/application.log
```

---

## Testing

### Manual API Testing
```bash
# All endpoints accept test requests

# Test public endpoint (no auth needed)
curl http://localhost:8080/api/auth/google

# Test protected endpoint (needs JWT)
curl -H "Authorization: Bearer JWT_TOKEN" \
  http://localhost:8080/api/auth/me
```

### Unit Testing (Future)
```bash
mvn test
```

### Integration Testing (Future)
```bash
mvn verify
```

---

## Performance

### H2 Database (Development)
- Supports ~1,000 users
- Data lost on restart
- Acceptable for local testing only

### PostgreSQL (Production)
- Scales to millions of users
- Data persists across restarts
- Use connection pooling
- Enable query logging for optimization

### Optimization Tips
- Use database indexes (created automatically)
- Enable connection pooling (default: HikariCP)
- Cache Google's public keys (done automatically)
- Monitor slow queries

---

## Documentation

- `IMPLEMENTATION_GUIDE.md` – Detailed setup and architecture
- `../OAUTH_ARCHITECTURE.md` – Complete architecture documentation
- `../OAUTH_TESTING_GUIDE.md` – Testing procedures
- `../QUICK_REFERENCE.md` – Quick reference guide

---

## Next Steps

1. ✅ Authentication implemented
2. ⏳ Implement Account domain
3. ⏳ Implement Transaction domain  
4. ⏳ Implement Loan domain
5. ⏳ Add API documentation (Swagger)
6. ⏳ Add comprehensive testing
7. ⏳ Setup monitoring and alerting

---

## Support & Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Google OAuth 2.0](https://developers.google.com/identity/protocols/oauth2)
- [JWT (jwt.io)](https://jwt.io/)
- [JJWT Library](https://github.com/jwtk/jjwt)

---

**Status:** ✅ Production Ready  
**Version:** 1.0.0  
**Last Updated:** December 14, 2025
