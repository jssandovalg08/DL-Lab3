package me.jssandoval.lab3.view;

import me.jssandoval.lab3.logic.Categorias;
import me.jssandoval.lab3.logic.DiffMarcador;
import me.jssandoval.lab3.logic.Paso;
import me.jssandoval.lab3.logic.Resultado;
import me.jssandoval.lab3.model.Expr;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Set;

import static me.jssandoval.lab3.view.Paleta.*;

/** Pestaña 1: cada paso mostrado como el estado completo de la ecuación, resaltando lo que cambió. */
class PasosPanel extends JPanel {

    private final PanelScroll panelPasos = new PanelScroll();
    private final JScrollPane scrollPasos = new JScrollPane(panelPasos);

    PasosPanel() {
        super(new BorderLayout(0, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        add(construirLeyenda(), BorderLayout.NORTH);
        scrollPasos.setBorder(BorderFactory.createLineBorder(GRIS_BORDE));
        scrollPasos.getViewport().setBackground(Color.WHITE);
        scrollPasos.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPasos, BorderLayout.CENTER);
    }

    private JPanel construirLeyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        leyenda.setBackground(Color.WHITE);
        for (String categoria : Categorias.TODAS) {
            leyenda.add(new Chip(categoria, colorParaCategoria(categoria)));
        }
        return leyenda;
    }

    void mostrar(Resultado r) {
        panelPasos.removeAll();

        panelPasos.add(etiquetaSeccion("ENTRADA"));
        panelPasos.add(textoMono(r.entrada(), new Color(0x43, 0x38, 0xCA), new Color(0xEE, 0xF2, 0xFF)));
        panelPasos.add(Box.createVerticalStrut(8));

        panelPasos.add(etiquetaSeccion("POSTFIJO"));
        panelPasos.add(textoMono(r.posfijo().toString(), GRIS_TEXTO, new Color(0xF3, 0xF4, 0xF6)));
        panelPasos.add(Box.createVerticalStrut(8));

        panelPasos.add(etiquetaSeccion("EXPRESIÓN INICIAL"));
        panelPasos.add(tarjetaFormula(r.original(), new Color(0xF3, 0xF4, 0xF6), GRIS_SUAVE, GRIS_TEXTO));

        JLabel evolucion = new JLabel("Evolución de la expresión");
        evolucion.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        evolucion.setForeground(new Color(0x11, 0x18, 0x27));
        evolucion.setAlignmentX(Component.LEFT_ALIGNMENT);
        evolucion.setBorder(new EmptyBorder(16, 0, 8, 0));
        panelPasos.add(evolucion);

        List<Paso> historial = r.historial();
        if (historial.isEmpty()) {
            JLabel vacio = new JLabel("La expresión ya estaba en su forma mínima; no se aplicó ninguna regla.");
            vacio.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            vacio.setForeground(GRIS_SUAVE);
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelPasos.add(vacio);
        } else {
            int idx = 1;
            for (Paso paso : historial) {
                panelPasos.add(construirTarjetaPaso(idx++, paso));
                panelPasos.add(Box.createVerticalStrut(8));
            }
        }

        JPanel resultado = new JPanel(new BorderLayout(0, 4));
        resultado.setBackground(new Color(0xEC, 0xFD, 0xF5));
        resultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(0x10, 0xB9, 0x81)),
                new EmptyBorder(10, 12, 10, 12)));
        JLabel etiquetaFinal = new JLabel("RESULTADO FINAL (MÍNIMO)");
        etiquetaFinal.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        etiquetaFinal.setForeground(new Color(0x06, 0x5F, 0x46));
        resultado.add(etiquetaFinal, BorderLayout.NORTH);
        resultado.add(new FormulaView(r.simplificado(), TAM_PASOS + 2, new Color(0x06, 0x5F, 0x46)), BorderLayout.CENTER);
        resultado.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultado.setMaximumSize(new Dimension(Integer.MAX_VALUE, resultado.getPreferredSize().height));
        panelPasos.add(Box.createVerticalStrut(8));
        panelPasos.add(resultado);

        panelPasos.revalidate();
        panelPasos.repaint();
        SwingUtilities.invokeLater(() -> scrollPasos.getVerticalScrollBar().setValue(0));
    }

    private JComponent construirTarjetaPaso(int numero, Paso paso) {
        String categoria = Categorias.deMensaje(paso.mensaje());
        Color color = colorParaCategoria(categoria);

        Set<Expr> cambiosAntes = DiffMarcador.nuevoConjunto();
        Set<Expr> cambiosDespues = DiffMarcador.nuevoConjunto();
        DiffMarcador.marcar(paso.antes(), paso.despues(), cambiosAntes, cambiosDespues);

        FormulaView antes = new FormulaView(paso.antes(), TAM_PASOS, GRIS_TEXTO);
        antes.setResalte(cambiosAntes, new Color(0xFF, 0xF1, 0xA8), new Color(0x1F, 0x29, 0x37));
        FormulaView despues = new FormulaView(paso.despues(), TAM_PASOS, GRIS_TEXTO);
        despues.setResalte(cambiosDespues, mezclarConBlanco(color, 0.80f), oscurecer(color, 0.65f));

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(new Color(0xFA, 0xFA, 0xFA));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color),
                new EmptyBorder(10, 12, 10, 12)));

        Chip chip = new Chip("PASO " + numero + " · " + categoria.toUpperCase(), color);
        chip.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descripcion = new JLabel(Categorias.descripcion(categoria));
        descripcion.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        descripcion.setForeground(GRIS_SUAVE);
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        descripcion.setBorder(new EmptyBorder(4, 0, 6, 0));
        JLabel flecha = new JLabel("↓");
        flecha.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        flecha.setForeground(new Color(0x9C, 0xA3, 0xAF));
        flecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        flecha.setBorder(new EmptyBorder(2, 6, 2, 0));
        antes.setAlignmentX(Component.LEFT_ALIGNMENT);
        despues.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(chip);
        tarjeta.add(descripcion);
        tarjeta.add(antes);
        tarjeta.add(flecha);
        tarjeta.add(despues);

        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getPreferredSize().height));
        return tarjeta;
    }

    private JPanel tarjetaFormula(Expr e, Color fondo, Color borde, Color texto) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(fondo);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, borde),
                new EmptyBorder(8, 10, 8, 10)));
        p.add(new FormulaView(e, TAM_PASOS, texto), BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private JLabel etiquetaSeccion(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        l.setForeground(GRIS_SUAVE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 3, 0));
        return l;
    }

    private JComponent textoMono(String texto, Color color, Color fondo) {
        JTextArea area = new JTextArea(texto);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setForeground(color);
        area.setBackground(fondo);
        area.setEditable(false);
        area.setLineWrap(false);
        area.setBorder(new EmptyBorder(6, 10, 6, 10));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.setMaximumSize(new Dimension(Integer.MAX_VALUE, area.getPreferredSize().height));
        return area;
    }
}
