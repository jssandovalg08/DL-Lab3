package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.Expr;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Genera el mapa de Karnaugh de 2 o 3 variables. */
public final class MapaKarnaugh {

    private MapaKarnaugh() { }

    /** @return el mapa, o null si la cantidad de variables no es 2 ni 3 (ver mensajeNoDisponible). */
    public static Mapa generar(Expr raiz, List<String> vars) {
        int n = vars.size();
        if (n < 2 || n > 3) return null;

        int nFilas = n / 2;           // 2 vars -> 1|1, 3 vars -> 1|2
        List<String> rowVars = vars.subList(0, nFilas);
        List<String> colVars = vars.subList(nFilas, n);
        String[] rowLabels = gray(rowVars.size());
        String[] colLabels = gray(colVars.size());

        String[] columnas = new String[colLabels.length + 1];
        columnas[0] = String.join("", rowVars) + "\\" + String.join("", colVars);
        System.arraycopy(colLabels, 0, columnas, 1, colLabels.length);

        Object[][] datos = new Object[rowLabels.length][colLabels.length + 1];
        for (int r = 0; r < rowLabels.length; r++) {
            datos[r][0] = rowLabels[r];
            for (int c = 0; c < colLabels.length; c++) {
                Map<String, Boolean> valores = new TreeMap<>();
                asignarBits(rowVars, rowLabels[r], valores);
                asignarBits(colVars, colLabels[c], valores);
                datos[r][c + 1] = Evaluador.evaluar(raiz, valores) ? "1" : "0";
            }
        }
        return new Mapa(columnas, datos, List.copyOf(rowVars), List.copyOf(colVars));
    }

    public static String mensajeNoDisponible(int nVars) {
        return nVars < 2 ? "Se necesitan al menos 2 variables para un mapa de Karnaugh."
                : "El mapa de Karnaugh solo se genera para 2 o 3 variables (la expresión tiene " + nVars + ").";
    }

    static String[] gray(int bits) {
        int n = 1 << bits;
        String[] r = new String[n];
        for (int i = 0; i < n; i++) {
            int g = i ^ (i >> 1);
            StringBuilder sb = new StringBuilder();
            for (int k = bits - 1; k >= 0; k--) sb.append((g >> k) & 1);
            r[i] = sb.toString();
        }
        return r;
    }

    private static void asignarBits(List<String> variables, String bits, Map<String, Boolean> valores) {
        for (int i = 0; i < variables.size(); i++) {
            valores.put(variables.get(i), bits.charAt(i) == '1');
        }
    }
}
