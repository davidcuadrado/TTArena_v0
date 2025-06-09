# TTArena - Sistema de Matchmaking con Redis y Gateway con Apisix

Este proyecto implementa un sistema de matchmaking distribuido utilizando Redis y un gateway API con Apache APISIX para la comunicación entre microservicios.

## Arquitectura

La arquitectura del sistema está compuesta por:

1. **Microservicios**: Conjunto de servicios independientes (user, auth, matchmaking, character, ability, map)
2. **Redis**: Sistema de mensajería y almacenamiento para el matchmaking distribuido
3. **Apache APISIX**: Gateway API para enrutar y proteger las comunicaciones entre microservicios

## Componentes Principales

### Sistema de Matchmaking con Redis

El sistema de matchmaking utiliza Redis para:
- Almacenar la cola de usuarios en espera de partida
- Publicar/suscribir eventos de emparejamiento
- Garantizar consistencia en entornos distribuidos con múltiples instancias

### Gateway con Apache APISIX

El gateway implementado con APISIX proporciona:
- Enrutamiento de peticiones a los microservicios correspondientes
- Autenticación y autorización mediante JWT y API keys
- Políticas CORS para acceso desde aplicaciones frontend
- Balanceo de carga entre múltiples instancias de microservicios

## Requisitos

- Java 23
- Docker y Docker Compose
- Gradle

## Configuración y Despliegue

### 1. Configuración de Redis y Microservicios

El archivo `docker-compose.yml` incluye la configuración necesaria para Redis y APISIX:

```bash
# Iniciar Redis y APISIX
docker-compose up -d
```

### 2. Compilación de Microservicios

```bash
./gradlew clean build
```

### 3. Ejecución de Microservicios

```bash
# Ejecutar cada microservicio en una terminal separada
java -jar matchmaking/build/libs/matchmaking-0.0.1-SNAPSHOT.jar
java -jar user/build/libs/user-0.0.1-SNAPSHOT.jar
# ... otros microservicios
```

## Uso del Sistema de Matchmaking

### Endpoints REST

El microservicio de matchmaking expone los siguientes endpoints:

- `POST /api/matchmaking/queue/{userId}`: Añadir usuario a la cola de matchmaking
- `DELETE /api/matchmaking/queue/{userId}`: Eliminar usuario de la cola
- `GET /api/matchmaking/queue/size`: Obtener tamaño actual de la cola
- `GET /api/matchmaking/queue/users`: Listar usuarios en la cola

### Acceso a través del Gateway

Todos los endpoints están disponibles a través del gateway APISIX:

```
http://localhost:9080/api/matchmaking/queue/{userId}
```

## Configuración de APISIX

### Rutas Configuradas

El gateway APISIX está configurado con las siguientes rutas:

1. `/api/matchmaking/*` → Microservicio de matchmaking (puerto 8082)
2. `/api/user/*` → Microservicio de usuario (puerto 8080)
3. `/api/auth/*` → Microservicio de autenticación (puerto 8081)
4. `/api/character/*` → Microservicio de personajes (puerto 8083)
5. `/api/ability/*` → Microservicio de habilidades (puerto 8084)
6. `/api/map/*` → Microservicio de mapas (puerto 8085)

### Seguridad

- Rutas de autenticación: Acceso público
- Rutas de matchmaking: Protegidas con API Key
- Otras rutas: Protegidas con JWT

## Flujo de Matchmaking

1. Los usuarios se conectan y envían eventos a través de Redis
2. El servicio de matchmaking procesa estos eventos y gestiona la cola en Redis
3. Cuando hay suficientes usuarios (2 por defecto), se crea una partida
4. Se publica un evento de partida encontrada a través de Redis
5. Los clientes reciben la notificación de partida encontrada

## Personalización

### Configuración de Redis

El archivo `application.yml` de cada microservicio contiene la configuración de Redis:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

### Configuración de APISIX

Los archivos de configuración de APISIX se encuentran en:
- `apisix/config.yaml`: Configuración general
- `apisix/apisix.yaml`: Definición de rutas y upstreams

## Solución de Problemas

### Redis no disponible

Si Redis no está disponible, el sistema de matchmaking funcionará en modo local (no distribuido).

### APISIX no disponible

Si APISIX no está disponible, los microservicios seguirán siendo accesibles directamente a través de sus puertos.

## Próximos Pasos

- Implementar sistema de métricas y monitoreo
- Añadir más criterios de matchmaking (nivel, región, etc.)
- Implementar sistema de partidas persistentes
