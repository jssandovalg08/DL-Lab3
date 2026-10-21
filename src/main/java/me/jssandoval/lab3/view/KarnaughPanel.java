package me.jssandoval.lab3.view;

import me.jssandoval.lab3.logic.Mapa;
import me.jssandoval.lab3.logic.MapaKarnaugh;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import static me.jssandoval.lab3.view.Paleta.*;

/** Pestaña 4: mapa de Karnaugh de la expresión ORIGINAL (solo 2 o 3 variables). */
class KarnaughPanel extends JPanel {

    private final JTable tabla = new JTable();
    private final JLabel etiqueta = new JLabel(" ", SwingConstants.CENTER);

    KarnaughPanel() {
        super(new BorderLayout(0, 8));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        etiqueta.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        etiqueta.setForeground(GRIS_SUAVE);
        tabla.setRowHeight(38);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        add(etiqueta, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    /** @param mapa null si no hay mapa para esa cantidad de variables */
    void mostrar(Mapa mapa, int numVariables) {
        if (mapa == null) {
            tabla.setModel(new DefaultTableModel());
            etiqueta.setText(MapaKarnaugh.mensajeNoDisponible(numVariables));
            return;
        }
        tabla.setModel(new DefaultTableModel(mapa.datos(), mapa.columnas()));
        tabla.setDefaultRenderer(Object.class, new KarnaughRenderer());
        for (int c = 0; c < tabla.getColumnCount(); c++) {
            tabla.getColumnModel().getColumn(c).setPreferredWidth(70);
        }
        etiqueta.setText("Construido con la expresión ORIGINAL (antes de simplificar) · filas: "
                + mapa.filasVars() + "  columnas: " + mapa.colVars());
    }

    private static class KarnaughRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            label.setOpaque(true);
            if (column == 0) {
                label.setBackground(INDIGO);
                label.setForeground(Color.WHITE);
            } else if ("1".equals(String.valueOf(value))) {
                label.setBackground(VERDE_CLARO);
                label.setForeground(new Color(0x03, 0x66, 0x43));
            } else {
                label.setBackground(new Color(0xF3, 0xF4, 0xF6));
                label.setForeground(GRIS_SUAVE);
            }
            return label;
        }
    }
}
