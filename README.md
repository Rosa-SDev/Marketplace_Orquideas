# 🌸 Marketplace de Orquídeas del Combeima

Plataforma web de comercio electrónico para el emprendimiento familiar **Orquídeas del Combeima**, ubicado en Ibagué, Tolima. Permite explorar y comprar orquídeas y macetas, gestionar pedidos con pago en línea y administrar el catálogo completo desde un panel de administración.

**Sitio en producción:** https://marketplace-orquideas.vercel.app

---

## Tabla de contenido

- [Descripción del proyecto](#descripción-del-proyecto)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Pre-requisitos](#pre-requisitos)
- [Instalación y configuración local](#instalación-y-configuración-local)
- [Ejecución local](#ejecución-local)
- [Endpoints de la API](#endpoints-de-la-api)
- [Datos semilla](#datos-semilla)
- [Equipo](#equipo)

---

## Descripción del proyecto

OrquiCombeima es una SPA (Single Page Application) con backend REST que ofrece:

- **Catálogo** de orquídeas y macetas con filtros, paginación y detalle de producto.
- **Autenticación** con Google OAuth2 y tokens JWT.
- **Carrito de compras** con reserva temporal de stock.
- **Pasarela de pagos** Wompi (PSE y tarjetas de débito/crédito).
- **Panel de administración** para gestionar productos, pedidos, usuarios, guías de cuidado y el contenido de la página.
- **Chatbot** de atención al cliente impulsado por Google Gemini AI.
- **Guías de cuidado** de orquídeas por variedad.
- **Integración con WhatsApp Business** para el contacto directo con el emprendimiento.
- **Gestión de imágenes** con Cloudinary.

---

## Stack tecnológico

### Backend
| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.11 |
| Spring Security + OAuth2 | (incluido en Spring Boot) |
| jjwt | 0.12.6 |
| Cloudinary SDK | 1.39.0 |
| MySQL Connector/J | (incluido en Spring Boot) |
| Lombok | (incluido en Spring Boot) |
| Maven Wrapper | incluido en el repo |

### Frontend
| Tecnología | Versión |
|---|---|
| React | ^19.2.4 |
| Vite | ^8.0.1 |
| React Router DOM | ^7.14.0 |
| Axios | ^1.14.0 |
| Zustand | ^5.0.12 |
| Recharts | ^3.8.1 |
| react-markdown | ^10.1.0 |

### Servicios externos
| Servicio | Uso |
|---|---|
| Aiven | Base de datos MySQL 8 gestionada en la nube |
| Google Cloud Console | OAuth 2.0 para autenticación |
| Cloudinary | Almacenamiento y CDN de imágenes |
| Wompi | Pasarela de pagos colombiana (PSE y tarjetas) |
| Google Gemini AI | Chatbot de atención al cliente |
| WhatsApp Business | Canal de contacto con el emprendimiento |
| Railway | Despliegue del backend |
| Vercel | Despliegue del frontend |

---

## Estructura del repositorio

```
Marketplace_Orquideas/
├── backend/                          # API REST — Spring Boot
│   ├── mvnw / mvnw.cmd               # Maven Wrapper (no requiere instalar Maven)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/orquicombeima/proyecto_orquideas/
│       │   ├── controller/           # Endpoints REST (público y admin)
│       │   ├── service/              # Lógica de negocio
│       │   ├── repository/           # Acceso a datos (JPA)
│       │   ├── model/                # Entidades JPA
│       │   ├── dto/                  # Objetos de transferencia de datos
│       │   ├── security/             # JWT, OAuth2, filtros de seguridad
│       │   ├── scheduler/            # Tareas programadas (liberación de reservas)
│       │   └── shared/config/        # Configuración global (CORS, Cloudinary, excepciones)
│       └── resources/
│           ├── application.properties
│           ├── application-local.properties  # ⚠️ gitignored — crearlo manualmente
│           └── data.sql              # Datos semilla (ejecutar manualmente)
│
└── frontend/                         # SPA — React + Vite
    ├── public/
    ├── src/
    │   ├── pages/                    # Vistas principales
    │   ├── components/               # Componentes reutilizables (layout, ui)
    │   ├── services/                 # Llamadas a la API con Axios
    │   ├── store/                    # Estado global con Zustand
    │   ├── hooks/                    # Custom hooks
    │   └── utils/                    # Utilidades
    ├── .env                          # ⚠️ gitignored — crearlo manualmente
    └── vercel.json                   # Reglas de rewrite para la SPA
```

---

## Pre-requisitos

Instalar el siguiente software antes de clonar el repositorio:

| Software | Versión mínima | Enlace |
|---|---|---|
| Git | ≥ 2.40 | https://git-scm.com/downloads |
| JDK 21 (Temurin recomendado) | 21.x | https://adoptium.net/temurin/releases/?version=21 |
| Node.js + npm | LTS ≥ 20 | https://nodejs.org/en/download |
| IntelliJ IDEA Community *(recomendado)* | última | https://www.jetbrains.com/idea/download/ |
| Postman *(opcional)* | última | https://www.postman.com/downloads/ |

> **Maven no requiere instalación.** El repositorio incluye el wrapper `mvnw` / `mvnw.cmd`.
>
> **MySQL local no es obligatorio.** El proyecto usa una instancia gestionada en Aiven.

---

## Instalación y configuración local

### 1. Clonar el repositorio

```bash
git clone https://github.com/Rosa-SDev/Marketplace_Orquideas.git
cd Marketplace_Orquideas
```

### 2. Configurar JAVA_HOME (Windows / PowerShell)

Si Java 21 está instalado en la ruta predeterminada:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version   # debe mostrar: openjdk version "21.x.x"
```

> Para hacerlo permanente: *Variables de entorno del sistema → Nuevo* `JAVA_HOME`, luego agregar `%JAVA_HOME%\bin` al `Path`.

### 3. Crear el archivo de variables del backend

Crear el archivo `backend/src/main/resources/application-local.properties` (está en `.gitignore`, nunca se sube al repo) con la siguiente estructura:

```properties
# Base de datos (Aiven)
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# Google OAuth2
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# Cloudinary
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# WhatsApp Business
WHATSAPP_NUMERO=
WHATSAPP_MENSAJE=

# JWT y administración
JWT_SECRET=
ADMIN_EMAIL=
FRONTEND_URL=http://localhost:5173

# Wompi
WOMPI_PUBLIC_KEY=
WOMPI_PRIVATE_KEY=
WOMPI_INTEGRITY_SECRET=
WOMPI_EVENTS_SECRET=

# Google Gemini AI
GEMINI_API_KEY=
```

> Los valores de cada variable los provee un integrante del equipo con acceso a los servicios. Consultar la documentación interna del proyecto (GCS).

### 4. Crear el archivo de variables del frontend

Crear el archivo `frontend/.env` (también en `.gitignore`) con:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WOMPI_PUBLIC_KEY=
```

> Solicitar el valor de `VITE_WOMPI_PUBLIC_KEY` a un integrante del equipo.

---

## Ejecución local

Abrir **dos terminales** en la raíz del repositorio.

### Terminal 1 — Backend (puerto 8080)

```bash
cd backend

# macOS / Linux
./mvnw clean install
./mvnw spring-boot:run

# Windows (PowerShell o CMD)
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

Salida esperada al iniciar correctamente:

```
Tomcat started on port 8080 (http) ...
Started ProyectoOrquideasApplication in X.XXX seconds
```

### Terminal 2 — Frontend (puerto 5173)

```bash
cd frontend
npm install
npm run dev
```

Salida esperada:

```
VITE v8.x  ready in xxx ms
➜  Local:   http://localhost:5173/
```

### Verificación rápida

| URL | Resultado esperado |
|---|---|
| http://localhost:5173 | Página de inicio del marketplace |
| http://localhost:8080/api/orquideas | JSON con lista paginada de orquídeas (200 OK) |
| http://localhost:8080/oauth2/authorization/google | Redirección al flujo de login con Google |
| http://localhost:8080/api/auth/me | Perfil del usuario autenticado (requiere JWT) |

---

## Endpoints de la API

Base URL local: `http://localhost:8080`

### Público (sin autenticación)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/orquideas` | Lista de orquídeas con filtros y paginación |
| GET | `/api/orquideas/{id}` | Detalle de una orquídea |
| GET | `/api/orquideas/{id}/recomendaciones` | Recomendaciones de maceta para una orquídea |
| GET | `/api/macetas` | Lista de macetas disponibles |
| GET | `/api/guia` | Lista de guías de cuidado |
| GET | `/api/guia/{variedad}` | Guía de cuidado por variedad de orquídea |
| GET | `/api/contenido` | Contenido dinámico de páginas |
| GET | `/api/contenido/{tipo}` | Contenido por tipo de página |
| GET | `/api/contacto` | Información de contacto del emprendimiento |
| GET | `/api/contacto-whatsapp/pedido/{idPedido}` | Link de WhatsApp para consulta de pedido |
| POST | `/api/chatbot` | Enviar mensaje al chatbot (Gemini AI) |
| GET | `/oauth2/authorization/google` | Iniciar flujo de login con Google |
| GET | `/api/auth/me` | Perfil del usuario autenticado (requiere JWT) |

### Cliente autenticado (requiere JWT)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/carrito` | Ver carrito del usuario |
| POST | `/api/carrito/agregar` | Agregar ítem al carrito |
| PUT | `/api/carrito/{idItem}/cantidad` | Actualizar cantidad de un ítem |
| DELETE | `/api/carrito/{idItem}` | Eliminar ítem del carrito |
| DELETE | `/api/carrito/vaciar` | Vaciar el carrito |
| POST | `/api/pedidos` | Crear pedido (inicia pago con Wompi) |
| GET | `/api/pedidos/historial` | Historial de pedidos del usuario |

### Administración (requiere rol ADMINISTRADOR)

| Método | Endpoint | Descripción |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/admin/orquideas` | CRUD de orquídeas |
| GET/POST/PUT/DELETE | `/api/admin/macetas` | CRUD de macetas |
| GET/POST/PUT/DELETE | `/api/admin/guia` | CRUD de guías de cuidado |
| GET/POST/PUT/DELETE | `/api/admin/recomendaciones` | CRUD de recomendaciones maceta-orquídea |
| GET/POST/PUT/DELETE | `/api/admin/contenido-pagina` | CRUD de contenido dinámico |
| GET | `/api/admin/pedidos` | Lista de pedidos |
| GET | `/api/admin/pedidos/recientes` | Pedidos recientes |
| GET | `/api/admin/clientes` | Lista de usuarios registrados |
| GET | `/api/admin/estadisticas` | Estadísticas del negocio |
| POST | `/api/webhook/wompi` | Webhook de eventos de pago Wompi |

---

## Datos semilla

El archivo `backend/src/main/resources/data.sql` contiene los registros iniciales de orquídeas, macetas, guías de cuidado y recomendaciones.

Se ejecuta **manualmente** una sola vez desde la herramienta de base de datos del IDE (IntelliJ → Database → consola SQL) apuntando a la instancia configurada en `DB_URL`.

> Si la base de datos ya tiene datos, no ejecutar el archivo nuevamente para evitar duplicados.

---

## Equipo

Proyecto académico desarrollado en la **Universidad de Ibagué** — Ingeniería de Sistemas.

| Nombre | Rol |
|---|---|
| Rosa Isabel Peña Yagüe | Backend |
| Juan Sebastián Gallego Villamil | Backend |
| Daniel Esteban Guzmán Rodríguez | Frontend / Diseño gráfico |
| Matthew Habib Corzo Torres | Frontend |

**Wiki completo del proyecto:** https://github.com/Rosa-SDev/Marketplace_Orquideas/wiki
