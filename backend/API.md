# LindaVista — Referencia de la API

Guía para quien desarrolla el **frontend** (web o móvil). Todos los dispositivos
consumen esta misma API y la misma base de datos.

- **Base URL (local):** `http://localhost:4000`
- **Prefijo de todas las rutas:** `/api`
- **Formato:** JSON (envía `Content-Type: application/json`).

> Frontend de ejemplo funcional: [`../web/index.html`](../web/index.html) — vanilla JS,
> consume todos estos endpoints. Úsalo como referencia.

---

## Convenciones

**IDs:** son `string` (UUID), p. ej. `"7418e03e-f692-4a2d-8542-2c85a9f17adf"`.

**Dinero:** número con 2 decimales (`16.00`). Fechas de factura/semana: `"YYYY-MM-DD"`.
Marcas de tiempo (`createdAt`...): ISO-8601 UTC (`"2026-06-19T21:47:51Z"`).

**Lista paginada:**
```json
{
  "data": [ /* ... */ ],
  "pagination": { "page": 1, "pageSize": 20, "total": 6, "totalPages": 1 }
}
```
Parámetros: `page` (empieza en 1) y `pageSize` (1–100, def. 20).

**Lista simple** (categorías, usuarios): `{ "data": [ ... ] }`.

**Un recurso:** el objeto directo (sin envoltura).

**Errores:** siempre con el código HTTP correcto y este cuerpo:
```json
{ "error": { "message": "texto", "details": { "campo": "motivo" } } }
```
| HTTP | Cuándo |
|---|---|
| 400 | Validación (revisa `details`) |
| 404 | No encontrado |
| 409 | Duplicado (p. ej. correo o categoría repetida) |
| 503 | Sin conexión a la base de datos |

