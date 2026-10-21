package me.jssandoval.lab3.view;

import me.jssandoval.lab3.model.Not;
import me.jssandoval.lab3.model.Var;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/** Teclado en pantalla: escribe directamente en el campo de texto que recibe. */
class TecladoPanel extends JPanel {
    private final JTextField campo;

    TecladoPanel(JTextField campo) {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0));
        this.campo = campo;
        setBackground(Paleta.FONDO);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(new EmptyBorder(8, 0, 0, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        Color c = Paleta.GRIS_TEXTO;
        for (String v : new String[]{"A", "B", "C", "D", "E", "F"}) {
            add(tecla(v, null, "Variable " + v, () -> insertar(v)));
        }
        add(tecla("+", null, "OR (suma)", () -> insertar("+")));
        add(tecla("*", null, "AND (producto)", () -> insertar("*")));
        add(tecla(null, FormulaView.icono(new Not(new Var("A")), 18, c),
                "NOT (negación): se escribe !", () -> insertar("!")));
        add(tecla(null, FormulaView.iconoOperador(false, 16, c),
                "XOR: se escribe (+)", () -> insertar("(+)")));
        add(tecla(null, FormulaView.iconoOperador(true, 16, c),
                "XNOR / XAND: se escribe (*) o (.)", () -> insertar("(*)")));
        add(tecla("(", null, "Abrir paréntesis", () -> insertar("(")));
        add(tecla(")", null, "Cerrar paréntesis", () -> insertar(")")));
        add(tecla("0", null, "Constante 0", () -> insertar("0")));
        add(tecla("1", null, "Constante 1", () -> insertar("1")));
        add(tecla("←", null, "Borrar el último símbolo", this::borrarAtras));
        add(tecla("C", null, "Borrar todo", () -> {
            campo.setText("");
            campo.requestFocusInWindow();
        }));
    }

    private JButton tecla(String texto, Icon icono, String ayuda, Runnable accion) {
        JButton b = new JButton(texto);
        if (icono != null) b.setIcon(icono);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        b.setBackground(Color.WHITE);
        b.setForeground(Paleta.GRIS_TEXTO);
        b.setFocusPainted(false);
        b.setFocusable(false);               // el cursor se queda en el campo de texto
        b.setBorder(BorderFactory.createLineBorder(Paleta.GRIS_BORDE));
        b.setPreferredSize(new Dimension(44, 34));
        b.setToolTipText(ayuda);
        b.addActionListener(e -> accion.run());
        return b;
    }

    private void insertar(String texto) {
        campo.replaceSelection(texto);
        campo.requestFocusInWindow();
    }

    /** Borra la selección o el símbolo anterior al cursor ((+), (*), (.) cuentan como uno solo). */
    private void borrarAtras() {
        if (campo.getSelectedText() != null) {
            campo.replaceSelection("");
        } else {
            int pos = campo.getCaretPosition();
            if (pos > 0) {
                String antes = campo.getText().substring(0, pos);
                int n = (antes.endsWith("(+)") || antes.endsWith("(*)")
                        || antes.endsWith("(.)") || antes.endsWith("(=)")) ? 3 : 1;
                try {
                    campo.getDocument().remove(pos - n, n);
                } catch (BadLocationException ignored) {
                    // no debería pasar: pos - n >= 0
                }
            }
        }
        campo.requestFocusInWindow();
    }
}
