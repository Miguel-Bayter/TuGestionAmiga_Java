# Tu Gestión Amiga

Proyecto de gestión de biblioteca con dos partes:

- **Parte 1: Aplicación de consola (Java SE)** con menú y CRUD.
- **Parte 2: Módulo web (Servlets + JSP)** para ejecutar en **Tomcat 9**.

Ambas partes usan la misma base de datos en MySQL.

# Parte 1 - Aplicación de consola (Java SE)

## Requisitos

- Java JDK 17+ (o compatible con tu configuración de NetBeans)
- MySQL 8+ (o compatible)
- Controlador JDBC de MySQL: **mysql-connector-j** agregado al proyecto (Bibliotecas del proyecto en NetBeans)

## Base de datos

El proyecto incluye scripts SQL en la carpeta `sql/`.

- **Opción recomendada:** ejecutar `sql/TuGestionAmiga_db.sql` (crea DB y tablas principales).
- Alternativamente, puedes usar los dumps individuales:
  - `sql/tugestionamiga_db_rol.sql`
  - `sql/tugestionamiga_db_usuario.sql`
  - `sql/tugestionamiga_db_categoria.sql`
  - `sql/tugestionamiga_db_libro.sql`
  - `sql/tugestionamiga_db_prestamo.sql`
  - `sql/tugestionamiga_db_compra.sql`

Tablas principales usadas por la aplicación:

- `usuario` (usuarios del sistema)
- `libro` (catálogo de libros, con campo `disponibilidad`)
- `prestamo` (registro de préstamos y devoluciones)

## Configuración de conexión

La clase `tugestionamiga.config.DatabaseConnection` intenta leer un archivo `db.properties` en la raíz del proyecto.
 
 Como el archivo se lee con una ruta relativa (`db.properties`), es importante que el *directorio de trabajo* al ejecutar apunte a la carpeta raíz del proyecto.

### Archivo `db.properties`

Crea/edita el archivo `db.properties` (en la carpeta raíz del proyecto) con este formato:

```properties
# URL JDBC (ajusta host/puerto si es necesario)
db.url=jdbc:mysql://localhost:3306/tugestionamiga_db?useSSL=false&serverTimezone=UTC

# Usuario/clave de MySQL
# (usa tu usuario real; por defecto el código intenta root con clave vacía)
db.user=root
db.password=<tu_password>
```

## Cómo está organizado el código (arquitectura)

La aplicación está separada por paquetes para que cada parte tenga una responsabilidad clara:

- `tugestionamiga.config`
  - **Qué hace:** configuración de conexión a MySQL.
  - **Clase principal:** `DatabaseConnection`
    - Lee `db.properties`.
    - Devuelve una conexión JDBC usando `DriverManager`.

- `tugestionamiga.model`
  - **Qué hace:** contiene las clases que representan los datos (POJOs).
  - **Clases:**
    - `Usuario`  -> tabla `usuario`
    - `Libro`    -> tabla `libro`
    - `Prestamo` -> tabla `prestamo`
    - `Rol` y `Categoria` están incluidos porque existen en el esquema.
  - Estas clases tienen atributos + getters/setters y un `toString()` para mostrar información en consola.

- `tugestionamiga.dao`
  - **Qué hace:** encapsula el acceso a datos (consultas SQL) y evita que el menú tenga SQL “mezclado”.
  - **Clases:**
    - `UsuarioDAO`: CRUD en `usuario`.
    - `LibroDAO`: CRUD en `libro`.
    - `PrestamoDAO`: CRUD en `prestamo` + flujo de **prestar** / **devolver**.

- `tugestionamiga.util`
  - **Qué hace:** utilidades.
  - `ConsoleInput`: centraliza lectura y validación (enteros, strings, fechas).

- `tugestionamiga.main`
  - **Qué hace:** contiene el punto de entrada.
  - `Application`: muestra menús, lee opciones, llama a los DAOs y muestra resultados.

## Funcionamiento interno (qué pasa cuando eliges una opción)

La ejecución típica es:

1) `Application` muestra un menú.
2) `ConsoleInput` lee los valores y valida formato.
3) `Application` llama al DAO correspondiente.
4) El DAO abre una conexión con `DatabaseConnection.getConnection()`.
5) El DAO ejecuta la consulta SQL con `PreparedStatement`.
6) Se mapean resultados a objetos `model` (cuando aplica) y se muestran en consola.

