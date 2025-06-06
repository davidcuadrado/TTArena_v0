# Microservicio de Habilidades de World of Warcraft

Este microservicio proporciona una API REST completa para gestionar habilidades de World of Warcraft, incluyendo todas las clases y especializaciones del juego.

## Características

- **Arquitectura de microservicio** con Spring Boot 3.4.3 y Java 23
- **Base de datos MongoDB** para almacenamiento de habilidades
- **API REST completa** con operaciones CRUD
- **Habilidades precargadas** de World of Warcraft para todas las clases
- **Búsqueda avanzada** por clase, especialización, tipo y texto
- **Validación de datos** y manejo de errores
- **Documentación automática** con Spring Boot Actuator

## Estructura del Proyecto

```
src/main/java/org/ttarena/arena_ability/
├── config/           # Configuraciones de MongoDB y Seguridad
├── controller/       # Controladores REST
├── model/           # Modelos de datos y enumeraciones
├── repository/      # Repositorios de MongoDB
└── service/         # Lógica de negocio
```

## Modelo de Datos

### Clases Disponibles
- **Paladin**: Clase híbrida con capacidades de tanque, DPS y curación
- **Priest**: Clase de curación y daño mágico
- **Rogue**: Clase de DPS cuerpo a cuerpo con sigilo
- **Shaman**: Clase híbrida con elementos y curación
- **Warrior**: Clase de tanque y DPS cuerpo a cuerpo

### Especializaciones
Cada clase tiene 3 especializaciones específicas:
- **Paladin**: Holy, Protection, Retribution
- **Priest**: Discipline, Holy, Shadow
- **Rogue**: Assassination, Outlaw, Subtlety
- **Shaman**: Elemental, Enhancement, Restoration
- **Warrior**: Arms, Fury, Protection

### Modelo de Habilidad
```java
public class Ability {
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private int cooldown;           // en segundos
    private int castTime;           // en milisegundos
    private int resourceCost;
    private ResourceType resourceType;
    private AbilityType abilityType;
    private WowClass wowClass;      // null si es específica de especialización
    private Specialization specialization; // null si es común para toda la clase
    private int baseValue;          // daño base o curación base
    private int range;              // rango en yardas
    private boolean areaEffect;     // si afecta un área
    private int areaRadius;         // radio del área de efecto
}
```

## API Endpoints

### Habilidades

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/abilities` | Obtener todas las habilidades |
| GET | `/api/v1/abilities/{id}` | Obtener habilidad por ID |
| GET | `/api/v1/abilities/name/{name}` | Obtener habilidad por nombre |
| GET | `/api/v1/abilities/class/{wowClass}` | Obtener habilidades por clase |
| GET | `/api/v1/abilities/specialization/{specialization}` | Obtener habilidades por especialización |
| GET | `/api/v1/abilities/class/{wowClass}/all` | Obtener todas las habilidades de una clase y sus especializaciones |
| GET | `/api/v1/abilities/type/{abilityType}` | Obtener habilidades por tipo |
| GET | `/api/v1/abilities/search?q={texto}` | Buscar habilidades por texto |
| POST | `/api/v1/abilities` | Crear nueva habilidad |
| PUT | `/api/v1/abilities/{id}` | Actualizar habilidad |
| DELETE | `/api/v1/abilities/{id}` | Eliminar habilidad |

### Ejemplos de Uso

#### Obtener todas las habilidades de Paladín
```bash
GET /api/v1/abilities/class/PALADIN
```

#### Obtener habilidades de Paladín Retribución
```bash
GET /api/v1/abilities/specialization/RETRIBUTION_PALADIN
```

#### Buscar habilidades que contengan "strike"
```bash
GET /api/v1/abilities/search?q=strike
```

#### Crear una nueva habilidad
```bash
POST /api/v1/abilities
Content-Type: application/json

{
    "name": "Divine Storm",
    "description": "Ataque en área que daña a todos los enemigos cercanos.",
    "iconUrl": "https://wow.zamimg.com/images/wow/icons/large/ability_paladin_divinestorm.jpg",
    "cooldown": 0,
    "castTime": 0,
    "resourceCost": 3,
    "resourceType": "HOLY_POWER",
    "abilityType": "OFFENSIVE",
    "specialization": "RETRIBUTION_PALADIN",
    "baseValue": 200,
    "range": 0,
    "areaEffect": true,
    "areaRadius": 8
}
```

## Habilidades Precargadas

El microservicio incluye las siguientes habilidades precargadas:

### Paladín
- **Generales**: Judgement, Crusader Strike, Hammer of Wrath, Word of Glory, Flash of Light, Divine Protection, Blessing of Protection, Blessing of Sacrifice
- **Retribución**: Final Verdict, Wake of Ashes
- **Sagrado**: Holy Shock, Beacon of Light
- **Protección**: Avenger's Shield, Ardent Defender

### Otras Clases
- **Priest**: Mind Control, Power Word: Shield, Shadow Word: Pain, Mind Flay
- **Rogue**: Stealth, Backstab, Vanish, Shadowstep
- **Shaman**: Chain Lightning, Frost Shock, Earth Elemental, Healing Rain
- **Warrior**: Mortal Strike, Rampage, Shield Wall, Intimidating Shout

## Configuración

### Requisitos
- Java 23
- MongoDB 4.4+
- Spring Boot 3.4.3

### Variables de Entorno
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/arena_abilities
server:
  port: 8080
```

### Ejecución
```bash
# Compilar el proyecto
./gradlew clean build

# Ejecutar la aplicación
java -jar build/libs/ability-0.0.1-SNAPSHOT.jar

# O ejecutar directamente
./gradlew bootRun
```

## Monitoreo

El microservicio incluye Spring Boot Actuator para monitoreo:
- **Health Check**: `GET /actuator/health`
- **Información**: `GET /actuator/info`
- **Métricas**: `GET /actuator/metrics`

## Seguridad

- Configuración de seguridad básica habilitada
- Endpoints de la API públicos para desarrollo
- Endpoints de actuator protegidos

## Desarrollo

### Estructura de Paquetes
- `config`: Configuraciones de Spring
- `controller`: Controladores REST con validación
- `model`: Entidades y enumeraciones
- `repository`: Interfaces de repositorio MongoDB
- `service`: Lógica de negocio e inicialización de datos

### Patrones Utilizados
- **Repository Pattern**: Para acceso a datos
- **Service Layer**: Para lógica de negocio
- **DTO Pattern**: Para transferencia de datos
- **Builder Pattern**: Para construcción de objetos

## Extensibilidad

El microservicio está diseñado para ser fácilmente extensible:
- Agregar nuevas clases en `WowClass` enum
- Agregar nuevas especializaciones en `Specialization` enum
- Agregar nuevos tipos de habilidades en `AbilityType` enum
- Implementar nuevos endpoints en el controlador
- Agregar validaciones personalizadas en el servicio

## Contribución

Para agregar nuevas habilidades:
1. Definir la habilidad en `AbilityDataInitializerService`
2. Asignar la clase o especialización correspondiente
3. Incluir todos los atributos necesarios (cooldown, costo, etc.)
4. Reiniciar la aplicación para cargar los nuevos datos

