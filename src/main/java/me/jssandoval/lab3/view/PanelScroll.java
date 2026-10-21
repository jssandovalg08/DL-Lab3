package me.jssandoval.lab3.view;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

/** Panel de la lista de pasos: ocupa todo el ancho y solo hace scroll horizontal si la fórmula no entra. */
class PanelScroll extends JPanel implements Scrollable {
    PanelScroll() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 20; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? Math.max(40, r.height - 40) : Math.max(40, r.width - 40);
    }
    @Override public boolean getScrollableTracksViewportWidth() {
        return getParent() instanceof JViewport vp && getPreferredSize().width <= vp.getWidth();
    }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