### Flujo de prestar un libro (opción "Registrar préstamo")

Implementado en `PrestamoDAO.registrarPrestamo(...)`:

1) Abre conexión y desactiva la confirmación automática (autocommit) (inicia transacción).
2) Consulta el libro con `FOR UPDATE` para bloquear la fila mientras dura la operación.
3) Verifica que `disponibilidad = 1`.
4) Inserta el préstamo en `prestamo` con estado `ACTIVO`.
5) Actualiza `libro.disponibilidad = 0`.
6) Si todo sale bien, confirma los cambios (`commit`). Si algo falla, deshace los cambios (`rollback`).

### Flujo de devolución (opción "Registrar devolución")

Implementado en `PrestamoDAO.registrarDevolucion(...)`:

1) Inicia transacción.
2) Busca el préstamo y obtiene el `id_libro`.
3) Cambia el préstamo a `DEVUELTO`.
4) Cambia el libro a `disponibilidad = 1`.
5) Confirmación (`commit`) / deshacer (`rollback`) según corresponda.

## Funcionalidades

### Usuarios (CRUD)

- Crear usuarios (nombre, correo, contraseña, id_rol opcional)
- Listar usuarios
- Actualizar usuarios
- Eliminar usuarios

### Libros (CRUD)

- Crear libros (título, autor, descripción, disponibilidad, categoría opcional)
- Listar libros
- Actualizar libros
- Eliminar libros

### Préstamos (CRUD + flujo de préstamo/devolución)

- Crear préstamo manual (para pruebas o carga directa)
- Listar préstamos
- Actualizar préstamos
- Eliminar préstamos

Además, incluye dos operaciones principales:

- **Registrar préstamo (prestar libro):**
  - Valida que el libro exista y esté disponible.
  - Inserta un registro en `prestamo` con estado `ACTIVO`.
  - Cambia `libro.disponibilidad` a `0`.

- **Registrar devolución:**
  - Marca el préstamo como `DEVUELTO`.
  - Cambia `libro.disponibilidad` a `1`.

Estas operaciones se realizan dentro de una **transacción** para asegurar consistencia.

## Cómo ejecutar en Apache NetBeans (paso a paso)

### 1) Crear el proyecto en NetBeans usando el código existente

Si esta carpeta no tiene `nbproject/`, NetBeans no la reconoce como proyecto todavía. La opción más práctica es:

1) **Archivo > Nuevo proyecto**
2) **Java con Ant**
3) **Proyecto Java con fuentes existentes**
4) Agregar como **Carpeta de paquetes fuente**:
  - `...\TuGestionAmiga\src`
5) Finalizar.

### 2) Configurar la clase principal

1) Click derecho al proyecto > **Propiedades**
2) **Ejecución**
3) En **Clase principal** selecciona/escribe:
  - `tugestionamiga.main.Application`

### 3) Agregar el controlador JDBC de MySQL

1) Click derecho al proyecto > **Propiedades**
2) **Bibliotecas** > **Agregar JAR/Carpeta...**
3) Selecciona el `.jar` de `mysql-connector-j`.

### 4) Configurar el directorio de trabajo (para que lea db.properties)

`DatabaseConnection` lee `db.properties` con una ruta relativa. Para evitar errores:

1) Proyecto > **Propiedades** > **Ejecución**
2) En **Directorio de trabajo** selecciona la carpeta raíz:
  - `...\TuGestionAmiga`

### 5) Preparar la base de datos

1) Ejecuta `sql/TuGestionAmiga_db.sql`.
2) Verifica que existe la base `tugestionamiga_db`.
3) Verifica que existen las tablas `usuario`, `libro`, `prestamo`.

### 6) Ejecutar

Ejecuta la clase `tugestionamiga.main.Application`.

## Cómo probar (paso a paso)

Al iniciar verás:

- `1. Usuarios`
- `2. Libros`
- `3. Préstamos`

### Prueba mínima recomendada (para validar todo)

1) **Crear un usuario**
   - Menú: `Usuarios` > `Crear`
   - Usa un correo único (por ejemplo `test@mail.com`).

2) **Crear un libro disponible**
   - Menú: `Libros` > `Crear`
   - Disponible: `1`

3) **Registrar préstamo**
   - Menú: `Préstamos` > `Registrar préstamo`
   - Ingresa `idUsuario` y `idLibro` creados.
   - Fechas en formato `AAAA-MM-DD`.

