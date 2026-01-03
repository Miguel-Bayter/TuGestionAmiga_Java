package tugestionamiga.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Utilidad para leer datos desde consola y validar el formato.
 *
 * Se centraliza la lectura para evitar repetir validaciones en el menú.
 */
public class ConsoleInput {

    private final Scanner scanner;

    /**
     * Constructor que recibe un objeto Scanner para leer desde consola.
     * 
     * @param scanner objeto Scanner para leer desde consola
     */
    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Lee un entero desde consola, repitiendo la pregunta hasta que sea válido.
     * 
     * @param label texto que se mostrará antes de leer el entero
     * @return entero leído desde consola
     */
    public int readInt(String label) {
        while (true) {
            System.out.print(label);
            String value = scanner.nextLine().trim();

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Valor inválido. Ingresa un número.");
            }
        }
    }

    /**
     * Lee una cadena desde consola.
     * 
     * @param label texto que se mostrará antes de leer la cadena
     * @return cadena leída desde consola
     */
    public String readString(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    /**
     * Lee una fecha en formato ISO (AAAA-MM-DD).
     * 
     * @param label texto que se mostrará antes de leer la fecha
     * @return fecha leída desde consola
     */
    public LocalDate readLocalDate(String label) {
        while (true) {
            System.out.print(label);
            String value = scanner.nextLine().trim();

            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ex) {
                System.out.println("Fecha inválida. Formato esperado: AAAA-MM-DD.");
            }
        }
    }
}
