package me.jssandoval.lab3.view;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Etiqueta con fondo de color redondeado. */
class Chip extends JLabel {
    private final Color color;

    Chip(String texto, Color color) {
        super(texto);
        this.color = color;
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        setBorder(new EmptyBorder(4, 9, 4, 9));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }
}
