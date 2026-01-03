package tugestionamiga.main;

import tugestionamiga.dao.LibroDAO;
import tugestionamiga.dao.PrestamoDAO;
import tugestionamiga.dao.UsuarioDAO;
import tugestionamiga.model.Libro;
import tugestionamiga.model.Prestamo;
import tugestionamiga.model.Usuario;
import tugestionamiga.util.ConsoleInput;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Punto de entrada de la aplicación.
 *
 * Implementa un menú por consola para gestionar usuarios, libros y préstamos.
 * Las operaciones de base de datos se delegan a las clases DAO.
 *
 * La aplicación se encarga de coordinar las operaciones de CRUD para cada entidad,
 * utilizando los DAOs correspondientes para interactuar con la base de datos.
 * El menú principal ofrece opciones para acceder a los submenús de usuarios, libros y préstamos.
 */
public class Application {

    private final UsuarioDAO usuarioDAO;
    private final LibroDAO libroDAO;
    private final PrestamoDAO prestamoDAO;

    private final ConsoleInput input;

    /**
     * Crea la aplicación indicando los DAOs que se van a usar.
     *
     * Esta inyección simple permite que el menú esté separado de la lógica de acceso a datos.
     *
     * @param usuarioDAO DAO de usuarios
     * @param libroDAO DAO de libros
     * @param prestamoDAO DAO de préstamos
     * @param input utilitario de lectura de consola
     */
    public Application(UsuarioDAO usuarioDAO, LibroDAO libroDAO, PrestamoDAO prestamoDAO, ConsoleInput input) {
        this.usuarioDAO = usuarioDAO;
        this.libroDAO = libroDAO;
        this.prestamoDAO = prestamoDAO;
        this.input = input;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Application app = new Application(
                    new UsuarioDAO(),
                    new LibroDAO(),
                    new PrestamoDAO(),
                    new ConsoleInput(scanner)
            );

            app.run();
        }
    }

     /**
      * Bucle principal del programa. Muestra el menú general y redirige a submenús.
      */
    public void run() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Tu Gestión Amiga ===");
            System.out.println("1. Usuarios");
            System.out.println("2. Libros");
            System.out.println("3. Préstamos");
            System.out.println("0. Salir");

            int option = input.readInt("Opción: ");

            try {
                switch (option) {
                    case 1 -> menuUsuarios();
                    case 2 -> menuLibros();
                    case 3 -> menuPrestamos();
                    case 0 -> running = false;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException ex) {
                System.out.println("Error de base de datos: " + ex.getMessage());
            }
        }

        System.out.println("Programa finalizado.");
    }

    private void menuUsuarios() throws SQLException {
        // Submenú de operaciones CRUD para la tabla usuario.
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Menú Usuarios ---");
            System.out.println("1. Listar");
            System.out.println("2. Crear");
            System.out.println("3. Actualizar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");

            int option = input.readInt("Opción: ");

            switch (option) {
                case 1 -> listarUsuarios();
                case 2 -> crearUsuario();
                case 3 -> actualizarUsuario();
                case 4 -> eliminarUsuario();
                case 0 -> back = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void listarUsuarios() throws SQLException {
        // Consulta todos los usuarios y los imprime con toString().
        List<Usuario> usuarios = usuarioDAO.findAll();

        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }

        usuarios.forEach(System.out::println);
    }

    private void crearUsuario() throws SQLException {
        // Lee datos por consola y crea el registro en la tabla usuario.
        String nombre = input.readString("Nombre: ");
        String correo = input.readString("Correo: ");
        String contrasena = input.readString("Contraseña: ");
        int idRol = input.readInt("Id Rol (0 si no aplica): ");

        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setContrasena(contrasena);
        u.setIdRol(idRol <= 0 ? null : idRol);

        int id = usuarioDAO.create(u);
        System.out.println("Usuario creado con ID: " + id);
    }

    private void actualizarUsuario() throws SQLException {
        // Carga un usuario por ID, solicita nuevos valores y ejecuta UPDATE.
        int idUsuario = input.readInt("ID de usuario a actualizar: ");
        Usuario current = usuarioDAO.findById(idUsuario);

        if (current == null) {
            System.out.println("No existe el usuario.");
            return;
        }

        System.out.println("Actual: " + current);

        String nombre = input.readString("Nombre: ");
        String correo = input.readString("Correo: ");
        String contrasena = input.readString("Contraseña: ");
        int idRol = input.readInt("Id Rol (0 si no aplica): ");

        current.setNombre(nombre);
        current.setCorreo(correo);
        current.setContrasena(contrasena);
        current.setIdRol(idRol <= 0 ? null : idRol);

        boolean ok = usuarioDAO.update(current);
        System.out.println(ok ? "Usuario actualizado." : "No se pudo actualizar.");
    }

    private void eliminarUsuario() throws SQLException {
        // Elimina un usuario por ID.
        int idUsuario = input.readInt("ID de usuario a eliminar: ");
        boolean ok = usuarioDAO.delete(idUsuario);
        System.out.println(ok ? "Usuario eliminado." : "No se pudo eliminar.");
    }

    private void menuLibros() throws SQLException {
        // Submenú de operaciones CRUD para la tabla libro.
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Menú Libros ---");
            System.out.println("1. Listar");
            System.out.println("2. Crear");
            System.out.println("3. Actualizar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");

            int option = input.readInt("Opción: ");

            switch (option) {
                case 1 -> listarLibros();
                case 2 -> crearLibro();
                case 3 -> actualizarLibro();
                case 4 -> eliminarLibro();
                case 0 -> back = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void listarLibros() throws SQLException {
        // Consulta todos los libros y los imprime.
        List<Libro> libros = libroDAO.findAll();

        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
            return;
        }

        libros.forEach(System.out::println);
    }

    private void crearLibro() throws SQLException {
        // Crea un libro y define su disponibilidad inicial.
        String titulo = input.readString("Título: ");
        String autor = input.readString("Autor: ");
        String descripcion = input.readString("Descripción: ");
        int disponible = input.readInt("Disponible (1=Sí, 0=No): ");
        int idCategoria = input.readInt("Id Categoría (0 si no aplica): ");

        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setDescripcion(descripcion);
        libro.setDisponible(disponible == 1);
        libro.setIdCategoria(idCategoria <= 0 ? null : idCategoria);

        int id = libroDAO.create(libro);
        System.out.println("Libro creado con ID: " + id);
    }

    private void actualizarLibro() throws SQLException {
        // Carga un libro por ID, solicita nuevos valores y ejecuta UPDATE.
        int idLibro = input.readInt("ID de libro a actualizar: ");
        Libro current = libroDAO.findById(idLibro);

        if (current == null) {
            System.out.println("No existe el libro.");
            return;
        }

        System.out.println("Actual: " + current);

        String titulo = input.readString("Título: ");
        String autor = input.readString("Autor: ");
        String descripcion = input.readString("Descripción: ");
        int disponible = input.readInt("Disponible (1=Sí, 0=No): ");
        int idCategoria = input.readInt("Id Categoría (0 si no aplica): ");

        current.setTitulo(titulo);
        current.setAutor(autor);
        current.setDescripcion(descripcion);
        current.setDisponible(disponible == 1);
        current.setIdCategoria(idCategoria <= 0 ? null : idCategoria);

        boolean ok = libroDAO.update(current);
        System.out.println(ok ? "Libro actualizado." : "No se pudo actualizar.");
    }

    private void eliminarLibro() throws SQLException {
        // Elimina un libro por ID.
        int idLibro = input.readInt("ID de libro a eliminar: ");
        boolean ok = libroDAO.delete(idLibro);
        System.out.println(ok ? "Libro eliminado." : "No se pudo eliminar.");
    }

    private void menuPrestamos() throws SQLException {
        // Submenú de préstamos: CRUD + flujo de prestar/devolver.
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Menú Préstamos ---");
            System.out.println("1. Listar");
            System.out.println("2. Crear (manual)");
            System.out.println("3. Actualizar");
            System.out.println("4. Eliminar");
            System.out.println("5. Registrar préstamo (prestar libro)");
            System.out.println("6. Registrar devolución");
            System.out.println("0. Volver");

            int option = input.readInt("Opción: ");

            switch (option) {
                case 1 -> listarPrestamos();
                case 2 -> crearPrestamoManual();
                case 3 -> actualizarPrestamo();
                case 4 -> eliminarPrestamo();
                case 5 -> prestarLibro();
                case 6 -> devolverLibro();
                case 0 -> back = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void listarPrestamos() throws SQLException {
        // Consulta todos los préstamos y los imprime.
        List<Prestamo> prestamos = prestamoDAO.findAll();

        if (prestamos.isEmpty()) {
            System.out.println("No hay préstamos registrados.");
            return;
        }

        prestamos.forEach(System.out::println);
    }

    private void crearPrestamoManual() throws SQLException {
        // Inserta un préstamo directamente (no modifica disponibilidad del libro).
        // Se deja para pruebas/carga manual.
        LocalDate fechaPrestamo = input.readLocalDate("Fecha préstamo (AAAA-MM-DD): ");
        LocalDate fechaDevolucion = input.readLocalDate("Fecha devolución (AAAA-MM-DD): ");
        String estado = input.readString("Estado: ");
        int idUsuario = input.readInt("Id Usuario (0 si no aplica): ");
        int idLibro = input.readInt("Id Libro (0 si no aplica): ");

        Prestamo p = new Prestamo();
        p.setFechaPrestamo(fechaPrestamo);
        p.setFechaDevolucion(fechaDevolucion);
        p.setEstado(estado);
        p.setIdUsuario(idUsuario <= 0 ? null : idUsuario);
        p.setIdLibro(idLibro <= 0 ? null : idLibro);

        int id = prestamoDAO.create(p);
        System.out.println("Préstamo creado con ID: " + id);
    }

    private void actualizarPrestamo() throws SQLException {
        // Actualiza un préstamo existente.
        int idPrestamo = input.readInt("ID de préstamo a actualizar: ");
        Prestamo current = prestamoDAO.findById(idPrestamo);

        if (current == null) {
            System.out.println("No existe el préstamo.");
            return;
        }

        System.out.println("Actual: " + current);

        LocalDate fechaPrestamo = input.readLocalDate("Fecha préstamo (AAAA-MM-DD): ");
        LocalDate fechaDevolucion = input.readLocalDate("Fecha devolución (AAAA-MM-DD): ");
        String estado = input.readString("Estado: ");
        int idUsuario = input.readInt("Id Usuario (0 si no aplica): ");
        int idLibro = input.readInt("Id Libro (0 si no aplica): ");

        current.setFechaPrestamo(fechaPrestamo);
        current.setFechaDevolucion(fechaDevolucion);
        current.setEstado(estado);
        current.setIdUsuario(idUsuario <= 0 ? null : idUsuario);
        current.setIdLibro(idLibro <= 0 ? null : idLibro);

        boolean ok = prestamoDAO.update(current);
        System.out.println(ok ? "Préstamo actualizado." : "No se pudo actualizar.");
    }

    private void eliminarPrestamo() throws SQLException {
        // Elimina un préstamo por ID.
        int idPrestamo = input.readInt("ID de préstamo a eliminar: ");
        boolean ok = prestamoDAO.delete(idPrestamo);
        System.out.println(ok ? "Préstamo eliminado." : "No se pudo eliminar.");
    }

    private void prestarLibro() throws SQLException {
        // Usa una transacción: primero valida disponibilidad del libro y luego registra el préstamo.
        int idUsuario = input.readInt("ID Usuario: ");
        int idLibro = input.readInt("ID Libro: ");
        LocalDate fechaPrestamo = input.readLocalDate("Fecha préstamo (AAAA-MM-DD): ");
        LocalDate fechaDevolucion = input.readLocalDate("Fecha devolución (AAAA-MM-DD): ");

        int idPrestamo = prestamoDAO.registrarPrestamo(idUsuario, idLibro, fechaPrestamo, fechaDevolucion);

        if (idPrestamo <= 0) {
            System.out.println("No se pudo registrar el préstamo. Verifica que el libro exista y esté disponible.");
            return;
        }

        System.out.println("Préstamo registrado con ID: " + idPrestamo);
    }

    private void devolverLibro() throws SQLException {
        // Marca el préstamo como DEVUELTO y vuelve a habilitar la disponibilidad del libro.
        int idPrestamo = input.readInt("ID Préstamo: ");
        boolean ok = prestamoDAO.registrarDevolucion(idPrestamo);
        System.out.println(ok ? "Devolución registrada." : "No se pudo registrar la devolución.");
    }
}
