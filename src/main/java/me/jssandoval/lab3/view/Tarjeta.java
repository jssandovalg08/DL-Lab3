package me.jssandoval.lab3.view;

import me.jssandoval.lab3.model.Expr;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Tarjeta de color con un texto de estado o con "texto + fórmula dibujada". */
class Tarjeta extends JPanel {
    private Color colorFondo;
    private final JLabel etiqueta = new JLabel();
    private final FormulaView vista = new FormulaView(null, 20, Color.WHITE);

    Tarjeta(Color colorFondo) {
        this.colorFondo = colorFondo;
        setOpaque(false);
        setLayout(new BorderLayout(10, 0));
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        vista.setAjustar(true);
        setBorder(new EmptyBorder(8, 14, 8, 14));
        add(etiqueta, BorderLayout.CENTER);
    }

    /** Solo texto (estado). */
    void setTexto(String texto) {
        removeAll();
        etiqueta.setText(texto);
        add(etiqueta, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /** Texto corto seguido de la fórmula dibujada (se reduce si no entra). */
    void setFormula(String prefijo, Expr e) {
        removeAll();
        etiqueta.setText(prefijo);
        vista.setExpr(e);
        add(etiqueta, BorderLayout.WEST);
        add(vista, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    void setColorFondo(Color c) { this.colorFondo = c; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(colorFondo);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }
}
