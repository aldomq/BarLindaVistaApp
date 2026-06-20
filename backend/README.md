# LindaVista — Backend (API REST en Java)

API central para **inventario** y **contabilidad semanal** de un negocio.
Pensada para ser consumida por **varios dispositivos**: la web app y la app móvil
comparten esta misma API y la misma base de datos en la nube.

> Plantilla sin marca/logo. Diseñada para crecer (siguiente paso: POS para cobrar en tablet).

---

## Stack

- **Java 21 + Spring Boot 4**
- **Spring Web** (API REST)
- **Spring Data JPA + Hibernate** (acceso a datos; crea las tablas solo)
- **PostgreSQL** (en la nube: **AWS RDS**, Neon o Supabase)
- **Jakarta Validation** (validación de entradas)
- **Maven Wrapper** (`mvnw`): no necesitas instalar Maven

Requisito: tener **JDK 21** instalado (`java -version`).

---

## 1. Configurar variables de entorno

```bash
cd backend
cp .env.example .env
```

Edita `.env` con los datos de tu base de datos (ver la guía de AWS más abajo).

## 2. Arrancar

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

> Las **tablas se crean automáticamente** al arrancar (Hibernate `ddl-auto=update`).
> Para cargar datos de ejemplo la primera vez, pon `SEED_ON_START=true` en `.env`.

La API queda en `http://localhost:4000`. Prueba: `http://localhost:4000/api/health`
(debe responder `"database":"ok"` cuando la conexión funciona).

## 3. Compilar / ejecutar como JAR (producción)

```bash
.\mvnw.cmd clean package
java -jar target\backend-0.0.1-SNAPSHOT.jar
```

## 4. Pruebas

```bash
.\mvnw.cmd test
```

Incluye una prueba de integración que levanta la API con una base en memoria (H2)
y ejecuta el flujo completo: crear producto → vender → descontar stock → contabilidad.

> Si `mvnw` se queja de `JAVA_HOME`, define la variable apuntando al JDK 21, por ejemplo:
> `set JAVA_HOME=C:\Program Files\Java\jdk-21`

---

## 🟧 Crear la base de datos en AWS RDS (PostgreSQL, capa gratis)

1. Entra a la **consola de AWS → RDS**: https://console.aws.amazon.com/rds
   (elige una región, p. ej. *us-east-1*).
2. **Create database** → *Standard create* → **PostgreSQL**.
3. **Templates: Free tier** (importante para no pagar).
4. **Settings**:
   - DB instance identifier: `lindavista`
   - Master username: `postgres`
   - Master password: *(elige una y guárdala)*
5. **Instance configuration**: deja `db.t3.micro` / `db.t4g.micro` (lo fija el free tier).
6. **Storage**: 20 GB y **desactiva** "Enable storage autoscaling" (para no salir de la capa gratis).
7. **Connectivity**:
   - **Public access: Yes** (para conectarte desde tu PC mientras desarrollas)
   - VPC security group: *Create new* (o uno existente)
8. **Additional configuration → Initial database name**: `lindavista`
9. **Create database** y espera ~5–10 min hasta que el estado sea **Available**.
10. Abre la instancia y copia el **Endpoint** (es el host) y el **Port** (5432).
11. En el **Security group** de la instancia → *Inbound rules* → **Edit** → *Add rule*:
    - Type: **PostgreSQL**, Port: **5432**, Source: **My IP** *(o 0.0.0.0/0 solo para pruebas)*.
12. Llena tu `.env`:
    ```
    DB_HOST=lindavista.xxxxxxxx.us-east-1.rds.amazonaws.com
    DB_PORT=5432
    DB_NAME=lindavista
    DB_USER=postgres
    DB_PASSWORD=tu_password
    DB_SSL=require
    ```
13. Arranca (`.\mvnw.cmd spring-boot:run`). Las tablas se crean solas.
    Verifica `http://localhost:4000/api/health` → `"database":"ok"`.

> 💡 ¿Prefieres gratis para siempre y sin tarjeta? Usa **Neon** (https://neon.tech) o
> **Supabase** (https://supabase.com): saca host/usuario/password/dbname del *connection string*
> y rellena los mismos `DB_*` con `DB_SSL=require`. No hay que cambiar nada de código.

---

## Convenciones de la API

- Base de todas las rutas: **`/api`**
- Listas paginadas: `{ "data": [...], "pagination": { page, pageSize, total, totalPages } }`
- Lista simple (categorías): `{ "data": [...] }`
- Un recurso: el objeto directo.
- Errores: `{ "error": { "message": "...", "details"?: ... } }` con el código HTTP correcto
  (400 validación, 404 no encontrado, 409 duplicado, 503 sin base de datos).
- El dinero se devuelve como número.

---

## Endpoints

📘 **Referencia completa con ejemplos de petición/respuesta: [API.md](API.md)**
(es la guía para quien desarrolla el frontend). Resumen rápido:

| Módulo | Rutas base |
|---|---|
| Salud | `GET /api/health` |
| Categorías | `/api/categories` (CRUD) |
| Productos | `/api/products` (CRUD, paginado: `search`, `categoryId`, `active`; `+ /{id}/adjust-stock`) |
| Ventas | `/api/sales` (vende un producto, descuenta stock) |
| Contabilidad | `/api/accounting/weeks` (+ `/summary`, `/allocations`) |
| Usuarios y roles | `/api/users` (roles `ADMIN`/`MANAGER`; sin login todavía) |
| Facturas | `/api/invoices` (filtra por fecha/proveedor; `+ /email-inbound` stub) |

> **Productos:** cada producto lleva su propia **presentación** (texto, ej. "Botella 355ml"),
> **precio** y **stock**. El frontend ofrece las presentaciones como opciones a elegir.

Cuerpos de ejemplo listos para probar en [requests.http](requests.http).

---

## Estructura del proyecto

```
backend/
├─ pom.xml                 # dependencias y build (Maven)
├─ mvnw / mvnw.cmd         # Maven Wrapper
└─ src/
   ├─ main/java/com/lindavista/
   │  ├─ LindavistaBackendApplication.java  # arranque
   │  ├─ common/            # errores, paginación, CORS, health
   │  ├─ category/          # entidad + repo + service + controller + dtos
   │  ├─ product/           # inventario (producto: presentación + precio + stock)
   │  ├─ sale/              # ventas (futuro POS)
   │  ├─ accounting/        # contabilidad semanal
   │  ├─ user/              # usuarios y roles (ADMIN/MANAGER)
   │  ├─ invoice/           # facturas (por fecha; archivo/correo pendientes)
   │  └─ bootstrap/         # datos de ejemplo (opcional)
   ├─ main/resources/application.properties
   └─ test/java/com/lindavista/  # pruebas
```

### Cómo agregar un módulo nuevo
1. Crea `com/lindavista/<modulo>/` con la entidad, su `Repository`, `Service`, `Controller` y `Dtos`.
2. Al arrancar, Hibernate crea la tabla nueva automáticamente.
3. (Listo: Spring detecta el controlador solo.)