4) **Verificar cambios**
   - Menú: `Préstamos` > `Listar` (debe aparecer `ACTIVO`).
   - Menú: `Libros` > `Listar` (el libro debe quedar `disponible=false`).

5) **Registrar devolución**
   - Menú: `Préstamos` > `Registrar devolución`
   - Ingresa `idPrestamo`.
   - Verifica que el préstamo queda `DEVUELTO` y el libro vuelve a `disponible=true`.

### Verificación en MySQL (opcional)

Puedes confirmar rápido desde MySQL Workbench:

```sql
USE tugestionamiga_db;

SELECT * FROM usuario;
SELECT id_libro, titulo, disponibilidad FROM libro;
SELECT * FROM prestamo;
```

## Solución de problemas (errores comunes)

- **Acceso denegado**
  - Revisa `db.user` y `db.password` en `db.properties`.
  - Verifica que NetBeans está ejecutando con el *directorio de trabajo* en la carpeta raíz.

- **No se encontró un controlador JDBC adecuado**
  - Falta agregar `mysql-connector-j` como librería del proyecto.

- **La tabla no existe**
  - Asegúrate de haber ejecutado el script SQL y que `db.url` apunte a `tugestionamiga_db`.

 ## Notas
 
 - En MySQL la columna de contraseña se llama `contraseña` (incluye `ñ`). En el código se usa backtick y/o alias SQL para evitar inconvenientes al mapear resultados.
 - Si intentas prestar un libro no disponible, la operación se rechaza.
 

# Parte 2 - Módulo Web (Servlets + JSP) - `TuGestionAmigaWeb`

Además del módulo de consola, el repositorio incluye un módulo web dentro de la carpeta `TuGestionAmigaWeb/` pensado para ejecutarse con **Tomcat 9**.

## Requisitos

- Java JDK 17+ (o compatible con tu NetBeans)
- Apache Tomcat 9
- MySQL 8+
- Controlador JDBC: **mysql-connector-j** agregado al proyecto web

## Configuración de base de datos (módulo web)

En el módulo web la configuración se lee desde:

- `TuGestionAmigaWeb/web/WEB-INF/db.properties`

Se dejó un ejemplo listo:

- `TuGestionAmigaWeb/web/WEB-INF/db.properties.example`

Pasos recomendados:

1) Copia `db.properties.example`.
2) Renómbralo a `db.properties`.
3) Completa `db.user` y `db.password`.

Ejemplo:

```properties
db.url=jdbc:mysql://localhost:3306/tugestionamiga_db?useSSL=false&serverTimezone=UTC
db.user=root
db.password=123456
```

**Nota:** `db.properties` está ignorado por `.gitignore` para no subir credenciales.

## Cómo ejecutar el módulo web en NetBeans con Tomcat 9

Este repositorio tiene un proyecto NetBeans de tipo **Java SE** (módulo consola). Para el módulo web, lo recomendado es crear un proyecto web adicional en NetBeans apuntando a la carpeta `TuGestionAmigaWeb/`.

Pasos:

1) En NetBeans, agrega Tomcat 9:
   - **Servicios** > **Servidores** > **Agregar servidor** > Apache Tomcat o TomEE.
   - Selecciona la instalación de **Tomcat 9**.

2) Crea el proyecto web usando fuentes existentes:
   - **Archivo > Nuevo proyecto**
   - Categoría: **Java Web**
   - Opción: **Aplicación web con fuentes existentes** (o similar)
   - Carpeta del proyecto: `...\TuGestionAmiga\TuGestionAmigaWeb`

3) Configura la versión del servlet/JSP:
   - Tomcat 9 usa `javax.servlet.*`.

4) Agrega el controlador JDBC de MySQL al proyecto web:
   - Click derecho al proyecto web > **Propiedades** > **Bibliotecas**
   - Agrega el `.jar` de `mysql-connector-j`.

   Alternativa práctica (si prefieres que quede dentro del proyecto):
   - Colocar el `.jar` dentro de `TuGestionAmigaWeb/web/WEB-INF/lib/`.

5) Prepara la base de datos:
   - Ejecuta `sql/TuGestionAmiga_db.sql` (si no lo has hecho).
   - Verifica que exista `tugestionamiga_db`.

6) Crea el archivo `db.properties` del módulo web:
   - Copia `TuGestionAmigaWeb/web/WEB-INF/db.properties.example` a `TuGestionAmigaWeb/web/WEB-INF/db.properties`.
   - Completa la contraseña.

