package me.jssandoval.lab3.view;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Color;
import java.awt.Font;

/** Colores, tamaños y tema visual compartidos por toda la vista. */
public final class Paleta {

    public static final Color INDIGO = new Color(0x4F, 0x46, 0xE5);
    public static final Color VERDE = new Color(0x16, 0xA3, 0x4A);
    public static final Color VERDE_CLARO = new Color(0xD1, 0xFA, 0xE5);
    public static final Color ROJO = new Color(0xDC, 0x26, 0x26);
    public static final Color GRIS_TEXTO = new Color(0x37, 0x41, 0x51);
    public static final Color GRIS_SUAVE = new Color(0x6B, 0x72, 0x80);
    public static final Color GRIS_BORDE = new Color(0xD1, 0xD5, 0xDB);
    public static final Color FONDO = new Color(0xF5, 0xF6, 0xFA);

    // Tamaños de fuente de las fórmulas
    public static final int TAM_PREVIA = 24;
    public static final int TAM_PASOS = 21;
    public static final int TAM_TABLA = 17;

    private Paleta() { }

    public static Color colorParaCategoria(String categoria) {
        if (categoria.startsWith("Absorción con negación")) return new Color(0xEA, 0x58, 0x0C);
        if (categoria.startsWith("Absorción")) return new Color(0xF5, 0x9E, 0x0B);
        if (categoria.startsWith("XNOR")) return new Color(0x0F, 0x76, 0x6E);
        if (categoria.startsWith("XOR")) return new Color(0xDB, 0x27, 0x77);
        if (categoria.startsWith("Factor común")) return new Color(0x06, 0xB6, 0xD4);
        if (categoria.startsWith("Idempotencia")) return new Color(0x22, 0xC5, 0x5E);
        if (categoria.startsWith("Complemento")) return new Color(0xEF, 0x44, 0x44);
        if (categoria.startsWith("Identidad")) return new Color(0x64, 0x74, 0x8B);
        if (categoria.startsWith("De Morgan")) return new Color(0x8B, 0x5C, 0xF6);
        if (categoria.startsWith("Doble negación") || categoria.startsWith("Negación de constante")) return new Color(0x0E, 0xA5, 0xE9);
        return INDIGO;
    }

    /** Mezcla con blanco: t = 0 deja el color, t = 1 da blanco. */
    public static Color mezclarConBlanco(Color c, float t) {
        return new Color(Math.round(c.getRed() * (1 - t) + 255 * t),
                Math.round(c.getGreen() * (1 - t) + 255 * t),
                Math.round(c.getBlue() * (1 - t) + 255 * t));
    }

    public static Color oscurecer(Color c, float f) {
        return new Color((int) (c.getRed() * f), (int) (c.getGreen() * f), (int) (c.getBlue() * f));
    }

    /** Instala el Look & Feel Metal con el tema moderno. Llamar antes de crear ventanas. */
    public static void instalarTema() {
        try {
            MetalLookAndFeel.setCurrentTheme(new TemaModerno());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignored) {
            // Si algo falla, seguimos con el look and feel por defecto.
        }
    }

    private static class TemaModerno extends DefaultMetalTheme {
        private final ColorUIResource p1 = new ColorUIResource(INDIGO);
        private final ColorUIResource p2 = new ColorUIResource(0x81, 0x8C, 0xF8);
        private final ColorUIResource p3 = new ColorUIResource(0xC7, 0xD2, 0xFE);
        private final ColorUIResource s1 = new ColorUIResource(0xD1, 0xD5, 0xDB);
        private final ColorUIResource s2 = new ColorUIResource(0xE5, 0xE7, 0xEB);
        private final ColorUIResource s3 = new ColorUIResource(0xF9, 0xFA, 0xFB);

        protected ColorUIResource getPrimary1() { return p1; }
        protected ColorUIResource getPrimary2() { return p2; }
        protected ColorUIResource getPrimary3() { return p3; }
        protected ColorUIResource getSecondary1() { return s1; }
        protected ColorUIResource getSecondary2() { return s2; }
        protected ColorUIResource getSecondary3() { return s3; }

        public FontUIResource getControlTextFont() { return new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 13); }
        public FontUIResource getSystemTextFont() { return new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 13); }
        public FontUIResource getUserTextFont() { return new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 13); }
        public FontUIResource getMenuTextFont() { return new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 13); }
        public FontUIResource getWindowTitleFont() { return new FontUIResource(Font.SANS_SERIF, Font.BOLD, 13); }
        public FontUIResource getSubTextFont() { return new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 11); }
    }
}
