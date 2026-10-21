package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.*;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Diferencia estructural entre dos árboles: qué nodos cambiaron en un paso (por identidad de objeto). */
public final class DiffMarcador {

    private DiffMarcador() { }

    public static Set<Expr> nuevoConjunto() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /**
     * Marca los nodos que difieren entre "a" (antes) y "b" (después): baja por la
     * estructura mientras coincida y marca el subárbol completo donde deja de coincidir.
     */
    public static void marcar(Expr a, Expr b, Set<Expr> ha, Set<Expr> hb) {
        if (a.toString().equals(b.toString())) return;
        if (a instanceof Not na && b instanceof Not nb) {
            marcar(na.hijo, nb.hijo, ha, hb);
        } else if (a instanceof And x && b instanceof And y) {
            marcarListas(x.operandos, y.operandos, a, b, ha, hb);
        } else if (a instanceof Or x && b instanceof Or y) {
            marcarListas(x.operandos, y.operandos, a, b, ha, hb);
        } else if (a instanceof Xor x && b instanceof Xor y && x.negado == y.negado) {
            marcar(x.izq, y.izq, ha, hb);
            marcar(x.der, y.der, ha, hb);
        } else {
            ha.add(a);
            hb.add(b);
        }
    }

    private static void marcarListas(List<Expr> la, List<Expr> lb, Expr a, Expr b, Set<Expr> ha, Set<Expr> hb) {
        if (la.size() == lb.size()) {
            for (int i = 0; i < la.size(); i++) marcar(la.get(i), lb.get(i), ha, hb);
            return;
        }
        // Distinto tamaño: se quitó o agregó algo. Se ignora el prefijo/sufijo común.
        int p = 0;
        while (p < la.size() && p < lb.size() && la.get(p).toString().equals(lb.get(p).toString())) p++;
        int s = 0;
        while (s < la.size() - p && s < lb.size() - p
                && la.get(la.size() - 1 - s).toString().equals(lb.get(lb.size() - 1 - s).toString())) s++;
        for (int i = p; i < la.size() - s; i++) ha.add(la.get(i));
        for (int i = p; i < lb.size() - s; i++) hb.add(lb.get(i));
        if (p + s >= la.size()) ha.add(a);   // solo se agregó algo: se marca el contenedor
        if (p + s >= lb.size()) hb.add(b);   // solo se quitó algo: se marca el resultado completo
    }
}