7) Ejecuta:
   - Click derecho al proyecto web > **Ejecutar**.

## Cómo probar el módulo web (paso a paso)

### Prueba 0: ver que carga el login

1) Abre la aplicación web.
2) Debe mostrar el login en `/login`.

### Prueba 1: registrarse

1) En el login, entra al enlace **“Regístrate aquí”**.
2) Completa el formulario y crea tu cuenta.
3) Debe redirigir a `/login` y mostrar el mensaje de registro exitoso.

### Prueba 2: iniciar sesión

1) Inicia sesión con el correo/contraseña que registraste.
2) Debe entrar al `/dashboard`.

### Prueba 3: CRUD web

1) **Usuarios**
   - Crear usuario.
   - Editar usuario.
   - Eliminar usuario (no se permite eliminar el mismo usuario que tiene la sesión iniciada).

2) **Libros**
   - (Si quieres probar el selector de Género/Categoría) asegúrate de tener categorías:

```sql
USE tugestionamiga_db;
INSERT INTO categoria (nombre_categoria) VALUES
('Novela'),('Ciencia ficción'),('Fantasía');
```

   - Crear libro disponible y seleccionar un **Género/Categoría**.
   - Editar libro.
   - Eliminar libro.

3) **Préstamos**
   - Registrar un préstamo (debe cambiar la disponibilidad del libro).
   - Registrar la devolución (debe volver a disponible).

## Roles y permisos (ADMIN / USUARIO)

En el módulo web se separaron permisos para que la aplicación muestre y permita acciones distintas según el rol del usuario.

- **ADMIN**
  - Acceso a la sección **Usuarios** (CRUD).
  - En **Libros**: crear/editar/eliminar.
  - En **Préstamos**: ver todos, registrar devoluciones y eliminar.

- **USUARIO**
  - No ve la sección **Usuarios**.
  - En **Libros**: solo listado.
  - En **Préstamos**: ve únicamente sus préstamos y al registrar un préstamo se usa su usuario de sesión.

### Crear roles y asignar un administrador

El esquema incluye la tabla `rol`. Para que la convención del código funcione (ADMIN=1, USUARIO=2), puedes insertar roles así:

```sql
INSERT INTO rol (id_rol, nombre_rol) VALUES (1, 'ADMIN')
  ON DUPLICATE KEY UPDATE nombre_rol = 'ADMIN';

INSERT INTO rol (id_rol, nombre_rol) VALUES (2, 'USUARIO')
  ON DUPLICATE KEY UPDATE nombre_rol = 'USUARIO';
```

Luego, para convertir un usuario existente en administrador:

```sql
UPDATE usuario SET id_rol = 1 WHERE correo = 'admin@mail.com';
```

Si un usuario no tiene `id_rol` (NULL), el sistema lo trata como usuario normal para no romper datos antiguos.

## Nota sobre errores de importación `javax.servlet`

Si abres la carpeta `TuGestionAmigaWeb/src` como un proyecto Java normal, NetBeans puede mostrar errores como “no se encuentra `javax.servlet`”. Esto se resuelve cuando el proyecto se crea como **Aplicación web** y queda asociado a **Tomcat 9** (que aporta las librerías del API Servlet).

## Solución de problemas (módulo web)

- **Falla el despliegue por permisos (Program Files)**
  - Si Tomcat está instalado en `C:\Program Files\...`, NetBeans puede fallar al copiar archivos de contexto.
  - Soluciones típicas:
    - Ejecutar NetBeans como administrador.
    - Configurar un **Catalina Base** en una carpeta con permisos (por ejemplo en `C:\Users\...`).

- **"No suitable driver found"**
  - Falta el conector JDBC de MySQL en el proyecto web o en el despliegue.
  - Verifica que `mysql-connector-j` esté agregado a Bibliotecas o dentro de `TuGestionAmigaWeb/web/WEB-INF/lib/`.


# Parte 3 - Implementación de framework (Servlets/JSP y Spring Boot)

En esta parte se explica la estructura tipo framework que se aplicó al proyecto y el módulo stand-alone agregado con **Spring Boot**.

## 1) Enfoque del repositorio (dos módulos)

En el repositorio conviven dos enfoques:

1) **Web clásico (Servlets + JSP)**
   - Carpeta: `TuGestionAmigaWeb/`
   - Se ejecuta en **Tomcat 9**.
   - Arquitectura en capas: `controller/` → `service/` → `dao/` → `model/`.

