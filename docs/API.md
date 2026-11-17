# REST API

Base path: `/api`. Docker origin: `http://localhost:8080`. Direct host backend origin: `http://localhost:8081`. Requests with bodies use `Content-Type: application/json`. IDs are positive integers. There is no authentication in this local version.

## Products

| Method | Path | Success |
| --- | --- | --- |
| GET | /products | 200, a page of products |
| GET | /products/{id} | 200, one product |
| POST | /products | 201, product and Location header |
| PUT | /products/{id} | 200, updated product |
| DELETE | /products/{id} | 204, no response body |

POST and PUT replace all editable fields:

```json
{
  "name": "Wireless keyboard",
  "description": "Compact USB keyboard",
  "sku": "KEY-001",
  "price": "49.90",
  "quantity": 12,
  "categoryId": 1
}
```

Name is required, at most 120 characters. Description is optional, at most 2000 characters. SKU is required, at most 64 ASCII letters/digits/hyphens/underscores; it is stored uppercase and must be unique. Price is required, non-negative, with at most 12 integer digits and 2 fractional digits. Quantity is required, from 0 to 2147483647. Category must exist. Missing/null descriptions are stored as empty strings. PUT does not modify IDs or creation times.

A product response contains `id`, `name`, `description`, `sku`, `price` (decimal string), `quantity`, `category` (category response), `createdAt` and `updatedAt`.

### Product query parameters

| Parameter | Default | Meaning |
| --- | --- | --- |
| search | absent | Case-insensitive substring of name, up to 120 characters; wildcards treated literally |
| categoryId | absent | Positive category ID |
| lowStock | false | Only quantity <= configured threshold |
| page | 0 | Zero-based page, non-negative |
| size | 10 | Between 1 and 100 |
| sort | createdAt | name, price or createdAt |
| direction | desc | asc or desc |

Filters combine with AND. An unknown positive category filter returns an empty page. Sorting adds ID as a stable tie-breaker. A page beyond the available results has empty content. Category names are not product-search targets.

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0
}
```

## Categories

| Method | Path | Success |
| --- | --- | --- |
| GET | /categories | 200, unpaginated array sorted by name |
| GET | /categories/{id} | 200, one category |
| POST | /categories | 201, category and Location header |
| PUT | /categories/{id} | 200, updated category |
| DELETE | /categories/{id} | 204, no response body |

POST and PUT input:

```json
{"name": "Office equipment", "description": "Accessories for the workspace"}
```

Name is required, at most 80 characters, and unique ignoring case. Surrounding whitespace is removed. Description is optional, at most 1000 characters. A category response includes `id`, `name`, `description`, `createdAt` and `updatedAt`.

Deleting a category with products returns 409 and a message instructing the caller to move or remove those products. Products are never automatically cascade-deleted.

## Dashboard

`GET /dashboard` returns 200:

```json
{
  "totalProducts": 0,
  "totalCategories": 0,
  "totalInventoryValue": "0",
  "lowStockCount": 0,
  "lowStockThreshold": 5,
  "recentProducts": [],
  "lowStockProducts": []
}
```

Totals cover the entire database, not a product page. Inventory value is exact price × quantity summed in PostgreSQL. Recent products are the five newest; low-stock products are up to five sorted by name. The count includes all matching products. These values are real server responses, including zeros when the database is empty.

## Error responses

```json
{
  "timestamp": "2026-11-17T12:00:00Z",
  "status": 400,
  "message": "Please check the submitted fields.",
  "path": "/api/products",
  "fieldErrors": {"name": "must not be blank"}
}
```

The timestamp above is illustrative. `fieldErrors` is empty when the failure is unrelated to individual DTO fields. Error text can change; clients should use HTTP status and field names rather than exact message matching.

| Status | Meaning |
| --- | --- |
| 400 | Invalid DTO, ID, number, JSON, pagination or sort |
| 404 | Missing product/category or route |
| 405 | HTTP method not supported |
| 409 | Duplicate SKU/name or a category still in use |
| 415 | Unsupported request content type |
| 500 | Unexpected internal error, details hidden |

Dates are ISO local date-time strings representing UTC. Clients may append `Z` before interpreting them as instants. Monetary values remain decimal strings in the JSON contract.

## Interactive documentation

- Docker: `/swagger-ui/index.html` through port 8080.
- Host development: `/swagger-ui/index.html` through port 8081.
- OpenAPI JSON: `/v3/api-docs`.

Springdoc derives request/response structures and query parameters from controllers and DTOs. A shared customizer adds documented 400/404/409/500 error schemas to each operation.

## PowerShell example

With the Docker stack running:

```powershell
$base = 'http://localhost:8080/api'
$categoryBody = @{ name = 'Office'; description = 'Workspace products' } | ConvertTo-Json
$category = Invoke-RestMethod -Method Post -Uri "$base/categories" -ContentType 'application/json' -Body $categoryBody
$productBody = @{ name = 'Keyboard'; description = 'USB'; sku = 'KEY-001'; price = '19.99'; quantity = 3; categoryId = $category.id } | ConvertTo-Json
$product = Invoke-RestMethod -Method Post -Uri "$base/products" -ContentType 'application/json' -Body $productBody
Invoke-RestMethod -Uri "$base/products?search=key&lowStock=true"
Invoke-RestMethod -Uri "$base/dashboard"
```

These calls create real local records. Use the UI to review, update and remove them when finished.
