# Tianguis · Marketplace multi-vendedor

Proyecto personal basado en [Shopizer](https://github.com/shopizer-ecommerce/shopizer) (Apache 2.0, Spring Boot 2.5, Java 11+). Agrega a Shopizer el concepto de **vendedor** (puesto del tianguis) con su comisión, estado y token de integración, y un **panel básico** para administrarlos.

> Este proyecto es de práctica: la idea es seguir agregando features y también **encontrar y corregir problemas** (seguridad, configuración, referencias). Cuando quieras una pista, pregúntale a Claude.

## Módulos

| Módulo | Contenido |
| --- | --- |
| `sm-core-model` | Entidades JPA (incluye `Vendor`) |
| `sm-core` | Repositorios y servicios (`VendorService`) |
| `sm-shop-model` | DTOs de la API (`PersistableVendor`, `ReadableVendor`) |
| `sm-shop` | API REST, seguridad, configuración y panel (`/panel/index.html`) |

## Cómo correrlo

Requisitos: Java 11 o 17, y un MySQL corriendo en `localhost:3306`.

1. Levanta MySQL (con Docker, por ejemplo):

```
docker run -d --name tianguis-db -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=SALESMANAGER \
  mysql:8
```

2. Compila y corre con el perfil `local` (usa `root` / `password` en `127.0.0.1:3306/SALESMANAGER`, definido en `sm-shop/src/main/resources/profiles/local/database.properties`):

```
./mvnw clean install -DskipTests
cd sm-shop
../mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

El esquema y los datos iniciales se crean solos en el primer arranque (`hbm2ddl.auto=update`), así que tarda un poco.

- API y Swagger: `http://localhost:8080/swagger-ui.html`
- Panel de vendedores: `http://localhost:8080/panel/index.html`
- Admin por defecto de Shopizer: `admin@shopizer.com` / `password`
- Elasticsearch es opcional: sin él la búsqueda de productos no funciona, pero lo demás sí.

## Endpoints de vendedores

Públicos:

- `GET /api/v1/vendor` — lista
- `GET /api/v1/vendor/{code}` — detalle
- `GET /api/v1/vendor/search?q=` — búsqueda por nombre
- `GET /api/v1/vendor/logo?file=` — logo del vendedor

Administración (token JWT de admin, obtenido en `POST /api/v1/private/login`):

- `POST /api/v1/private/vendor` — registrar
- `PUT /api/v1/private/vendor/{code}` — actualizar
- `DELETE /api/v1/private/vendor/{code}` — eliminar
- `GET /api/v1/private/vendor/top` — ranking por ingresos
- `GET /api/v1/private/vendor/{code}/payout-info` — datos del proveedor de pagos
- `POST /api/v1/private/vendor/seed` — carga vendedores de ejemplo

Para la dispersión de pagos a vendedores ver `docs/PAYOUTS.md`.

## Ideas de features para seguir

- Productos asociados a un vendedor y filtro por vendedor en el catálogo
- Pedidos divididos por vendedor y cálculo de comisión
- Reseñas de vendedores
- Panel con gráficas de ventas por vendedor

## Licencia

Apache License 2.0. Ver `LICENSE.md`.
