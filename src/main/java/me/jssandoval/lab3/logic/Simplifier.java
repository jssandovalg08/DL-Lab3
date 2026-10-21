package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.Expr;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta todo el proceso: parseo, bucle de simplificación, tablas y verificación.
 * No depende de Swing, así que se puede usar desde consola, tests u otra interfaz.
 */
public final class Simplifier {

    private Simplifier() { }

    public static Resultado simplificar(String entrada) {
        List<String> posfijo = ExpressionParser.aPostfijo(entrada);
        // simplificar() MUTA el árbol en el lugar: "original" queda intacto y "actual" es el que se simplifica.
        Expr original = ExpressionParser.construir(posfijo);
        Expr actual = ExpressionParser.construir(posfijo);

        List<String> mensajes = new ArrayList<>();
        List<Paso> historial = new ArrayList<>();
        String antesTexto;

        // Fase 0: reglas algebraicas (incluye reglas propias de XOR/XNOR).
        // Fase 1: con la expresión ya convergida, se reconocen patrones XOR / XNOR escritos con AND/OR.
        // Cada llamada aplica a lo sumo una regla; solo se registra un paso cuando se agregó un mensaje.
        for (int fase = 0; fase < 2; fase++) {
            do {
                antesTexto = actual.toString();
                Expr antesExpr = actual.copiar();   // copia: simplificar() muta el árbol
                boolean[] aplicado = {false};
                int previos = mensajes.size();
                actual = fase == 0 ? actual.simplificar(mensajes, aplicado)
                        : actual.recomponerXor(mensajes, aplicado);
                if (mensajes.size() > previos) {
                    historial.add(new Paso(mensajes.get(mensajes.size() - 1), antesExpr, actual.copiar()));
                }
            } while (!antesTexto.equals(actual.toString()));
        }

        List<String> vars = Evaluador.variables(original);
        return new Resultado(
                entrada, posfijo, original, historial, actual, vars,
                Evaluador.equivalentes(original, actual, vars),
                TablaVerdad.generar(original, vars),
                TablaVerdad.generar(actual, vars),
                MapaKarnaugh.generar(original, vars));
    }
}
