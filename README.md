# kotlin-todo-app

### Tech
- Kotlin
- SparkJava
- KotlinQuery
- HikariCP
- Fuel
- H2 DB

## Docker Container
#### Docker Commands
- Run: `docker-compose up`
- Run detached: `docker-compose up -d`
- Rebuild container: `docker-compose up --build`
- Bring down container: `docker-compose down`
- Stop running process and clear volumes: `docker-compose down --remove-orphans --volumes`
- Get IP address of docker container: `docker inspect <container_id> | grep IPAddress` -> hostname
- Remove dangling images: `docker system prune`
- Remove all unused images: `docker system prune -a`
- Remove all unused images and volumes: `docker system prune -a --volumes`