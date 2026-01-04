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

### Qué se hizo y por qué

La idea fue reutilizar la lógica ya implementada (modelos + DAOs) y adaptarla a un entorno web con arquitectura **MVC**:

- **Modelo:** clases Java que representan los datos (`Usuario`, `Libro`, `Prestamo`, `Rol`).
- **Controlador:** Servlets que reciben peticiones HTTP, validan datos, llaman DAOs y luego redirigen o reenvían a JSP.
- **Vista:** JSP que muestran la información y contienen formularios HTML para enviar datos a los Servlets.

### Estructura del módulo web

Carpetas principales:

- `TuGestionAmigaWeb/src/`
  - `controller/` (Servlets)
  - `dao/` (acceso a datos)
  - `model/` (POJOs)
  - `util/` (conexión JDBC para web)
  - `filter/` y `listener/` (seguridad básica y carga de configuración)

- `TuGestionAmigaWeb/web/`
  - `index.jsp`
  - `jsp/` (vistas)
  - `css/` (estilos)
  - `js/` (scripts)
  - `WEB-INF/web.xml` (configuración y mapeos)

### Configuración de base de datos (módulo web)

En el módulo web, la conexión NO se lee desde la raíz del proyecto como en consola.

Aquí se lee desde:

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

### Cómo funciona la conexión en web

- `util.ConexionBD` lee `WEB-INF/db.properties` usando `ServletContext.getResourceAsStream(...)`.
- `listener.AppContextListener` carga las propiedades al iniciar la aplicación y las guarda en el `ServletContext`.
- Cada DAO recibe el `ServletContext` para abrir conexiones con `ConexionBD.getConnection(context)`.

### Seguridad básica (sesión)

- `LoginServlet` valida el usuario (correo/contraseña) y guarda el objeto en sesión como `usuarioLogueado`.
- `filter.AuthFilter` bloquea el acceso a las páginas internas si no existe sesión.
- `LogoutServlet` invalida la sesión.

### Rutas principales

Estas rutas están definidas en `web.xml`:

- `GET/POST /login`
- `GET/POST /register`
- `GET /logout`
- `GET /dashboard`
- `GET/POST /usuarios`
- `GET/POST /libros`
- `GET/POST /prestamos`

### Cambios de esta fase (módulo web)

En esta fase se agregaron/mejoraron estas partes:

- **Registro de usuario (pantalla pública)**
  - Ruta nueva: `/register`.
  - `controller.RegisterServlet` + `jsp/register.jsp`.
  - Validaciones simples: campos obligatorios, contraseñas coinciden, correo único.
  - Al registrarse correctamente, se muestra un mensaje en `/login`.

- **Mejoras visuales (CSS)**
  - Se mejoró el estilo general en `web/css/styles.css`.
  - Login y registro se ajustaron para verse centrados y con diseño tipo “tarjeta”.

- **Controlador JDBC (para evitar "No suitable driver")**
  - Se asegura que el conector `mysql-connector-j` esté disponible en el despliegue.
  - En `util.ConexionBD` se fuerza la carga del driver para que sea más fácil detectar si falta el `.jar`.

### Explicación detallada del flujo y clases principales

#### 1) `web.xml` (configuración web)

En `TuGestionAmigaWeb/web/WEB-INF/web.xml` se definen:

- Los **mapeos** de cada Servlet (por ejemplo, `/login`, `/usuarios`, etc.).
- El **filtro** `AuthFilter` para controlar acceso por sesión.
- El **listener** `AppContextListener` para cargar configuración al iniciar.

#### 2) Inicio de sesión

- `controller.LoginServlet`
  - **GET:** muestra `jsp/login.jsp`.
  - **POST:** lee `correo` y `contrasena`, consulta en BD con `dao.UsuarioDAO.findByCorreoYContrasena(...)`.
  - Si el usuario existe, guarda en sesión `usuarioLogueado` y redirige a `/dashboard`.

- `controller.LogoutServlet`
  - Invalida la sesión y redirige a `/login`.

#### 3) Protección de páginas (sesión)

- `filter.AuthFilter`
  - Permite acceder sin sesión a:
    - `/login`
    - `/register`
    - `index.jsp`
    - recursos estáticos (`/css/`, `/js/`)
  - Para cualquier otra ruta, exige que exista `usuarioLogueado` en sesión.

