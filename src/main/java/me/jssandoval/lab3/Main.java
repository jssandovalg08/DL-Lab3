package me.jssandoval.lab3;

import me.jssandoval.lab3.controller.SimplificadorController;
import me.jssandoval.lab3.view.Paleta;
import me.jssandoval.lab3.view.SimplificadorFrame;

import javax.swing.SwingUtilities;

/** Punto de entrada de la aplicación gráfica. */
public class Main {
    public static void main(String[] args) {
        Paleta.instalarTema();
        SwingUtilities.invokeLater(() -> {
            SimplificadorFrame ventana = new SimplificadorFrame();
            new SimplificadorController(ventana);
            ventana.setVisible(true);
        });
    }
}
