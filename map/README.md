# Documentación del Microservicio de Mapas Hexagonales

## Descripción General

Este microservicio proporciona una API REST para la gestión de mapas hexagonales en el proyecto Arena. Permite crear, consultar, actualizar y eliminar mapas compuestos por hexágonos, así como manipular los hexágonos individuales dentro de cada mapa.

## Arquitectura

El microservicio sigue una arquitectura en capas típica de Spring Boot:

1. **Capa de Controlador**: Expone los endpoints REST y maneja las peticiones HTTP.
2. **Capa de Servicio**: Implementa la lógica de negocio.
3. **Capa de Repositorio**: Gestiona la persistencia de datos en MongoDB.
4. **Capa de Modelo**: Define las entidades y documentos.
5. **Capa de Utilidades**: Proporciona funciones auxiliares para operaciones con hexágonos.
6. **Capa de Excepciones**: Maneja los errores de forma consistente.

## Modelo de Datos

### Sistema de Coordenadas Hexagonales

El sistema utiliza coordenadas cúbicas (q, r, s) donde:
- **q**: Eje horizontal (de izquierda a derecha)
- **r**: Eje diagonal (de arriba-izquierda a abajo-derecha)
- **s**: Eje diagonal (de arriba-derecha a abajo-izquierda)

La restricción principal es que q + r + s = 0 para todas las coordenadas válidas.

### Entidades Principales

1. **HexCoordinate**: Representa las coordenadas de un hexágono.
2. **HexTile**: Representa un hexágono individual con propiedades como tipo de terreno, elevación, etc.
3. **GameMap**: Representa un mapa completo compuesto por múltiples hexágonos.

## API REST

### Endpoints para Mapas

- `GET /api/maps`: Obtiene todos los mapas disponibles.
- `GET /api/maps/search?name={name}&author={author}`: Busca mapas por nombre o autor.
- `GET /api/maps/{id}`: Obtiene un mapa específico por su ID.
- `POST /api/maps`: Crea un nuevo mapa.
- `PUT /api/maps/{id}`: Actualiza un mapa existente.
- `DELETE /api/maps/{id}`: Elimina un mapa.
- `POST /api/maps/generate`: Genera un mapa hexagonal vacío con dimensiones específicas.

### Endpoints para Hexágonos

- `GET /api/maps/{mapId}/tiles/{q}/{r}/{s}`: Obtiene un hexágono específico de un mapa.
- `POST /api/maps/{mapId}/tiles`: Añade un hexágono a un mapa.
- `PUT /api/maps/{mapId}/tiles/{q}/{r}/{s}`: Actualiza un hexágono en un mapa.
- `DELETE /api/maps/{mapId}/tiles/{q}/{r}/{s}`: Elimina un hexágono de un mapa.

## Utilidades

- **HexUtils**: Proporciona funciones para manipular coordenadas hexagonales, convertir entre sistemas de coordenadas, etc.
- **MapGenerator**: Ofrece métodos para generar mapas aleatorios, calcular rutas y campos de visión.

## Manejo de Excepciones

El microservicio incluye un manejo global de excepciones que proporciona respuestas de error consistentes:

- `MapNotFoundException`: Cuando no se encuentra un mapa solicitado.
- `HexTileNotFoundException`: Cuando no se encuentra un hexágono solicitado.
- `InvalidHexCoordinateException`: Cuando se proporcionan coordenadas hexagonales inválidas.

## Ejemplos de Uso

### Crear un Nuevo Mapa

```http
POST /api/maps
Content-Type: application/json

{
  "name": "Mundo de Arena",
  "description": "Un vasto desierto con oasis y montañas",
  "author": "GameMaster",
  "width": 20,
  "height": 20
}
```

### Generar un Mapa Automáticamente

```http
POST /api/maps/generate?name=Bosque%20Encantado&description=Un%20bosque%20misterioso&author=GameMaster&radius=10&terrainType=forest
```

### Añadir un Hexágono a un Mapa

```http
POST /api/maps/{mapId}/tiles
Content-Type: application/json

{
  "coordinate": {
    "q": 1,
    "r": -1,
    "s": 0
  },
  "terrainType": "bosque",
  "elevation": 2,
  "passable": true,
  "movementCost": 2
}
```

## Consideraciones Técnicas

- El microservicio utiliza Spring WebFlux para proporcionar una API reactiva.
- Los datos se almacenan en MongoDB utilizando Spring Data MongoDB Reactive.
- Se utiliza Lombok para reducir el código boilerplate.
- La validación de datos se realiza mediante Jakarta Validation.
