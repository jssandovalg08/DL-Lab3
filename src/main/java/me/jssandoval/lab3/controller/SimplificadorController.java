package me.jssandoval.lab3.controller;

import me.jssandoval.lab3.logic.ExpressionParser;
import me.jssandoval.lab3.logic.Resultado;
import me.jssandoval.lab3.logic.Simplifier;
import me.jssandoval.lab3.view.SimplificadorFrame;

/** Conecta la vista con la lógica: recibe eventos de la ventana, llama a logic/ y le devuelve datos a la vista. */
public class SimplificadorController {

    private static final String EJEMPLO = "{{[!(A+B)*(A+C)](+)!(A+B+C)}(*)!(A+B+D)}(+)!(E(A+B))";

    private final SimplificadorFrame vista;

    public SimplificadorController(SimplificadorFrame vista) {
        this.vista = vista;
        vista.setAlCambiarTexto(this::actualizarPrevia);
        vista.setAlSimplificar(this::simplificar);
        vista.setEntrada(EJEMPLO);   // dispara la vista previa
    }

    private void actualizarPrevia(String texto) {
        if (texto.isEmpty()) {
            vista.mostrarPreviaVacia();
            return;
        }
        try {
            vista.mostrarPrevia(ExpressionParser.parsear(texto));
        } catch (Exception ex) {
            vista.mostrarPreviaInvalida();
        }
    }

    private void simplificar(String texto) {
        if (texto.isEmpty()) return;
        try {
            Resultado r = Simplifier.simplificar(texto);
            vista.mostrarResultado(r);
        } catch (Exception ex) {
            vista.mostrarError(ex.getMessage() != null ? ex.getMessage() : "expresión incompleta o mal formada");
        }
    }
}
