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


## Pushing API to dockerhub
- Build image for kubernetes: `docker build -t kotlin-todo-app .`
- Tag image to repo on dockerhub: `docker tag <image-name> <dockerhub-username>/<repository-name>:<tag-name>`
  - ie. `docker tag kotlin-todo-app dmdinh/kotlin-todo-app:X.X.X`
- Push up to dockerhub: `docker push <dockerhub-username>/<respository-name>:<tag-name>`
  - ie. `docker push dmdinh/kotlin-todo-app:1.0.0`

#### MiniKube (local kubernetes)
- spin up: `minikube start`
- spin down: `minikube stop`

#### Deploying API to Kubernetes via MiniKube
- Create and apply kubernetes from the yaml files:
```
kubectl apply -f kotlin-todo-app-deployment.yaml
kubectl apply -f kotlin-todo-app-service.yaml
```

- Check pod status: `kubectl get pods`
- Get services in cluster: `kubectl get services`
- Get url exposed: `minikube service <service-name> --url` (`minikube service kotlin-todo-app --url`)