**CORS:** abierto (`*`) en desarrollo. **No hay autenticación todavía** (ver
[Usuarios](#usuarios-y-roles)).

---

## Salud

`GET /api/health`
```json
{ "status": "ok", "database": "ok", "timestamp": "2026-06-19T22:00:00Z" }
```

---

## Categorías

| Método | Ruta | Cuerpo |
|---|---|---|
| GET | `/api/categories?search=` | — |
| POST | `/api/categories` | `{ "name": "Cervezas" }` |
| GET | `/api/categories/{id}` | — |
| PATCH | `/api/categories/{id}` | `{ "name": "..." }` |
| DELETE | `/api/categories/{id}` | — |

Respuesta de una categoría:
```json
{ "id": "...", "name": "Cervezas", "productCount": 3,
  "createdAt": "...", "updatedAt": "..." }
```

---

## Productos

Cada **producto** lleva su propia **presentación** (texto: "Botella 355ml",
"Caballito (shot)"…), **precio** y **stock**. El frontend ofrece las presentaciones
como opciones para elegir; en el backend es un campo libre.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/products?search=&categoryId=&active=&page=&pageSize=` | Lista paginada |
| POST | `/api/products` | Crea producto |
| GET | `/api/products/{id}` | Detalle |
| PATCH | `/api/products/{id}` | Actualiza (cualquier campo) |
| DELETE | `/api/products/{id}` | Elimina |
| POST | `/api/products/{id}/adjust-stock` | Suma/resta stock `{ delta, reason? }` |

**Crear producto:**
```json
POST /api/products
{
  "name": "Tequila Don Julio",
  "presentation": "Caballito (shot)",
  "description": "Tequila reposado 100% agave",
  "notes": "Revisar caducidad lote actual",
  "tags": ["Promoción", "Temporada"],
  "cost": 35,
  "price": 90,
  "stock": 200,
  "categoryId": "<id-categoria>"
}
```

Campos opcionales en create/update: `description`, `notes`, `tags` (arreglo de strings, máx. 40 chars c/u), `presentation`, `active`, `categoryId`.

**Respuesta de un producto** (`margin` = `price − cost`, lo calcula el backend):
```json
{
  "id": "7418e03e-...",
  "name": "Tequila Don Julio",
  "presentation": "Caballito (shot)",
  "description": "Tequila reposado 100% agave",
  "notes": "Revisar caducidad lote actual",
  "tags": ["Promoción", "Temporada"],
  "cost": 35.00, "price": 90.00, "margin": 55.00,
  "stock": 200, "active": true,
  "categoryId": "3fd60f25-...",
  "category": { "id": "3fd60f25-...", "name": "Licores" },
  "createdAt": "...", "updatedAt": "..."
}
```

`adjust-stock` suma/resta de forma segura (no deja stock negativo → 400) y devuelve
el producto actualizado.

---

## Ventas (base del futuro POS)

Se vende un **producto**. La venta **descuenta stock** de forma atómica: si una
línea no tiene stock suficiente, **se revierte toda la venta**.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/sales?from=&to=&page=&pageSize=` | Historial (`from`/`to` ISO date-time) |
| POST | `/api/sales` | Registra venta y descuenta stock |
| GET | `/api/sales/{id}` | Detalle |

**Crear venta:**
```json
POST /api/sales
{
  "paymentMethod": "cash",
  "note": "Mesa 4",
  "items": [
    { "productId": "7418e03e-...", "quantity": 2 },
    { "productId": "0be7fdb2-...", "quantity": 1, "unitPrice": 35.00 }
  ]
}
```
- `paymentMethod` ∈ `cash | card | transfer | other` (def. `cash`).
- `unitPrice` opcional: si no se manda, usa el precio del producto.

**Respuesta:**
```json
{
  "id": "...", "total": 215.00, "paymentMethod": "cash", "note": "Mesa 4",
  "createdAt": "...",
  "items": [
    { "id": "...", "productId": "7418e03e-...",
      "name": "Tequila Don Julio - Caballito (shot)",
      "quantity": 2, "unitPrice": 90.00, "subtotal": 180.00 }
  ]
}
```
`name` es una "foto" (producto + presentación) al momento de la venta; no cambia si luego editas el producto.

---

## Contabilidad semanal

Registra la **ganancia de la semana** y **repártela** entre salarios, recompra y
ahorro/mejoras.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/accounting/weeks?page=&pageSize=` | Lista de semanas |
| POST | `/api/accounting/weeks` | Crea semana |
| GET | `/api/accounting/weeks/{id}` | Detalle con su reparto |
| PATCH | `/api/accounting/weeks/{id}` | Actualiza |
| DELETE | `/api/accounting/weeks/{id}` | Elimina |
| GET | `/api/accounting/weeks/{id}/summary` | Resumen del reparto |
| POST | `/api/accounting/weeks/{id}/allocations` | Agrega reparto |
| PATCH | `/api/accounting/allocations/{allocationId}` | Actualiza reparto |
| DELETE | `/api/accounting/allocations/{allocationId}` | Elimina reparto |

**Crear semana** (si no mandas `profit`, lo calcula como `revenue − cost`):
```json
POST /api/accounting/weeks
{ "weekStart": "2026-06-08", "weekEnd": "2026-06-14",
  "revenue": 8000, "cost": 4500, "notes": "Semana de ejemplo" }
```

**Agregar reparto** (`type` ∈ `SALARIES | RESTOCK | SAVINGS | OTHER`):
```json
POST /api/accounting/weeks/{id}/allocations
{ "type": "SALARIES", "label": "Sueldos", "amount": 1500 }
```

**Resumen** `GET /api/accounting/weeks/{id}/summary`:
```json
{ "profit": 3500, "allocated": 1500, "remaining": 2000, "byType": { "SALARIES": 1500 } }
```

---

## Usuarios y roles

Roles: **`ADMIN`** (dueño) y **`MANAGER`** (administra el negocio).

> ⚠️ **No hay login todavía.** Estos endpoints están abiertos. Cuando agregues
> autenticación, **protégelos para que solo el `ADMIN` gestione usuarios**. La
> contraseña se guarda encriptada (BCrypt) y **nunca** se devuelve en las respuestas.

| Método | Ruta | Cuerpo |
|---|---|---|
| GET | `/api/users?search=` | — |
| POST | `/api/users` | `{ "name", "email", "password", "role" }` |
| GET | `/api/users/{id}` | — |
| PATCH | `/api/users/{id}` | `{ "name"?, "email"?, "password"?, "role"?, "active"? }` |
| DELETE | `/api/users/{id}` | — |

Respuesta:
```json
{ "id": "...", "name": "Administrador", "email": "admin@negocio.com",
  "role": "ADMIN", "active": true, "createdAt": "...", "updatedAt": "..." }
```

---

## Facturas

Archivo de facturas/recibos por fecha. **Bases listas; falta conectar el
almacenamiento del archivo (S3) y la captura por correo** (eso lo haces tú).

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/invoices?from=&to=&vendor=&page=&pageSize=` | Lista por fecha/proveedor |
| POST | `/api/invoices` | Registra factura (origen `UPLOAD`) |
| GET | `/api/invoices/{id}` | Detalle |
| PATCH | `/api/invoices/{id}` | Actualiza |
| DELETE | `/api/invoices/{id}` | Elimina |
| POST | `/api/invoices/email-inbound` | **Stub** para captura por correo |

**Crear factura:**
```json
POST /api/invoices
{ "invoiceDate": "2026-06-18", "vendor": "Distribuidora del Norte",
  "amount": 4500.00, "number": "A-12345", "notes": "Recompra" }
```

**Respuesta** (los campos de archivo se llenan cuando conectes S3):
```json
{ "id": "...", "invoiceDate": "2026-06-18", "vendor": "Distribuidora del Norte",
  "amount": 4500.00, "number": "A-12345", "notes": "Recompra", "source": "UPLOAD",
  "fileName": null, "contentType": null, "fileUrl": null, "previewUrl": null,
  "accountingWeekId": null, "createdAt": "...", "updatedAt": "..." }
```

**Pendiente para ti (frontend/integración):**
- Subir el PDF/foto a **S3** y guardar `fileUrl`/`previewUrl`/`contentType` (vía POST o PATCH).
- **Correo:** configurar un buzón (`facturas@tunegocio.com`) en Mailgun/Postmark/AWS SES
  que haga `POST /api/invoices/email-inbound` con los datos del correo. El servicio crea
  la factura marcada como `EMAIL`. Falta: validar la firma del webhook y guardar el adjunto en S3.

---

Cuerpos de ejemplo listos para probar: [`requests.http`](requests.http)
(extensión *REST Client* de VS Code).
