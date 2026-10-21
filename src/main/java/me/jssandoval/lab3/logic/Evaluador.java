package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** Evaluación de expresiones y utilidades sobre variables. */
public final class Evaluador {

    private Evaluador() { }

    public static boolean evaluar(Expr e, Map<String, Boolean> valores) {
        if (e instanceof Constant c) return c.value;
        if (e instanceof Var v) return Boolean.TRUE.equals(valores.get(v.nombre));
        if (e instanceof Not n) return !evaluar(n.hijo, valores);
        if (e instanceof And and) {
            boolean r = true;
            for (Expr op : and.operandos) r &= evaluar(op, valores);
            return r;
        }
        if (e instanceof Or or) {
            boolean r = false;
            for (Expr op : or.operandos) r |= evaluar(op, valores);
            return r;
        }
        if (e instanceof Xor x) {
            boolean r = evaluar(x.izq, valores) ^ evaluar(x.der, valores);
            return x.negado != r;
        }
        throw new IllegalStateException("Tipo de expresión desconocido: " + e.getClass());
    }

    /** Variables de la expresión, ordenadas alfabéticamente. */
    public static List<String> variables(Expr e) {
        TreeSet<String> set = new TreeSet<>();
        recolectar(e, set);
        return new ArrayList<>(set);
    }

    private static void recolectar(Expr e, TreeSet<String> out) {
        if (e instanceof Var v) {
            out.add(v.nombre);
        } else if (e instanceof Not n) {
            recolectar(n.hijo, out);
        } else if (e instanceof And and) {
            for (Expr op : and.operandos) recolectar(op, out);
        } else if (e instanceof Or or) {
            for (Expr op : or.operandos) recolectar(op, out);
        } else if (e instanceof Xor x) {
            recolectar(x.izq, out);
            recolectar(x.der, out);
        }
    }

    /** Valores de las variables para la fila "fila" de una tabla de verdad (la primera variable es el bit más alto). */
    public static Map<String, Boolean> asignacion(List<String> vars, int fila) {
        int n = vars.size();
        Map<String, Boolean> valores = new TreeMap<>();
        for (int col = 0; col < n; col++) {
            valores.put(vars.get(col), ((fila >> (n - 1 - col)) & 1) == 1);
        }
        return valores;
    }

    public static boolean equivalentes(Expr a, Expr b, List<String> vars) {
        int filas = Math.max(1, 1 << vars.size());
        for (int fila = 0; fila < filas; fila++) {
            Map<String, Boolean> valores = asignacion(vars, fila);
            if (evaluar(a, valores) != evaluar(b, valores)) return false;
        }
        return true;
    }
}
