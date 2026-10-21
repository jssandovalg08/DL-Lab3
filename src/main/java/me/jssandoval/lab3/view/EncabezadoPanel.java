package me.jssandoval.lab3.view;

import me.jssandoval.lab3.model.Expr;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;

/** Parte superior: título, campo de entrada, teclado, vista previa en vivo y tarjetas de resultado. */
class EncabezadoPanel extends JPanel {

    private final JTextField campoEntrada = new JTextField();
    private final JLabel etiquetaPrevia = new JLabel("VISTA PREVIA");
    private final FormulaView vistaPrevia = new FormulaView(null, Paleta.TAM_PREVIA, Paleta.GRIS_TEXTO);
    private final Tarjeta tarjetaResultado = new Tarjeta(Paleta.INDIGO);
    private final Tarjeta tarjetaVerificacion = new Tarjeta(Paleta.GRIS_SUAVE);

    private Consumer<String> alSimplificar = t -> { };
    private Consumer<String> alCambiarTexto = t -> { };

    EncabezadoPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Paleta.FONDO);
        setBorder(new EmptyBorder(16, 18, 12, 18));

        JLabel titulo = new JLabel("Simplificador de Álgebra Booleana");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titulo.setForeground(Paleta.INDIGO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Simplificación paso a paso  →  Tablas de verdad  →  Mapa de Karnaugh (hasta 3 variables)");
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitulo.setForeground(Paleta.GRIS_SUAVE);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setBorder(new EmptyBorder(2, 0, 14, 0));

        JPanel filaEntrada = new JPanel(new BorderLayout(10, 0));
        filaEntrada.setBackground(Paleta.FONDO);
        filaEntrada.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaEntrada.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        campoEntrada.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        campoEntrada.setToolTipText("<html>Operadores: <b>+</b> OR &nbsp; <b>*</b> AND (o implícito: AB) &nbsp; <b>!</b> NOT<br>"
                + "<b>(+)</b> XOR &nbsp; <b>(*)</b> o <b>(.)</b> XNOR/XAND &nbsp; Agrupación: ( ) [ ] { }<br>"
                + "También se aceptan los símbolos ⊕ ⊙ ¬ pegados desde otro lado. Usá el teclado de abajo si preferís.</html>");
        campoEntrada.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Paleta.GRIS_BORDE),
                new EmptyBorder(6, 10, 6, 10)));
        campoEntrada.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { notificarCambio(); }
            @Override public void removeUpdate(DocumentEvent e) { notificarCambio(); }
            @Override public void changedUpdate(DocumentEvent e) { notificarCambio(); }
        });
        filaEntrada.add(campoEntrada, BorderLayout.CENTER);

        JButton boton = new JButton("Simplificar");
        boton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        boton.setBackground(Paleta.INDIGO);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(8, 20, 8, 20));
        boton.addActionListener(e -> alSimplificar.accept(getTexto()));
        campoEntrada.addActionListener(e -> alSimplificar.accept(getTexto()));
        filaEntrada.add(boton, BorderLayout.EAST);

        etiquetaPrevia.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        etiquetaPrevia.setForeground(Paleta.GRIS_SUAVE);
        etiquetaPrevia.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiquetaPrevia.setBorder(new EmptyBorder(10, 0, 4, 0));

        vistaPrevia.setOpaque(true);
        vistaPrevia.setBackground(Color.WHITE);
        vistaPrevia.setBorder(new EmptyBorder(10, 8, 10, 8));
        JScrollPane scrollPrevia = new JScrollPane(vistaPrevia,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPrevia.setBorder(BorderFactory.createLineBorder(Paleta.GRIS_BORDE));
        scrollPrevia.getViewport().setBackground(Color.WHITE);
        scrollPrevia.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPrevia.setPreferredSize(new Dimension(100, 72));
        scrollPrevia.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel filaTarjetas = new JPanel(new GridLayout(2, 1, 0, 8));
        filaTarjetas.setBackground(Paleta.FONDO);
        filaTarjetas.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaTarjetas.setBorder(new EmptyBorder(12, 0, 0, 0));
        filaTarjetas.add(tarjetaResultado);
        filaTarjetas.add(tarjetaVerificacion);

        add(titulo);
        add(subtitulo);
        add(filaEntrada);
        add(new TecladoPanel(campoEntrada));
        add(etiquetaPrevia);
        add(scrollPrevia);
        add(filaTarjetas);

        tarjetaResultado.setTexto("Ingresa una expresión y presiona Simplificar");
        tarjetaVerificacion.setTexto("Verificación pendiente");
    }

    private void notificarCambio() { alCambiarTexto.accept(getTexto()); }

    void setAlSimplificar(Consumer<String> c) { this.alSimplificar = c; }
    void setAlCambiarTexto(Consumer<String> c) { this.alCambiarTexto = c; }

    String getTexto() { return campoEntrada.getText().trim(); }
    void setTexto(String t) { campoEntrada.setText(t); }

    void mostrarPrevia(Expr e) {
        vistaPrevia.setExpr(e);
        etiquetaPrevia.setText("VISTA PREVIA");
    }

    void mostrarPreviaVacia() {
        vistaPrevia.setExpr(null);
        etiquetaPrevia.setText("VISTA PREVIA  ·  escribe o usa el teclado para armar la expresión");
    }

    void mostrarPreviaInvalida() {
        vistaPrevia.setExpr(null);
        etiquetaPrevia.setText("VISTA PREVIA  ·  expresión incompleta o mal formada");
    }

    void mostrarResultado(Expr simplificado, boolean equivalente) {
        tarjetaResultado.setColorFondo(Paleta.INDIGO);
        tarjetaResultado.setFormula("Resultado mínimo:", simplificado);
        if (equivalente) {
            tarjetaVerificacion.setColorFondo(Paleta.VERDE);
            tarjetaVerificacion.setTexto("✓ Verificado: equivalente a la original en todas las combinaciones");
        } else {
            tarjetaVerificacion.setColorFondo(Paleta.ROJO);
            tarjetaVerificacion.setTexto("✗ La simplificación NO coincide con la original — revisar reglas");
        }
    }
}
