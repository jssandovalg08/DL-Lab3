package me.jssandoval.lab3;

import me.jssandoval.lab3.logic.Paso;
import me.jssandoval.lab3.logic.Resultado;
import me.jssandoval.lab3.logic.Simplifier;

import java.util.Scanner;

/** Versión de consola: usa la misma lógica que la GUI (logic/Simplifier). */
public class Test {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduce la expresión lógica: ");
        String entrada = scanner.nextLine().trim();
        System.out.println("Entrada: " + entrada);

        Resultado r = Simplifier.simplificar(entrada);
        System.out.println("Posfijo: " + r.posfijo());
        System.out.println("\nAST Original:\n" + r.original());

        System.out.println("\n--- Pasos de Simplificación ---");
        for (Paso p : r.historial()) {
            System.out.println(p.mensaje());
        }

        System.out.println("\nExpresión Final (Mínima):\n" + r.simplificado());
        System.out.println("Equivalente a la original: " + r.equivalente());
    }
}
