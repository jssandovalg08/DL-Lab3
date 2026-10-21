package me.jssandoval.lab3.model;

import java.util.*;
import java.util.stream.Collectors;

public class Not extends Expr {
    public Expr hijo;
    public Not(Expr h) { hijo = h; }

    public Expr copiar() { return new Not(hijo.copiar()); }

    public Expr recomponerXor(List<String> pasos, boolean[] aplicado) {
        if (!aplicado[0]) hijo = hijo.recomponerXor(pasos, aplicado);
        if (aplicado[0]) return this;
        if (hijo instanceof Xor x) {
            Expr invertido = new Xor(x.izq, x.der, !x.negado);
            pasos.add((x.negado ? "XOR: " : "XNOR: ") + this + " -> " + invertido);
            aplicado[0] = true;
            return invertido;
        }
        return this;
    }

    public Expr simplificar(List<String> pasos, boolean[] aplicado) {
        if (!aplicado[0]) {
            hijo = hijo.simplificar(pasos, aplicado);
        }
        if (aplicado[0]) return this;

        // Negación de constante: !1 -> 0, !0 -> 1
        if (hijo instanceof Constant c) {
            Constant negada = new Constant(!c.value);
            pasos.add("Negación de constante: " + this + " -> " + negada);
            aplicado[0] = true;
            return negada;
        }

        // Doble negación
        if (hijo instanceof Not) {
            Expr nieto = ((Not) hijo).hijo;
            pasos.add("Doble negación: " + this + " -> " + nieto);
            aplicado[0] = true;
            return nieto;
        }

        // Negación de XOR/XNOR: !(A ^ B) -> A XNOR B
        if (hijo instanceof Xor x) {
            Expr invertido = new Xor(x.izq, x.der, !x.negado);
            pasos.add((x.negado ? "XOR: " : "XNOR: ") + this + " -> " + invertido);
            aplicado[0] = true;
            return invertido;
        }

        // De Morgan sobre OR
        if (hijo instanceof Or or) {
            List<Expr> negados = or.operandos.stream().map(Not::new).collect(Collectors.toList());
            And nuevoAnd = new And(negados);
            pasos.add("De Morgan: " + this + " -> " + nuevoAnd);
            aplicado[0] = true;
            return nuevoAnd;
        }

        // De Morgan sobre AND
        if (hijo instanceof And and) {
            List<Expr> negados = and.operandos.stream().map(Not::new).collect(Collectors.toList());
            Or nuevoOr = new Or(negados);
            pasos.add("De Morgan: " + this + " -> " + nuevoOr);
            aplicado[0] = true;
            return nuevoOr;
        }

        return this;
    }

    public String toString() {
        if (hijo instanceof Var || hijo instanceof Constant) return "!" + hijo;
        if (hijo instanceof And || hijo instanceof Or || hijo instanceof Xor) return "!" + hijo; // ya vienen entre paréntesis
        return "!(" + hijo + ")";
    }
}