#### 4) Dashboard

- `controller.DashboardServlet`
  - Reenvía a `jsp/dashboard.jsp`.
  - La JSP lee `usuarioLogueado` desde sesión para mostrar el nombre/correo.

#### 5) CRUD de usuarios

- `controller.UsuarioServlet`
  - Usa el parámetro `accion` para decidir qué hacer:
    - **GET listar:** carga lista y muestra `jsp/usuarios.jsp`.
    - **GET editar:** carga un usuario por id y lo pone en `usuarioEdit` para rellenar el formulario.
    - **POST crear/actualizar/eliminar:** ejecuta el DAO y luego redirige a listar.
  - Maneja mensajes simples con `session.setAttribute("mensaje"/"error")`.

- `jsp/usuarios.jsp`
  - Contiene:
    - Formulario de registro/edición.
    - Tabla con el listado.
    - Botón para eliminar con confirmación.

#### 6) CRUD de libros

- `controller.LibroServlet`
  - Misma idea que usuarios (`accion=listar|editar|crear|actualizar|eliminar`).
  - Reenvía a `jsp/listarLibros.jsp`.

#### 7) Préstamos y devoluciones

- `controller.PrestamoServlet`
  - **GET:** carga usuarios, libros y préstamos para armar los combos/listado en `jsp/prestamos.jsp`.
  - **POST registrarPrestamo:** llama `dao.PrestamoDAO.registrarPrestamo(...)`.
  - **POST registrarDevolucion:** llama `dao.PrestamoDAO.registrarDevolucion(...)`.
  - **POST eliminar:** elimina el préstamo por id.

En `dao.PrestamoDAO`:

- `registrarPrestamo(...)` usa transacción y `FOR UPDATE` para evitar inconsistencias.
- `registrarDevolucion(...)` cambia el estado a `DEVUELTO` y vuelve a poner el libro disponible.

### Cómo ejecutar el módulo web en NetBeans con Tomcat 9

Este repositorio actualmente tiene un proyecto NetBeans de tipo **Java SE** (módulo consola). Para el módulo web, lo recomendado es crear un proyecto web adicional (en NetBeans) apuntando a la carpeta `TuGestionAmigaWeb/`.

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

### Cómo probar esta fase del módulo web (paso a paso)

#### Prueba 0: ver que carga el login

1) Abre la aplicación web.
2) Debe mostrar el login en `/login`.

#### Prueba 1: registrarse (nuevo)

1) En el login, entra al enlace **“Regístrate aquí”**.
2) Completa el formulario y crea tu cuenta.
3) Debe redirigir a `/login` y mostrar el mensaje de registro exitoso.

#### Prueba 2: iniciar sesión

1) Inicia sesión con el correo/contraseña que registraste.
2) Debe entrar al `/dashboard`.

#### Prueba 3: CRUD web (validación completa)

1) **Usuarios**
   - Crear usuario.
   - Editar usuario.
   - Eliminar usuario.

2) **Libros**
   - Crear libro disponible.
   - Editar libro.
   - Eliminar libro.

3) **Préstamos**
   - Registrar un préstamo (debe cambiar la disponibilidad del libro).
   - Registrar la devolución (debe volver a disponible).

### Nota sobre errores de importación `javax.servlet`

Si abres la carpeta `TuGestionAmigaWeb/src` como un proyecto Java normal, NetBeans puede mostrar errores como “no se encuentra `javax.servlet`”. Esto se resuelve cuando el proyecto se crea como **Aplicación web** y queda asociado a **Tomcat 9** (que aporta las librerías del API Servlet).

### Solución de problemas (módulo web)

- **Falla el despliegue por permisos (Program Files)**
  - Si Tomcat está instalado en `C:\Program Files\...`, NetBeans puede fallar al copiar archivos de contexto.
  - Soluciones típicas:
    - Ejecutar NetBeans como administrador.
    - Configurar un **Catalina Base** en una carpeta con permisos (por ejemplo en `C:\Users\...`).

- **"No suitable driver found"**
  - Falta el conector JDBC de MySQL en el proyecto web o en el despliegue.
  - Verifica que `mysql-connector-j` esté agregado a Bibliotecas o dentro de `TuGestionAmigaWeb/web/WEB-INF/lib/`.