### Mejora aplicada en el módulo web: selector de Categoría/Género en Libros

En la pantalla de **Libros** se mejoró el campo “ID Categoría” para que no se tenga que memorizar un número.
Ahora el formulario muestra un **selector** con los géneros disponibles (tabla `categoria`) y guarda el `id_categoria` correspondiente.

Archivos agregados/modificados:

- `TuGestionAmigaWeb/src/model/Categoria.java`
  - Representa un género/categoría (`id_categoria`, `nombre_categoria`).
- `TuGestionAmigaWeb/src/dao/CategoriaDAO.java`
  - Consulta las categorías con `findAll()`.
- `TuGestionAmigaWeb/src/service/CategoriaService.java`
  - Expone `listarCategorias()` para que el servlet no tenga SQL.
- `TuGestionAmigaWeb/src/controller/LibroServlet.java`
  - Carga la lista de categorías y la pasa a la JSP como `categorias`.
- `TuGestionAmigaWeb/web/jsp/listarLibros.jsp`
  - Reemplaza el input numérico por un `<select>` con “ID - Nombre”.
  - En el listado muestra la categoría en formato “ID - Nombre”.

Además, se agregaron categorías de ejemplo en los scripts SQL para que el selector tenga opciones en una instalación nueva:

- `sql/TuGestionAmiga_db.sql`
- `sql/tugestionamiga_db_categoria.sql`

2) **Stand-alone con Spring Boot**
   - Carpeta: `TuGestionAmigaSpring/`
   - Se ejecuta como aplicación stand-alone (servidor embebido).
   - Stack: **Spring Boot + Thymeleaf + Spring JDBC + Spring Security**.

### Qué problema resuelve el módulo Spring

El módulo `TuGestionAmigaSpring` permite ejecutar la aplicación sin un Tomcat externo.
Además, la estructura queda más parecida a un “framework” (controladores, vistas, seguridad y repositorios) y se mantiene la misma base de datos.

## 2) Framework usado en el módulo Spring (`TuGestionAmigaSpring`)

El módulo `TuGestionAmigaSpring` se agregó para que la aplicación se pueda ejecutar sin Tomcat externo.

- **Spring Boot**
  - Aporta el arranque automático y el servidor embebido.
- **Spring MVC**
  - Controladores web con anotaciones (`@Controller`, `@GetMapping`).
- **Thymeleaf**
  - Reemplaza JSP para renderizar vistas del lado servidor.
- **Spring JDBC**
  - Manejo de conexión a MySQL por `DataSource` (sin JPA/Hibernate por ahora).
- **Spring Security**
  - Maneja el flujo de autenticación (login/logout) y protege rutas.

### Rutas base (Spring)

- `GET /login` muestra el formulario.
- `POST /login` lo procesa Spring Security.
- `GET /dashboard` es una ruta protegida (solo logueados).
- `POST /logout` cierra sesión.

## 3) Seguridad con base de datos existente (usuario/rol)

La autenticación se conecta a la misma base MySQL del proyecto.

- Tabla `usuario`:
  - `correo` se usa como `username`.
  - La columna de contraseña se llama ``contraseña`` (incluye `ñ`).

- Tabla `rol`:
  - `nombre_rol` se convierte en autoridad con el prefijo `ROLE_` (por ejemplo `ROLE_ADMIN`).

En `SecurityConfig` se definieron queries JDBC para que Spring Security consulte:

- Usuario + contraseña
- Roles/autorizaciones

Nota: por compatibilidad con el esquema actual, se usa `{noop}` para validar contraseñas en texto plano.

## 4) Configuración de BD en Spring (opción C)

Para no subir credenciales, se dejó un archivo de plantilla y el archivo real queda ignorado por Git.

1) Copia:
   - `TuGestionAmigaSpring/src/main/resources/application.properties.example`
   a:
   - `TuGestionAmigaSpring/src/main/resources/application.properties`

2) Edita `application.properties` con tu configuración MySQL.

También puedes usar variables de entorno:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

## 5) Esquema de base de datos usado por el módulo Spring

El módulo Spring trabaja sobre la misma base `tugestionamiga_db` y depende de estas columnas en la tabla `libro`:

- `stock` (inventario)
- `valor` (precio unitario del catálogo)

