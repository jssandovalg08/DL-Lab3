package me.jssandoval.lab3.model;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Expr {
    /**
     * Simplifica esta expresión aplicando A LO SUMO una regla. La bandera
     * "aplicado" es compartida por toda la llamada raíz.simplificar(...):
     * apenas una regla se aplica en cualquier parte del árbol, todo el resto
     * de la recursión deja de tocar cosas y solo devuelve lo que ya tenía.
     * Esto permite que cada iteración del bucle en Main/GUI corresponda a
     * una única transformación identificable.
     */
    public abstract Expr simplificar(List<String> pasos, boolean[] aplicado);
    public abstract String toString();

    /** Copia profunda (los nodos se modifican en el lugar al simplificar). */
    public abstract Expr copiar();

    /**
     * Fase 2 (después de simplificar): reconoce patrones XOR / XNOR y los reemplaza
     * por un nodo Xor, que es más corto que su expansión en AND/OR. Igual que
     * simplificar(), aplica a lo sumo una transformación por llamada.
     */
    public abstract Expr recomponerXor(List<String> pasos, boolean[] aplicado);

    /** true si una es la negación de la otra: A / !A  o  !A / A. */
    public boolean esComplementoDe(Expr otra) {
        return (otra instanceof Not n && n.hijo.esIgual(this))
                || (this instanceof Not m && m.hijo.esIgual(otra))
                || (this instanceof Xor a && otra instanceof Xor b && a.esComplementoDeXor(b));
    }

    /** u = (p, q) y v = (!p, !q) en cualquier orden. */
    static boolean sonComplementarios(List<Expr> u, List<Expr> v) {
        return (v.get(0).esComplementoDe(u.get(0)) && v.get(1).esComplementoDe(u.get(1)))
                || (v.get(0).esComplementoDe(u.get(1)) && v.get(1).esComplementoDe(u.get(0)));
    }

    /**
     * Construye p ^ q (o su negación, XNOR, si "negado") sacando las negaciones
     * de los operandos: !a ^ b = a XNOR b. También evita XNOR anidados: p XNOR (q XNOR r) = p ^ (q ^ r).
     */
    static Expr construirXor(Expr p, Expr q, boolean negado) {
        if (p instanceof Not n) { p = n.hijo; negado = !negado; }
        if (q instanceof Not n) { q = n.hijo; negado = !negado; }
        if (p instanceof Xor x && x.negado) { p = new Xor(x.izq, x.der, false); negado = !negado; }
        if (q instanceof Xor x && x.negado) { q = new Xor(x.izq, x.der, false); negado = !negado; }
        return new Xor(p, q, negado);
    }

    /** true si todos los elementos de "pequeno" aparecen (por igualdad) en "grande". */
    static boolean contieneTodos(List<Expr> grande, List<Expr> pequeno) {
        return pequeno.stream().allMatch(p -> grande.stream().anyMatch(g -> g.esIgual(p)));
    }

    public boolean esIgual(Expr otra) {
        return this.toString().equals(otra.toString());
    }

    /**
     * Si "xr" tiene un operando igual a x (o complemento de x) y se sabe que x vale
     * "valorX", devuelve el Xor ya reducido: p ^ q con p constante -> q o !q.
     * Devuelve null si no aplica.
     */
    static Expr xorBajoSupuesto(Xor xr, Expr x, boolean valorX) {
        Expr otro;
        boolean p;
        if (xr.izq.esIgual(x)) { p = valorX; otro = xr.der; }
        else if (xr.izq.esComplementoDe(x)) { p = !valorX; otro = xr.der; }
        else if (xr.der.esIgual(x)) { p = valorX; otro = xr.izq; }
        else if (xr.der.esComplementoDe(x)) { p = !valorX; otro = xr.izq; }
        else return null;
        boolean invertir = p != xr.negado;   // p ^ q = q si p=0, !q si p=1; XNOR invierte
        return invertir ? new Not(otro) : otro;
    }
}
