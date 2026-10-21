package me.jssandoval.lab3.view;

import me.jssandoval.lab3.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Dibuja una expresión booleana como se escribe en el tablero:
 *  - negación = raya encima (sobre una letra o sobre todo un grupo, sin paréntesis),
 *  - + (OR) y * (AND) tal cual,
 *  - XOR = círculo con una cruz (+), XNOR/XAND = círculo con un punto (.),
 *  - paréntesis dibujados a la altura del contenido.
 *
 * Se dibuja con Java2D (no con texto Unicode) para no depender de las fuentes
 * instaladas. Puede resaltar subexpresiones (identidad de objeto) para mostrar
 * qué parte cambió en un paso de simplificación.
 */
public class FormulaView extends JComponent {

    private static final int PAD = 4;

    private enum Ctx { TOP, NOT, AND, OR, XOR }

    private Expr expr;
    private final int tamano;
    private Set<Expr> resaltados = Collections.emptySet();
    private Color fondoResalte = new Color(0xFF, 0xF1, 0xA8);
    private Color textoResalte = new Color(0x1F, 0x29, 0x37);
    private boolean ajustar;
    private Caja caja;

    public FormulaView(Expr expr, int tamano, Color color) {
        this.tamano = tamano;
        setForeground(color);
        setExpr(expr);
    }

    public void setExpr(Expr e) {
        this.expr = e;
        setToolTipText(e == null ? null : e.toString());
        reconstruir();
    }

    /** Resalta (por identidad de objeto) los nodos indicados, dibujándolos sobre un fondo de color. */
    public void setResalte(Set<Expr> nodos, Color fondo, Color texto) {
        this.resaltados = nodos;
        this.fondoResalte = fondo;
        this.textoResalte = texto;
        reconstruir();
    }

    /** Si es true, la fórmula se reduce (hasta la mitad) para caber en el ancho disponible. */
    public void setAjustar(boolean ajustar) {
        this.ajustar = ajustar;
        revalidate();
        repaint();
    }

    private void reconstruir() {
        caja = expr == null ? null
                : construir(expr, Ctx.TOP, new Medida(tamano), resaltados, fondoResalte, textoResalte);
        revalidate();
        repaint();
    }

    /** Tamaño que ocupa la fórmula (sin bordes), útil para dimensionar columnas de tablas. */
    public static Dimension medir(Expr e, int tamano) {
        Caja c = construir(e, Ctx.TOP, new Medida(tamano), Collections.emptySet(), null, null);
        return new Dimension(c.w + 2 * PAD, c.asc + c.desc + 2 * PAD);
    }

    @Override
    public Dimension getPreferredSize() {
        Insets in = getInsets();
        int w = (caja == null ? 12 : caja.w) + 2 * PAD + in.left + in.right;
        int h = (caja == null ? tamano : caja.asc + caja.desc) + 2 * PAD + in.top + in.bottom;
        return new Dimension(w, h);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension p = getPreferredSize();
        return ajustar ? new Dimension(40, p.height) : p;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            if (isOpaque()) {
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            if (caja == null) return;
            activarCalidad(g2);
            Insets in = getInsets();
            int disponible = getWidth() - in.left - in.right - 2 * PAD;
            double escala = (ajustar && disponible < caja.w) ? Math.max(0.5, (double) disponible / caja.w) : 1.0;
            double areaH = getHeight() - in.top - in.bottom;
            double alto = (caja.asc + caja.desc) * escala;
            double base = in.top + (areaH - alto) / 2.0 + caja.asc * escala;
            g2.translate(in.left + PAD, base);
            g2.scale(escala, escala);
            g2.setColor(getForeground());
            caja.pintar(g2, 0, 0);
        } finally {
            g2.dispose();
        }
    }

