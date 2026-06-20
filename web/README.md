# Web — Frontend de ejemplo

Una app web **de ejemplo/referencia** que consume la API de [`../backend`](../backend).
Sirve para **dos cosas**:

1. **Ver cómo luce** la plantilla (inventario, contabilidad, facturas, usuarios).
2. Ser un **ejemplo funcional** de cómo se consume la API, para quien construya el
   frontend "real" (web o móvil).

> No es el producto final ni tiene marca: es una base/plantilla.

## Cómo abrirla

Es un **solo archivo** sin instalación: abre [`index.html`](index.html) en el navegador
(doble clic), o sírvelo con un servidor estático:

```bash
npx serve .      # queda en http://localhost:3000
```

Necesita el **backend encendido** (`http://localhost:4000`). El indicador de arriba
muestra 🟢 *conectado* / 🔴 *backend apagado*.

## Configuración

La URL de la API está al inicio del `<script>` en `index.html`:

```js
const API = "http://localhost:4000/api";
```

Si abres la web desde **otro dispositivo**, cambia `localhost` por la IP de la PC
donde corre el backend.

## Qué muestra

- **Inventario:** tabla de productos (presentación, costo, precio, margen, stock), buscador, alta de producto/categoría (la presentación se elige de una lista), ajuste de stock.
- **Contabilidad:** semanas con ingresos/costos/ganancia y el reparto (salarios, recompra, ahorro) con barra de colores.
- **Facturas:** archivo por fecha, con filtro por proveedor. El *preview* del archivo y la captura por correo se conectan después (ver [API.md](../backend/API.md)).
- **Usuarios:** lista con insignia de rol (Admin / Encargado).

## Tecnología

HTML + CSS + **JavaScript vanilla** (sin framework ni build), `fetch` contra la API.
Quien haga el frontend definitivo puede usar lo que prefiera (React, Vue, móvil nativo…):
lo único que importa es que consuma los mismos endpoints. Ver
**[backend/API.md](../backend/API.md)**.
