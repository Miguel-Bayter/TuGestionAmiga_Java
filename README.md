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
   - Crear libro disponible.
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

# Parte 3 - Implementación del framework (explicación)

En esta parte se describe cómo se implementó una estructura más ordenada (tipo framework) sobre la base clásica de **Servlets + JSP**, aplicando separación por capas y un flujo MVC más claro.

## Cómo ejecutar el módulo web (resumen)

Esta sección resume los pasos para ejecutar el módulo web desde NetBeans con Tomcat.

1) **Crear la base de datos**
   - Ejecuta el script SQL de la base (tablas y datos iniciales) en MySQL.

2) **Configurar la conexión (`db.properties`)**
   - Revisa `TuGestionAmigaWeb/web/WEB-INF/db.properties`.
   - Ajusta host, puerto, nombre de BD, usuario y contraseña según tu MySQL.

3) **Configurar el proyecto como Aplicación Web**
   - Abre el proyecto en NetBeans como proyecto web.
   - Asocia el servidor **Tomcat 9**.
   - Verifica que el conector JDBC de MySQL esté disponible (por ejemplo `mysql-connector-j` en bibliotecas o en `WEB-INF/lib`).

4) **Ejecutar**
   - Ejecuta el proyecto (Run).
   - Abre en el navegador la ruta de login.
     - Ejemplo (si el contexto se llama `TuGestionAmigaWeb`):
       - `http://localhost:8080/TuGestionAmigaWeb/login`

## Creación de ADMIN por script (roles y usuario)

El control de roles usa `usuario.id_rol` (FK a `rol`). Para que el módulo web reconozca el administrador se usa la convención:

- `id_rol = 1`  ADMIN
- `id_rol = 2`  USUARIO

### 1) Insertar roles (si aún no existen)

```sql
USE tugestionamiga_db;

INSERT INTO rol (id_rol, nombre_rol) VALUES (1, 'ADMIN')
  ON DUPLICATE KEY UPDATE nombre_rol = 'ADMIN';

INSERT INTO rol (id_rol, nombre_rol) VALUES (2, 'USUARIO')
  ON DUPLICATE KEY UPDATE nombre_rol = 'USUARIO';
```

### 2) Crear un admin nuevo (o convertir si el correo ya existe)

```sql
USE tugestionamiga_db;

INSERT INTO usuario (nombre, correo, `contraseña`, id_rol)
VALUES ('Administrador', 'admin@mail.com', 'admin123', 1)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  `contraseña` = VALUES(`contraseña`),
  id_rol = 1;
```

### 3) Convertir un usuario existente en admin

```sql
USE tugestionamiga_db;

UPDATE usuario
SET id_rol = 1
WHERE correo = 'admin@mail.com';
```

## Arquitectura MVC aplicada

- **Modelo:** clases Java que representan los datos (`Usuario`, `Libro`, `Prestamo`, `Rol`).
- **Vista:** JSP que muestran información y contienen formularios.
- **Controlador:** Servlets que reciben peticiones HTTP, validan parámetros y coordinan la respuesta.

La idea es que cada capa tenga una responsabilidad específica para que el código sea más fácil de mantener.

## Estructura del módulo web

Carpetas principales:

- `TuGestionAmigaWeb/src/`
  - `controller/` (Servlets)
  - `service/` (reglas del negocio/validaciones)
  - `dao/` (acceso a datos y SQL)
  - `model/` (POJOs)
  - `util/` (conexión JDBC para web)
  - `filter/` y `listener/` (sesión y carga de configuración)

- `TuGestionAmigaWeb/web/`
  - `index.jsp`
  - `jsp/` (vistas)
  - `css/` (estilos)
  - `js/` (scripts)
  - `WEB-INF/web.xml` (configuración y mapeos)

## Conexión a base de datos en web

- `util.ConexionBD` lee `WEB-INF/db.properties` usando `ServletContext.getResourceAsStream(...)`.
- `listener.AppContextListener` carga las propiedades al iniciar la aplicación y las guarda en el `ServletContext`.
- Los DAOs reciben el `ServletContext` para abrir conexiones con `ConexionBD.getConnection(context)`.

Esto evita depender del directorio de trabajo (como en consola) y se adapta mejor al despliegue en Tomcat.

## Seguridad básica (sesión)

- `LoginServlet` valida el usuario y guarda el objeto en sesión como `usuarioLogueado`.
- `filter.AuthFilter` bloquea el acceso a páginas internas si no existe sesión.
- `LogoutServlet` invalida la sesión.

## Rutas principales

Rutas definidas en `web.xml`:

- `GET/POST /login`
- `GET/POST /register`
- `GET /logout`
- `GET /dashboard`
- `GET/POST /usuarios`
- `GET/POST /libros`
- `GET/POST /prestamos`