Si creaste la base desde cero con `sql/TuGestionAmiga_db.sql`, ambas columnas ya deben estar definidas.

Si tu base ya existía antes, aplica estas migraciones:

```sql
USE tugestionamiga_db;

-- 1) Inventario
ALTER TABLE libro
ADD COLUMN stock INT NOT NULL DEFAULT 0;

-- 2) Precio unitario del catálogo
ALTER TABLE libro
ADD COLUMN valor DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Recalcular disponibilidad por consistencia
UPDATE libro
SET disponibilidad = (stock > 0);
```

## 5) Mejoras implementadas en el módulo Spring (`TuGestionAmigaSpring`)

En el módulo Spring se agregaron pantallas y flujos para que la experiencia sea equivalente (visual y funcional) al módulo web.

- **Frontend (Thymeleaf) con layout reutilizable**
  - Se crearon fragmentos para reutilizar el layout (navbar + sidebar) en todas las vistas.
  - Se portaron estilos y scripts del módulo web a `static/`.

- **Autenticación y registro**
  - Pantalla de **login** y **registro** con validaciones (campos requeridos, contraseñas coinciden, correo único).
  - Integración con **Spring Security** usando la base existente (`usuario`/`rol`).

- **CRUD de Usuarios (solo ADMIN)**
  - Listar / crear / editar / eliminar.
  - Protección para evitar eliminar el usuario con la sesión actual.

- **CRUD de Libros + Categorías (ADMIN para modificar)**
  - Listado para todos.
  - Crear/editar/eliminar solo para ADMIN.
  - Selector de categoría (género) igual al módulo web.

- **Stock en libros + consistencia automática de disponibilidad**
  - Se agregó el campo `stock` a nivel de aplicación.
  - La disponibilidad se calcula como `stock > 0`.
  - **Nota:** para usar stock en la BD se requiere agregar la columna en MySQL (ver script más abajo).

- **Valor del libro (precio unitario de catálogo)**
  - Se agregó el campo `valor` en Libros (visible y editable solo para ADMIN).
  - En Compras el usuario no escribe el precio: se toma el `valor` del libro.

- **Préstamos con transacciones (stock)**
  - Registrar préstamo: valida stock, registra préstamo y descuenta stock.
  - Registrar devolución: marca DEVUELTO y repone stock.
  - Validación de fechas: la **fecha de préstamo debe ser la actual** y la devolución no puede ser anterior.
  - Cantidad: se permite prestar más de 1 ejemplar del mismo libro (si hay stock).

- **Compras (nuevo apartado)**
  - Registro y listado de compras (`compra`).
  - La compra registra el movimiento y actualiza stock de forma transaccional.
  - Fecha fija: la compra se registra con la fecha actual.
  - Precio controlado: el precio se calcula desde el `valor` del libro y el usuario solo decide la cantidad.

- **Autollenado desde Libros (USUARIO)**
  - En el listado de Libros, cada fila tiene accesos directos a Préstamo/Compra.
  - Al entrar a `/prestamos?idLibro=...` o `/compras?idLibro=...` el selector queda preseleccionado.

- **Perfil del usuario (nuevo apartado)**
  - Ruta `/perfil` con resumen del usuario en sesión.
  - Tablas con libros **comprados** y **prestados** (incluyendo DEVUELTO) usando `JOIN` a `libro`.

### Script de migración (requerido para stock/valor)

El esquema original solo trae `disponibilidad`. Para habilitar stock en MySQL:

```sql
USE tugestionamiga_db;

ALTER TABLE libro
ADD COLUMN stock INT NOT NULL DEFAULT 0;

ALTER TABLE libro
ADD COLUMN valor DECIMAL(10,2) NOT NULL DEFAULT 0.00;

UPDATE libro
SET disponibilidad = (stock > 0);
```

## 6) Script para crear roles y un ADMIN (para probar)

```sql
USE tugestionamiga_db;

-- Si ejecutaste sql/TuGestionAmiga_db.sql, los roles ADMIN/USUARIO ya vienen creados.
-- Solo necesitas crear el usuario administrador de prueba.

INSERT INTO usuario (nombre, correo, `contraseña`, id_rol)
VALUES ('Administrador', 'admin@mail.com', 'admin123', 1)
ON DUPLICATE KEY UPDATE
  nombre = 'Administrador',
  `contraseña` = 'admin123',
  id_rol = 1;
```

## 6) Cómo probar el módulo Spring Boot

