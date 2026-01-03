# Tu Gestión Amiga

Aplicación de consola en **Java SE** para la gestión de una biblioteca. Permite administrar **usuarios**, **libros** y **préstamos** usando **MySQL** y **JDBC** con patrón **DAO**.

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