## Capa `service/` y separación por capas

Se agregó la capa `service/` para que los Servlets no mezclen validaciones con SQL.

- `service.UsuarioService`
  - Autenticación.
  - Registro público (validaciones de campos y correo único).
  - Operaciones CRUD delegando en `UsuarioDAO`.

- `service.LibroService`
  - CRUD de libros delegando en `LibroDAO`.

- `service.PrestamoService`
  - Validación de fechas.
  - Delegación del flujo prestar/devolver en `PrestamoDAO`.

Con esto, el flujo queda más fácil de seguir:

1) **Servlet** recibe parámetros y decide la acción.
2) **Service** valida y aplica reglas.
3) **DAO** ejecuta SQL y mapea resultados.
4) **JSP** muestra datos y formularios.

## Flujo explicado (ejemplos)

### Inicio de sesión

1) `GET /login` muestra `jsp/login.jsp`.
2) `POST /login` lee `correo` y `contrasena`.
3) `UsuarioService.autenticar(...)` consulta `UsuarioDAO.findByCorreoYContrasena(...)`.
4) Si coincide, se guarda `usuarioLogueado` en sesión y se redirige a `/dashboard`.

### CRUD de usuarios

`controller.UsuarioServlet` usa el parámetro `accion`:

- **GET listar:** carga lista y reenvía a `jsp/usuarios.jsp`.
- **GET editar:** carga un usuario por id y lo pone en `usuarioEdit` para rellenar el formulario.
- **POST crear/actualizar/eliminar:** ejecuta la operación y redirige a listar.

Regla aplicada en servidor:

- No se permite que el usuario con sesión iniciada se elimine a sí mismo, para evitar que la sesión quede apuntando a un usuario inexistente.

### Préstamos y devoluciones

- `PrestamoService.registrarPrestamo(...)` valida fechas y delega en `PrestamoDAO.registrarPrestamo(...)`.
- En el DAO, el préstamo se maneja con transacción para mantener consistencia:
  - Se inserta el préstamo.
  - Se actualiza `libro.disponibilidad`.
  - Se confirma o se revierte según el resultado.

### Roles y permisos (ADMIN / USUARIO)

Para que la aplicación sea más realista, se separó el comportamiento entre un **administrador** y un **usuario normal**.
La base de datos ya tiene `usuario.id_rol` (FK a `rol`), así que se aprovechó ese campo para controlar permisos.

En el código se manejó una convención simple:

- `id_rol = 1` se toma como **ADMIN**.
- `id_rol = 2` se toma como **USUARIO**.
- Si `id_rol` viene en `NULL`, se trata como usuario normal para no romper registros que no tengan rol asignado.

Para centralizar esa lógica se agregaron helpers en `model.Usuario`:

- `esAdministrador()`
- `esUsuario()`

### Separación de permisos en backend (Servlets)

Además de ocultar botones en la interfaz, se controlaron permisos en el servidor para que no baste con “editar el HTML”.

- `controller.UsuarioServlet`
  - Se dejó el CRUD de usuarios **solo para administradores**.
  - Si un usuario normal intenta entrar, se redirige al dashboard con un mensaje.

- `controller.LibroServlet`
  - **Cualquier usuario** puede listar libros.
  - Las acciones `crear`, `actualizar`, `eliminar` y `editar` quedaron **solo para administradores**.

- `controller.PrestamoServlet`
  - En modo **ADMIN**:
    - carga todos los préstamos.
    - permite registrar devoluciones y eliminar préstamos.
    - al registrar un préstamo permite seleccionar el usuario.
  - En modo **USUARIO**:
    - el listado se limita a los préstamos del usuario logueado.
    - al registrar un préstamo se fuerza el `idUsuario` desde la sesión.

### Filtrado de préstamos por usuario (DAO/Service)

Para que el usuario normal solo vea lo suyo, se agregó:

- `dao.PrestamoDAO.findByUsuario(int idUsuario)`
- `service.PrestamoService.listarPrestamosPorUsuario(int idUsuario)`

Esto se usa desde `PrestamoServlet` cuando el usuario no es administrador.

### Ajustes de interfaz por rol (JSP)

Para que la experiencia sea más clara, la UI también se ajustó:

- `jsp/dashboard.jsp`
  - El enlace y la tarjeta de **Usuarios** solo aparecen para administradores.

- `jsp/listarLibros.jsp`
  - En modo usuario se oculta el formulario de registro/edición y las acciones de la tabla.

- `jsp/prestamos.jsp`
  - En modo usuario se oculta la selección de usuario, la sección de devoluciones y las acciones de eliminación.
