package me.jssandoval.lab3.view;

import me.jssandoval.lab3.logic.Resultado;
import me.jssandoval.lab3.model.Expr;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Consumer;

/**
 * Ventana principal. Solo conoce cómo MOSTRAR cosas y avisar de eventos del usuario
 * (texto cambiado / simplificar); no sabe nada de cómo se simplifica.
 */
public class SimplificadorFrame extends JFrame {

    private final EncabezadoPanel encabezado = new EncabezadoPanel();
    private final PasosPanel pasos = new PasosPanel();
    private final TablaVerdadPanel tablaOriginal = new TablaVerdadPanel();
    private final TablaVerdadPanel tablaSimplificada = new TablaVerdadPanel();
    private final KarnaughPanel karnaugh = new KarnaughPanel();

    public SimplificadorFrame() {
        super("Simplificador de Álgebra Booleana");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Paleta.FONDO);
        setLayout(new BorderLayout(0, 0));

        add(encabezado, BorderLayout.NORTH);
        add(construirTabs(), BorderLayout.CENTER);

        setMinimumSize(new Dimension(980, 780));
        pack();
        setLocationRelativeTo(null);
    }

    private JTabbedPane construirTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabs.setBorder(new EmptyBorder(0, 14, 14, 14));
        tabs.addTab("1. Pasos de simplificación", pasos);
        tabs.addTab("2. Tabla de verdad · Original", tablaOriginal);
        tabs.addTab("3. Tabla de verdad · Simplificada", tablaSimplificada);
        tabs.addTab("4. Mapa de Karnaugh · Original", karnaugh);
        return tabs;
    }

    // ---------- Eventos hacia el controlador ----------

    public void setAlSimplificar(Consumer<String> accion) { encabezado.setAlSimplificar(accion); }
    public void setAlCambiarTexto(Consumer<String> accion) { encabezado.setAlCambiarTexto(accion); }

    // ---------- Órdenes desde el controlador ----------

    public void setEntrada(String texto) { encabezado.setTexto(texto); }

    public void mostrarPrevia(Expr e) { encabezado.mostrarPrevia(e); }
    public void mostrarPreviaVacia() { encabezado.mostrarPreviaVacia(); }
    public void mostrarPreviaInvalida() { encabezado.mostrarPreviaInvalida(); }

    public void mostrarResultado(Resultado r) {
        karnaugh.mostrar(r.karnaugh(), r.variables().size());
        pasos.mostrar(r);
        tablaOriginal.mostrar(r.tablaOriginal());
        tablaSimplificada.mostrar(r.tablaSimplificada());
        encabezado.mostrarResultado(r.simplificado(), r.equivalente());
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
                "No se pudo procesar la expresión:\n" + mensaje,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