### Opción A: ejecutar desde NetBeans (sin `mvn`)

Si Windows te muestra que `mvn` no existe, puedes ejecutar con NetBeans:

1) `File > Open Project...`
2) Abre la carpeta `TuGestionAmigaSpring/`.
3) NetBeans debe reconocerlo como proyecto Maven.
4) Click derecho al proyecto → `Run`.

Luego abre:

- `http://localhost:8080/login`

Credenciales de prueba:

- Correo: `admin@mail.com`
- Contraseña: `admin123`

Debe redirigir a `/dashboard` y mostrar el usuario y sus roles.

### Prueba recomendada (flujo completo con stock, compras y perfil)

Requisito: haber ejecutado `sql/TuGestionAmiga_db.sql` (ya incluye `categoria`, `rol` y la columna `stock` en `libro`).

1) **Iniciar sesión**
   - URL: `http://localhost:8080/login`
   - Usuario: `admin@mail.com`
   - Clave: `admin123`

2) **Crear un libro con stock y valor**
   - Ir a: `http://localhost:8080/libros`
   - Crear un libro con:
     - Título / Autor (cualquiera)
     - Stock: `3`
     - Valor: por ejemplo `25.00`
   - Validar en el listado que:
     - `Stock = 3`
     - `Disponible = Sí`

3) **Registrar un préstamo (consume stock)**
   - Ir a: `http://localhost:8080/prestamos`
   - Registrar un préstamo con:
     - Fecha préstamo: <strong>hoy</strong> (la app no permite fechas pasadas/futuras)
     - Fecha devolución: hoy o una fecha futura
   - Validar en `Libros` que el stock bajó en 1.

4) **Registrar devolución (reponer stock)**
   - En `Préstamos`, registrar devolución con el ID del préstamo.
   - Validar en `Libros` que el stock subió en 1.

5) **Registrar una compra (consume stock y usa el valor del catálogo)**
   - Ir a: `http://localhost:8080/compras`
   - Registrar una compra con:
     - Fecha compra: hoy
     - Cantidad: por ejemplo `2`
     - Nota: el precio no se escribe, se toma del valor del libro
   - Validar en `Libros` que el stock bajó en 1.

6) **Ver Perfil**
   - Ir a: `http://localhost:8080/perfil`
   - Validar que aparezcan:
     - Las compras del usuario (tabla Compras)
     - Los préstamos (incluye ACTIVO/DEVUELTO)

### Opción B: ejecutar con Maven (si lo instalas)

```bash
mvn spring-boot:run
```

### Opción C: empaquetar y ejecutar como `.jar`

```bash
mvn -DskipTests package
java -jar target/tugestionamiga-spring-0.0.1-SNAPSHOT.jar
```

## 7) Documentación agregada en el código (Spring)

Para que el funcionamiento quede claro se agregaron Javadoc y descripciones en:

- `TuGestionAmigaSpringApplication` (arranque stand-alone)
- `SecurityConfig` (queries JDBC, roles y `{noop}`)
- `AuthController` (GET /login)
- `DashboardController` (ruta protegida para validar login)

## 8) Cómo probar el selector de Categoría/Género en Libros (módulo web)

Requisito: estar ejecutando el módulo web `TuGestionAmigaWeb` en Tomcat 9.

1) **Asegurar que existan categorías**
   - Si usaste `sql/TuGestionAmiga_db.sql` desde cero, ya se insertan categorías.
   - Si tu BD ya existía y no tiene datos, inserta algunas:

```sql
USE tugestionamiga_db;
INSERT INTO categoria (nombre_categoria) VALUES
('Novela'),('Ciencia ficción'),('Fantasía');
```

2) **Entrar a Libros**
   - Inicia sesión como ADMIN.
   - Ve a `/libros?accion=listar`.

3) **Crear un libro con categoría**
   - En el formulario “Registrar libro”, elige una opción en “Género / Categoría”.
   - Guarda.

4) **Validar en el listado**
   - En la columna “Categoría” debe verse “ID - Nombre”.

5) **Validar en base de datos (opcional)**

```sql
USE tugestionamiga_db;

SELECT l.id_libro, l.titulo, l.id_categoria, c.nombre_categoria
FROM libro l
LEFT JOIN categoria c ON c.id_categoria = l.id_categoria
ORDER BY l.id_libro;
```

Si seleccionas “-- Sin categoría --”, el libro se guarda con `id_categoria` en `NULL`.

