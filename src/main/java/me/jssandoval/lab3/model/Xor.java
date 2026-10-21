package me.jssandoval.lab3.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * XOR (A ^ B) o, con negado = true, XNOR / "XAND" (A XNOR B). Solo lo produce la
 * fase 2 (recomponerXor): el parser expande el XOR a AND/OR al inicio.
 */
public class Xor extends Expr {
    public Expr izq, der;
    public boolean negado;

    public Xor(Expr izq, Expr der, boolean negado) {
        this.izq = izq;
        this.der = der;
        this.negado = negado;
    }

    public Expr simplificar(List<String> pasos, boolean[] aplicado) {
        if (!aplicado[0]) izq = izq.simplificar(pasos, aplicado);
        if (!aplicado[0]) der = der.simplificar(pasos, aplicado);
        if (aplicado[0]) return this;

        // Constantes: A ^ 0 -> A, A ^ 1 -> !A (y al revés para XNOR)
        if (izq instanceof Constant || der instanceof Constant) {
            Expr otro = izq instanceof Constant ? der : izq;
            Constant k = (Constant) (izq instanceof Constant ? izq : der);
            if (otro instanceof Constant k2) {
                Expr resultado = new Constant((k.value ^ k2.value) != negado);
                pasos.add("Identidad: " + this + " -> " + resultado);
                aplicado[0] = true;
                return resultado;
            }
            Expr resultado = (k.value != negado) ? new Not(otro) : otro;
            pasos.add("Identidad: " + this + " -> " + resultado);
            aplicado[0] = true;
            return resultado;
        }

        // A ^ A -> 0 (XNOR: 1) ; A ^ !A -> 1 (XNOR: 0)
        if (izq.esIgual(der)) {
            Expr resultado = new Constant(negado);
            pasos.add("Complemento: " + this + " -> " + resultado);
            aplicado[0] = true;
            return resultado;
        }
        if (izq.esComplementoDe(der)) {
            Expr resultado = new Constant(!negado);
            pasos.add("Complemento: " + this + " -> " + resultado);
            aplicado[0] = true;
            return resultado;
        }

        // Sacar negaciones de los operandos: !A ^ B -> A XNOR B ; (A XNOR B) ^ C -> (A ^ B) XNOR C
        if (izq instanceof Not || der instanceof Not
                || (izq instanceof Xor a && a.negado) || (der instanceof Xor b && b.negado)) {
            Expr nuevo = construirXor(izq, der, negado);
            pasos.add((((Xor) nuevo).negado ? "XNOR: " : "XOR: ") + this + " -> " + nuevo);
            aplicado[0] = true;
            return nuevo;
        }

        // Cancelación en cadenas de XOR: (A ^ B) ^ B -> A ; (A ^ B) ^ (C ^ A) -> B ^ C
        // (cada término repetido se cancela de a pares, x ^ x = 0)
        List<Expr> cadena = new ArrayList<>();
        aplanarXor(this, cadena);
        for (int i = 0; i < cadena.size(); i++) {
            for (int j = i + 1; j < cadena.size(); j++) {
                if (cadena.get(i).esIgual(cadena.get(j))) {
                    Expr repetido = cadena.get(i);
                    cadena.remove(j);
                    cadena.remove(i);
                    Expr resultado;
                    if (cadena.isEmpty()) {
                        resultado = new Constant(negado);
                    } else {
                        Expr r = cadena.get(0);
                        for (int k = 1; k < cadena.size(); k++) r = new Xor(r, cadena.get(k), false);
                        if (negado) r = (r instanceof Xor rx) ? new Xor(rx.izq, rx.der, true) : new Not(r);
                        resultado = r;
                    }
                    pasos.add((negado ? "XNOR: " : "XOR: ") + "cancelación de " + repetido + " en " + this + " -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        // Factor común dentro del XOR: (F & X) ^ (F & Y) -> F & (X ^ Y)
        // y para XNOR: (F & X) XNOR (F & Y) -> !F | (X XNOR Y)
        List<Expr> fi = izq instanceof And ai ? ai.operandos : List.of(izq);
        List<Expr> fd = der instanceof And ad ? ad.operandos : List.of(der);
        List<Expr> comunes = new ArrayList<>();
        for (Expr e : fi) {
            if (fd.stream().anyMatch(g -> g.esIgual(e)) && comunes.stream().noneMatch(c -> c.esIgual(e))) comunes.add(e);
        }
        if (!comunes.isEmpty()) {
            Expr restoI = restoSin(fi, comunes);
            Expr restoD = restoSin(fd, comunes);
            Expr factor = comunes.size() == 1 ? comunes.get(0) : new And(comunes);
            Expr resultado = negado
                    ? new Or(new Not(factor), new Xor(restoI, restoD, true))
                    : new And(factor, new Xor(restoI, restoD, false));
            pasos.add((negado ? "XNOR: " : "XOR: ") + "factor común (" + factor + ") en " + this + " -> " + resultado);
            aplicado[0] = true;
            return resultado;
        }
        return this;
    }

    /** Aplana una cadena de XOR no negados en sus términos: (A ^ (B ^ C)) -> [A, B, C]. */
    private static void aplanarXor(Expr e, List<Expr> salida) {
        if (e instanceof Xor x && !x.negado) {
            aplanarXor(x.izq, salida);
            aplanarXor(x.der, salida);
        } else if (e instanceof Xor x) {
            // XNOR: el nivel superior lo maneja el llamador; aquí solo se baja por el primero
            aplanarXor(x.izq, salida);
            aplanarXor(x.der, salida);
        } else {
            salida.add(e);
        }
    }

    /** Lo que queda de "factores" al quitar los "comunes"; vacío = 1, uno solo = ese, varios = AND. */
    private static Expr restoSin(List<Expr> factores, List<Expr> comunes) {
        List<Expr> resto = new ArrayList<>();
        for (Expr f : factores) {
            if (comunes.stream().noneMatch(c -> c.esIgual(f))) resto.add(f);
        }
        if (resto.isEmpty()) return new Constant(true);
        return resto.size() == 1 ? resto.get(0) : new And(resto);
    }

    public Expr recomponerXor(List<String> pasos, boolean[] aplicado) {
        if (!aplicado[0]) izq = izq.recomponerXor(pasos, aplicado);
        if (!aplicado[0]) der = der.recomponerXor(pasos, aplicado);
        return this;
    }

    public Expr copiar() { return new Xor(izq.copiar(), der.copiar(), negado); }

    boolean esComplementoDeXor(Xor o) {
        boolean mismoPar = (izq.esIgual(o.izq) && der.esIgual(o.der))
                || (izq.esIgual(o.der) && der.esIgual(o.izq));
        return mismoPar && negado != o.negado;
    }

    public String toString() {
        return "(" + izq + (negado ? " XNOR " : " ^ ") + der + ")";
    }
}