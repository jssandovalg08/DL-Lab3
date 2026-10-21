package me.jssandoval.lab3.view;

import me.jssandoval.lab3.logic.Tabla;
import me.jssandoval.lab3.model.Expr;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import static me.jssandoval.lab3.view.Paleta.*;

/** Pestañas 2 y 3: tabla de verdad con una columna por operación y encabezados dibujados como fórmula. */
class TablaVerdadPanel extends JPanel {

    private final JTable tabla = new JTable();

    TablaVerdadPanel() {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        tabla.setRowHeight(28);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    void mostrar(Tabla t) {
        int n = t.numVars();
        tabla.setModel(new DefaultTableModel(t.datos(), t.columnas()));
        tabla.setDefaultRenderer(Object.class, new OperacionesRenderer(n));
        // Cada columna toma el ancho de su fórmula; si no entra, la tabla hace scroll horizontal.
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getTableHeader().setDefaultRenderer(new CabeceraFormulaRenderer(t.cabeceras(), n));
        for (int c = 0; c < tabla.getColumnCount(); c++) {
            int minimo = c < n ? 50 : 70;
            int ancho = FormulaView.medir(t.cabeceras().get(c), TAM_TABLA).width + 20;
            tabla.getColumnModel().getColumn(c).setPreferredWidth(Math.max(minimo, ancho));
        }
        tabla.getTableHeader().revalidate();
        tabla.getTableHeader().repaint();
    }

    /** Dibuja el encabezado de cada columna como fórmula (con rayas, ⊕, ⊙…). */
    private static class CabeceraFormulaRenderer implements TableCellRenderer {
        private final List<Expr> exprs;
        private final int numVars;
        private final FormulaView vista = new FormulaView(null, TAM_TABLA, GRIS_TEXTO);

        CabeceraFormulaRenderer(List<Expr> exprs, int numVars) {
            this.exprs = exprs;
            this.numVars = numVars;
            vista.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            int modelo = table.convertColumnIndexToModel(column);
            boolean esFinal = modelo == exprs.size() - 1;
            vista.setExpr(modelo < exprs.size() ? exprs.get(modelo) : null);
            vista.setForeground(esFinal ? new Color(0x06, 0x5F, 0x46) : GRIS_TEXTO);
            vista.setBackground(esFinal ? VERDE_CLARO
                    : (modelo < numVars ? new Color(0xF3, 0xF4, 0xF6) : Color.WHITE));
            vista.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GRIS_BORDE));
            return vista;
        }
    }

    private static class OperacionesRenderer extends DefaultTableCellRenderer {
        private final int numVars;
        OperacionesRenderer(int numVars) { this.numVars = numVars; }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            boolean esFinal = column == table.getColumnCount() - 1;
            boolean esVariable = column < numVars;

            if (esFinal) {
                label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
                label.setBackground("1".equals(String.valueOf(value)) ? VERDE_CLARO : new Color(0xF3, 0xF4, 0xF6));
                label.setForeground(GRIS_TEXTO);
            } else if (esVariable) {
                label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                label.setBackground(new Color(0xF3, 0xF4, 0xF6));
                label.setForeground(GRIS_TEXTO);
            } else {
                label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFA, 0xFA));
                label.setForeground(GRIS_TEXTO);
            }
            return label;
        }
    }
}
