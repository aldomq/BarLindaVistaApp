# LindaVista
https://aldomq.github.io/BarLindaVistaApp/
Plantilla de aplicación para un negocio: **inventario** + **contabilidad semanal**,
pensada desde el inicio para ser **multi-dispositivo** (web, móvil y tablet comparten datos)
y para **crecer** (próximo paso: POS para cobrar en tablet).

> Sin logo ni marca: es una base lista para personalizar.

## Arquitectura

```
        ┌─────────────┐        ┌─────────────┐
        │   Web app    │       │   App móvil  │   (la desarrolla otra persona)
        └──────┬──────┘        └──────┬──────┘
               │   HTTP / JSON        │
               └──────────┬───────────┘
                          ▼
                 ┌──────────────────┐
                 │   Backend (API)  │   Java + Spring Boot
                 └────────┬─────────┘
                          ▼
                 ┌──────────────────┐
                 │  PostgreSQL nube │   AWS RDS (o Neon / Supabase)
                 └──────────────────┘
```

Una **sola API** y una **sola base de datos** en la nube: cualquier dispositivo
(web, móvil, tablet) ve la misma información.

## Carpetas

- [`backend/`](backend/) — **API REST en Java (Spring Boot)**, ya implementada y probada.
  Inventario (productos + presentaciones), ventas, contabilidad, usuarios/roles y facturas.
  Setup y **guía de AWS RDS** en [backend/README.md](backend/README.md);
  **referencia de endpoints** en [backend/API.md](backend/API.md).
- [`web/`](web/) — **frontend de ejemplo** (un solo `index.html`, sin build) que consume
  la API. Es una referencia para el frontend real; ver [web/README.md](web/README.md).
- `backend-node/` — versión anterior en Node/TypeScript (referencia; se puede borrar).
- La app móvil vive en su propio repositorio y consume la misma API.

## Empezar

Sigue [backend/README.md](backend/README.md): configurar la base de datos en la nube
(AWS RDS, Neon o Supabase) y arrancar la API con el Maven Wrapper.
