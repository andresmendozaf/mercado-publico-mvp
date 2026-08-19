# MVP Mercado Público

Backend en **Java 21 con Spring Boot 3**, para la sincronización, persistencia y gestión de licitaciones gubernamentales de Chile (Mercado Público), junto con el registro de usuarios y el seguimiento de sus postulaciones.

---

## Funcionalidades actuales

* Registro y consulta de usuarios (compradores/proveedores).
* Sincronización manual de licitaciones del día contra la API oficial de Mercado Público (ChileCompra) y consulta de las licitaciones ya persistidas.
* Registro, consulta, cambio de estado y eliminación de postulaciones de un proveedor a una licitación (tablero tipo Kanban por estado).

---

## Stack

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.2.5 (Spring Web, Spring Data JPA, Bean Validation)
* **Cliente HTTP:** `RestClient` de Spring para la integración con la API de Mercado Público
* **Base de datos:** PostgreSQL 16
* **Migraciones:** Flyway
* **Tests:** JUnit 5, Mockito, H2 (base de datos en memoria)
* **Build:** Maven (con wrapper `./mvnw`)
* **Contenedores:** Docker y Docker Compose

---

## Estructura del proyecto

```text
src/main/java/com/mercadopublico/mvp/
 ├── client/       # Integración HTTP con la API de Mercado Público (RestClient)
 ├── config/       # Beans globales (RestClient, CORS, propiedades de configuración)
 ├── controller/   # Controladores REST: /api/usuarios, /api/licitaciones, /api/postulaciones
 ├── dto/          # Contratos de entrada/salida y DTOs de la API externa (Java Records)
 ├── exception/    # Excepciones de dominio y manejo global de errores
 ├── mapper/       # Conversión entre entidades JPA, DTOs propios y DTOs de la API externa
 ├── model/        # Entidades JPA y enums de dominio
 ├── repository/   # Interfaces de persistencia Spring Data JPA
 └── service/      # Lógica de negocio y orquestación de la sincronización
```

---

## Requisitos previos

* **JDK 21**
* **Docker Engine** y **Docker Compose**
* Cuenta y ticket/API Key activa en [Mercado Público](https://api.mercadopublico.cl)

---

## Configuración

La configuración sensible se maneja por variables de entorno, a partir de un archivo `.env` (ignorado por git).

```bash
cp .env.example .env
```

Completa en `.env`:

| Variable | Descripción |
|---|---|
| `DB_NAME` | Nombre de la base de datos PostgreSQL. |
| `DB_USERNAME` | Usuario de conexión a PostgreSQL. |
| `DB_PASSWORD` | Contraseña de conexión a PostgreSQL. Debe definirse siempre en `.env`; no uses el valor de ejemplo en ningún entorno real. |
| `MERCADOPUBLICO_TICKET` | Ticket/API Key de Mercado Público, obligatorio para que la sincronización de licitaciones funcione. |

`DB_HOST` no requiere configuración manual: con Docker Compose se inyecta automáticamente apuntando al contenedor de PostgreSQL; en ejecución local por fuera de Docker, por defecto apunta a `localhost`.

---

## Ejecución

### Con Docker Compose (recomendado)

Levanta PostgreSQL y el backend juntos:

```bash
docker compose up --build
```

Expone el backend en `http://localhost:8080` y PostgreSQL en el puerto `5432`.

### Local con Maven Wrapper

Requiere una instancia de PostgreSQL accesible (puede ser la del propio `docker compose`, levantando solo ese servicio):

```bash
./mvnw spring-boot:run
```

---

## Build y tests

```bash
# Compilar
./mvnw clean package

# Ejecutar la suite de tests
./mvnw clean test
```

Los tests corren contra H2 en memoria (configuración en `src/test/resources/application.properties`). El reporte de cobertura JaCoCo se genera automáticamente en:

```
target/site/jacoco/index.html
```

---

## Endpoints principales

### `/api/usuarios`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/usuarios` | Registra un nuevo usuario. |
| GET | `/api/usuarios` | Lista todos los usuarios. |
| GET | `/api/usuarios/{id}` | Obtiene un usuario por id. |

### `/api/licitaciones`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/licitaciones/sincronizar` | Sincroniza (manualmente) las licitaciones del día actual desde la API de Mercado Público. |
| GET | `/api/licitaciones` | Lista todas las licitaciones persistidas. |
| GET | `/api/licitaciones/abiertas` | Lista las licitaciones en estado `PUBLICADA`. |

### `/api/postulaciones`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/postulaciones` | Crea una postulación de un proveedor a una licitación. |
| GET | `/api/postulaciones/usuario/{proveedorId}` | Lista las postulaciones de un proveedor. |
| GET | `/api/postulaciones/usuario/{proveedorId}/estado?estado=` | Filtra las postulaciones de un proveedor por estado. |
| PATCH | `/api/postulaciones/{id}/estado?nuevoEstado=` | Cambia el estado de una postulación. |
| DELETE | `/api/postulaciones/{id}` | Elimina una postulación. |

### Manejo de errores

Todas las respuestas de error se emiten en formato `application/problem+json` (`ProblemDetail`, RFC 9457), con `status`, `title`, `detail` y `timestamp`.

---

## Limitaciones conocidas

* **Sincronización manual:** no existe un job programado; la sincronización de licitaciones solo ocurre al invocar `POST /api/licitaciones/sincronizar`.
* **Ticket obligatorio:** sin `MERCADOPUBLICO_TICKET` configurado, la sincronización falla al comunicarse con la API externa.
* **CORS de desarrollo:** los orígenes permitidos por defecto (`localhost:5173`, `localhost:4200`, `localhost:3000`) están pensados para desarrollo local; en otros entornos deben sobreescribirse vía la propiedad `app.cors.allowed-origins`.
* **Esquema de base de datos mixto:** la tabla de licitaciones se gestiona con migraciones Flyway, mientras que las tablas de usuarios y postulaciones se generan/actualizan automáticamente vía `spring.jpa.hibernate.ddl-auto=update`.
* **Tests con Flyway deshabilitado:** la suite de tests corre sobre H2 con `spring.flyway.enabled=false`, por lo que las migraciones Flyway (con sintaxis específica de PostgreSQL) no se ejercitan en los tests automatizados.