    private static void activarCalidad(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ---------- Iconos (para los botones del teclado) ----------

    public static Icon icono(Expr e, int tamano, Color color) {
        Caja c = construir(e, Ctx.TOP, new Medida(tamano), Collections.emptySet(), null, null);
        return new IconoCaja(c, color);
    }

    public static Icon iconoOperador(boolean xnor, int tamano, Color color) {
        return new IconoCaja(new Circulo(xnor, new Medida(tamano)), color);
    }

    private static final class IconoCaja implements Icon {
        private final Caja caja;
        private final Color color;

        IconoCaja(Caja caja, Color color) { this.caja = caja; this.color = color; }

        @Override public int getIconWidth() { return caja.w + 2; }
        @Override public int getIconHeight() { return caja.asc + caja.desc + 2; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            activarCalidad(g2);
            g2.setColor(color);
            caja.pintar(g2, x + 1, y + 1 + caja.asc);
            g2.dispose();
        }
    }

    // ---------- Construcción de la caja a partir del árbol ----------

    private static final Graphics2D MEDIDOR = crearMedidor();

    private static Graphics2D crearMedidor() {
        Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        activarCalidad(g);
        return g;
    }

    private static Caja construir(Expr e, Ctx ctx, Medida m, Set<Expr> resaltar, Color bg, Color fg) {
        Caja c;
        if (e instanceof Var v) {
            c = new Texto(v.nombre, m);
        } else if (e instanceof Constant k) {
            c = new Texto(k.value ? "1" : "0", m);
        } else if (e instanceof Not n) {
            // La raya cubre todo el grupo: no hacen falta paréntesis dentro de una negación.
            c = new Sobre(construir(n.hijo, Ctx.NOT, m, resaltar, bg, fg), m);
        } else if (e instanceof And a) {
            c = unir(a.operandos, "*", Ctx.AND, m, resaltar, bg, fg);
        } else if (e instanceof Or o) {
            c = unir(o.operandos, "+", Ctx.OR, m, resaltar, bg, fg);
            if (ctx == Ctx.AND || ctx == Ctx.XOR) c = new Parentesis(c);
        } else if (e instanceof Xor x) {
            List<Caja> partes = new ArrayList<>();
            partes.add(construir(x.izq, Ctx.XOR, m, resaltar, bg, fg));
            partes.add(new Espacio(5));
            partes.add(new Circulo(x.negado, m));
            partes.add(new Espacio(5));
            partes.add(construir(x.der, Ctx.XOR, m, resaltar, bg, fg));
            c = new Fila(partes);
            if (ctx == Ctx.AND || ctx == Ctx.OR || ctx == Ctx.XOR) c = new Parentesis(c);
        } else {
            c = new Texto(String.valueOf(e), m);
        }
        if (resaltar.contains(e)) c = new Resalte(c, bg, fg);
        return c;
    }

    private static Caja unir(List<Expr> operandos, String op, Ctx ctx, Medida m,
                             Set<Expr> resaltar, Color bg, Color fg) {
        List<Caja> partes = new ArrayList<>();
        for (int i = 0; i < operandos.size(); i++) {
            if (i > 0) {
                partes.add(new Espacio(5));
                partes.add(new Texto(op, m));
                partes.add(new Espacio(5));
            }
            partes.add(construir(operandos.get(i), ctx, m, resaltar, bg, fg));
        }
        return new Fila(partes);
    }

    // ---------- Cajas ----------

    private static final class Medida {
        final Font fuente;
        final FontMetrics fm;
        final int cap;

        Medida(int tamano) {
            fuente = new Font(Font.SERIF, Font.PLAIN, tamano);
            fm = MEDIDOR.getFontMetrics(fuente);
            Rectangle2D r = new TextLayout("H", fuente, MEDIDOR.getFontRenderContext()).getBounds();
            cap = (int) Math.ceil(r.getHeight());
        }
    }

    /** Elemento dibujable con ancho y alto sobre/bajo la línea base. */
    abstract static class Caja {
        int w, asc, desc;
        abstract void pintar(Graphics2D g, int x, int y);
    }

    private static final class Texto extends Caja {
        private final String s;
        private final Medida m;

        Texto(String s, Medida m) {
            this.s = s;
            this.m = m;
            w = m.fm.stringWidth(s);
            asc = m.cap;
            desc = Math.max(2, m.fm.getDescent() / 2);
        }

        @Override
        void pintar(Graphics2D g, int x, int y) {
            g.setFont(m.fuente);
            g.drawString(s, x, y);
        }
    }

    private static final class Espacio extends Caja {
        Espacio(int ancho) { w = ancho; }
        @Override void pintar(Graphics2D g, int x, int y) { }
    }

    private static final class Fila extends Caja {
        private final List<Caja> partes;

        Fila(List<Caja> partes) {
            this.partes = partes;
            for (Caja p : partes) {
                w += p.w;
                asc = Math.max(asc, p.asc);
                desc = Math.max(desc, p.desc);
            }
        }

        @Override
        void pintar(Graphics2D g, int x, int y) {
            int cx = x;
            for (Caja p : partes) {
                p.pintar(g, cx, y);
                cx += p.w;
            }
        }
    }

    /** Raya de negación sobre el contenido (las rayas contiguas quedan separadas por un hueco). */
    private static final class Sobre extends Caja {
        private static final int LADO = 3, SEP = 3;
        private final Caja in;

        Sobre(Caja in, Medida m) {
            this.in = in;
            w = in.w + 2 * LADO;
            asc = in.asc + SEP + 2;
            desc = in.desc;
        }

        @Override
        void pintar(Graphics2D g, int x, int y) {
            in.pintar(g, x + LADO, y);
            Stroke antes = g.getStroke();
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            int ly = y - in.asc - SEP;
            g.draw(new Line2D.Double(x + 1, ly, x + w - 1, ly));
            g.setStroke(antes);
        }
    }

    private static final class Circulo extends Caja {
        private final boolean xnor;
        private final Medida m;

        Circulo(boolean xnor, Medida m) {
            this.xnor = xnor;
            this.m = m;
            int d = m.cap + 5;
            w = d + 2;
            asc = (int) Math.ceil(m.cap / 2.0 + d / 2.0) + 1;
            desc = (int) Math.ceil(d / 2.0 - m.cap / 2.0) + 1;
        }

        @Override
        void pintar(Graphics2D g, int x, int y) {
            double cx = x + w / 2.0, cy = y - m.cap / 2.0, r = (w - 2) / 2.0;
            Stroke antes = g.getStroke();
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            if (!xnor) {                       // XOR: círculo con cruz
                double k = r * 0.6;
                g.draw(new Line2D.Double(cx - k, cy, cx + k, cy));
                g.draw(new Line2D.Double(cx, cy - k, cx, cy + k));
            } else {                           // XNOR / XAND: círculo con punto
                g.fill(new Ellipse2D.Double(cx - 1.9, cy - 1.9, 3.8, 3.8));
            }
            g.setStroke(antes);
        }
    }

    private static final class Parentesis extends Caja {
        private static final int ANCHO = 5;
        private final Caja in;

        Parentesis(Caja in) {
            this.in = in;
            w = in.w + 2 * (ANCHO + 1);
            asc = in.asc + 1;
            desc = in.desc + 1;
        }

        @Override
        void pintar(Graphics2D g, int x, int y) {
            in.pintar(g, x + ANCHO + 1, y);
            double top = y - asc, bot = y + desc, h = bot - top;
            Stroke antes = g.getStroke();
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Double izq = new Path2D.Double();
            izq.moveTo(x + ANCHO, top);
            izq.curveTo(x - 0.5, top + h * 0.28, x - 0.5, bot - h * 0.28, x + ANCHO, bot);
            g.draw(izq);
            double xr = x + w - ANCHO;
            Path2D.Double der = new Path2D.Double();
            der.moveTo(xr, top);
            der.curveTo(xr + ANCHO + 0.5, top + h * 0.28, xr + ANCHO + 0.5, bot - h * 0.28, xr, bot);
            g.draw(der);
            g.setStroke(antes);
        }
    }

    private static final class Resalte extends Caja {
        private static final int LADO = 3, VERT = 2;
        private final Caja in;
        private final Color bg, fg;

        Resalte(Caja in, Color bg, Color fg) {
            this.in = in;
            this.bg = bg;
            this.fg = fg;
            w = in.w + 2 * LADO;
            asc = in.asc + VERT;
            desc = in.desc + VERT;
        }

        @Override
        void pintar(Graphics2D g, int x, int y) {
            Color antes = g.getColor();
            g.setColor(bg);
            g.fillRoundRect(x, y - asc, w, asc + desc, 8, 8);
            g.setColor(fg);
            in.pintar(g, x + LADO, y);
            g.setColor(antes);
        }
    }
}