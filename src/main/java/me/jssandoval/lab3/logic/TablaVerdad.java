package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Genera los datos de la tabla de verdad (sin nada de Swing). */
public final class TablaVerdad {

    private TablaVerdad() { }

    public static Tabla generar(Expr raiz, List<String> vars) {
        List<Expr> operaciones = new ArrayList<>();
        recolectarOperaciones(raiz, operaciones, new TreeSet<>());
        if (operaciones.isEmpty()) {
            // Una sola variable o constante: igual se muestra una columna con el resultado.
            operaciones.add(raiz);
        }

        int n = vars.size();
        String[] columnas = new String[n + operaciones.size()];
        List<Expr> cabeceras = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            columnas[i] = vars.get(i);
            cabeceras.add(new Var(vars.get(i)));
        }
        for (int i = 0; i < operaciones.size(); i++) {
            columnas[n + i] = operaciones.get(i).toString();
            cabeceras.add(operaciones.get(i));
        }

        int filas = Math.max(1, 1 << n);
        Object[][] datos = new Object[filas][columnas.length];
        for (int fila = 0; fila < filas; fila++) {
            Map<String, Boolean> valores = Evaluador.asignacion(vars, fila);
            for (int col = 0; col < n; col++) {
                datos[fila][col] = valores.get(vars.get(col)) ? "1" : "0";
            }
            for (int i = 0; i < operaciones.size(); i++) {
                datos[fila][n + i] = Evaluador.evaluar(operaciones.get(i), valores) ? "1" : "0";
            }
        }
        return new Tabla(columnas, datos, cabeceras, n);
    }

    /** Recolecta, en post-orden, cada subexpresión compuesta única. */
    private static void recolectarOperaciones(Expr e, List<Expr> ops, TreeSet<String> vistos) {
        if (e instanceof Var || e instanceof Constant) return;
        if (e instanceof Not n) {
            recolectarOperaciones(n.hijo, ops, vistos);
        } else if (e instanceof And and) {
            for (Expr op : and.operandos) recolectarOperaciones(op, ops, vistos);
        } else if (e instanceof Or or) {
            for (Expr op : or.operandos) recolectarOperaciones(op, ops, vistos);
        } else if (e instanceof Xor x) {
            recolectarOperaciones(x.izq, ops, vistos);
            recolectarOperaciones(x.der, ops, vistos);
        }
        String key = e.toString();
        if (!vistos.contains(key)) {
            vistos.add(key);
            ops.add(e);
        }
    }
}
