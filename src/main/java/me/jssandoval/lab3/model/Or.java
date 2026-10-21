package me.jssandoval.lab3.model;

import java.util.*;
import java.util.stream.Collectors;

public class Or extends Expr {
    public List<Expr> operandos;

    public Or(Expr... exprs) { operandos = new ArrayList<>(Arrays.asList(exprs)); }
    public Or(List<Expr> exprs) { operandos = new ArrayList<>(exprs); }

    public Expr copiar() { return new Or(operandos.stream().map(Expr::copiar).collect(Collectors.toList())); }

    public Expr recomponerXor(List<String> pasos, boolean[] aplicado) {
        for (int i = 0; i < operandos.size() && !aplicado[0]; i++) {
            operandos.set(i, operandos.get(i).recomponerXor(pasos, aplicado));
        }
        if (aplicado[0]) return this;

        // (p & q) | (!p & !q) -> p XNOR q ; con negaciones dentro: (A & !B) | (!A & B) -> A ^ B
        for (int i = 0; i < operandos.size(); i++) {
            for (int j = i + 1; j < operandos.size(); j++) {
                if (operandos.get(i) instanceof And a1 && operandos.get(j) instanceof And a2
                        && a1.operandos.size() == 2 && a2.operandos.size() == 2
                        && sonComplementarios(a1.operandos, a2.operandos)) {
                    Xor xor = (Xor) construirXor(a1.operandos.get(0), a1.operandos.get(1), true);
                    pasos.add((xor.negado ? "XNOR: " : "XOR: ") + a1 + " | " + a2 + " -> " + xor);
                    operandos.set(i, xor);
                    operandos.remove(j);
                    aplicado[0] = true;
                    return operandos.size() == 1 ? operandos.get(0) : this;
                }
            }
        }
        return this;
    }

    private static List<Expr> factores(Expr e) {
        return e instanceof And a ? a.operandos : List.of(e);
    }

    public boolean contiene(Expr e) {
        return operandos.stream().anyMatch(o -> o.esIgual(e));
    }

    public Expr simplificar(List<String> pasos, boolean[] aplicado) {
        for (int i = 0; i < operandos.size() && !aplicado[0]; i++) {
            operandos.set(i, operandos.get(i).simplificar(pasos, aplicado));
        }
        if (aplicado[0]) return this;

        // Aplanado: (A | B) | C -> A | B | C (silencioso)
        List<Expr> aplanados = new ArrayList<>();
        for (Expr e : operandos) {
            if (e instanceof Or) aplanados.addAll(((Or) e).operandos);
            else aplanados.add(e);
        }
        if (aplanados.size() != operandos.size()) operandos = aplanados;

        // Identidad/Dominancia con constantes: si hay un 1, todo el OR es 1
        if (operandos.stream().anyMatch(e -> e instanceof Constant c && c.value)) {
            Expr resultado = new Constant(true);
            pasos.add("Identidad: " + this + " -> " + resultado + " (contiene una constante 1)");
            aplicado[0] = true;
            return resultado;
        }
        // Los 0 son neutros en OR, se eliminan (A | 0 -> A)
        List<Expr> sinCeros = new ArrayList<>(operandos);
        sinCeros.removeIf(e -> e instanceof Constant c && !c.value);
        if (sinCeros.size() != operandos.size()) {
            operandos = sinCeros;
            Expr resultado = operandos.isEmpty() ? new Constant(false)
                    : (operandos.size() == 1 ? operandos.get(0) : this);
            pasos.add("Identidad: constante 0 eliminada en OR -> " + resultado);
            aplicado[0] = true;
            return resultado;
        }

        // Complemento: A | !A -> 1
        for (Expr x : operandos) {
            for (Expr y : operandos) {
                if (x.esComplementoDe(y)) {
                    Expr resultado = new Constant(true);
                    pasos.add("Complemento: " + this + " -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        // Idempotencia: A | A -> A
        List<Expr> sinDuplicados = new ArrayList<>();
        for (Expr e : operandos) {
            if (sinDuplicados.stream().noneMatch(u -> u.esIgual(e))) sinDuplicados.add(e);
        }
        if (sinDuplicados.size() != operandos.size()) {
            operandos = sinDuplicados;
            Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
            pasos.add("Idempotencia: términos duplicados eliminados -> " + resultado);
            aplicado[0] = true;
            return resultado;
        }

        // Absorción: A | (A & B) -> A  y, en general, T1 | (T1 & X) -> T1
        // (un término cuyos factores contienen a todos los de otro término es redundante)
        for (int i = 0; i < operandos.size(); i++) {
            List<Expr> absorbente = factores(operandos.get(i));
            for (int j = 0; j < operandos.size(); j++) {
                if (i == j) continue;
                List<Expr> candidato = factores(operandos.get(j));
                if (candidato.size() > absorbente.size() && contieneTodos(candidato, absorbente)) {
                    Expr eliminado = operandos.remove(j);
                    Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                    pasos.add("Absorción: " + eliminado + " es absorbido por "
                            + (i > j ? operandos.get(i - 1) : operandos.get(i)) + " -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        // Absorción con cláusula anidada (dual): si una cláusula de un AND está formada solo
        // por términos que ya están en el OR, ese AND es redundante:
        // A | B | ((A | B) & C) -> A | B
        for (int j = 0; j < operandos.size(); j++) {
            if (!(operandos.get(j) instanceof And a)) continue;
            List<Expr> otros = new ArrayList<>(operandos);
            otros.remove(j);
            if (otros.isEmpty()) continue;
            for (Expr c : a.operandos) {
                List<Expr> miembros = c instanceof Or oc ? oc.operandos : List.of(c);
                if (contieneTodos(otros, miembros)) {
                    Expr eliminado = operandos.remove(j);
                    Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                    pasos.add("Absorción: " + eliminado + " es redundante (contiene " + c + ") -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        // Absorción con negación (consenso): A | (!A & B) -> A | B
        for (int i = 0; i < operandos.size(); i++) {
            Expr x = operandos.get(i);
            if (x instanceof And) continue;
            for (int j = 0; j < operandos.size(); j++) {
                if (i == j) continue;
                if (operandos.get(j) instanceof And and) {
                    Expr aEliminar = and.operandos.stream()
                            .filter(e -> e.esComplementoDe(x))
                            .findFirst().orElse(null);
                    if (aEliminar != null) {
                        and.operandos.remove(aEliminar);
                        Expr reemplazo;
                        if (and.operandos.isEmpty()) reemplazo = new Constant(true);
                        else if (and.operandos.size() == 1) reemplazo = and.operandos.get(0);
                        else reemplazo = and;
                        operandos.set(j, reemplazo);
                        Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                        pasos.add("Absorción con negación: se elimina " + aEliminar + " (complemento de " + x + ") -> " + resultado);
                        aplicado[0] = true;
                        return resultado;
                    }
                }
            }
        }

        // Factor común: (A & B) | (A & C) -> A & (B | C)
        for (int i = 0; i < operandos.size(); i++) {
            for (int j = i + 1; j < operandos.size(); j++) {
                if (operandos.get(i) instanceof And a1 && operandos.get(j) instanceof And a2) {
                    List<Expr> comunes = new ArrayList<>(a1.operandos);
                    comunes.removeIf(e -> !a2.contiene(e));

                    if (!comunes.isEmpty()) {
                        Expr comun = comunes.get(0);

                        a1.operandos.removeIf(e -> e.esIgual(comun));
                        a2.operandos.removeIf(e -> e.esIgual(comun));

                        Expr resto1 = a1.operandos.size() == 1 ? a1.operandos.get(0) : a1;
                        Expr resto2 = a2.operandos.size() == 1 ? a2.operandos.get(0) : a2;

                        Expr nuevoOr = new Or(resto1, resto2);
                        Expr nuevoAnd = new And(comun, nuevoOr);

                        operandos.set(i, nuevoAnd);
                        operandos.remove(j);
                        Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                        pasos.add("Factor común (" + comun + "): -> " + resultado);
                        aplicado[0] = true;
                        return resultado;
                    }
                }
            }
        }

        // XOR/XNOR bajo contexto: A | (A ^ B) -> A | B ;  A | (A XNOR B) -> A | !B
        // (dentro de un OR, cada término vale 0 al evaluar los demás)
        for (int i = 0; i < operandos.size(); i++) {
            Expr x = operandos.get(i);
            for (int j = 0; j < operandos.size(); j++) {
                if (i == j || !(operandos.get(j) instanceof Xor xr)) continue;
                Expr reducido = xorBajoSupuesto(xr, x, false);
                if (reducido != null) {
                    operandos.set(j, reducido);
                    Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                    pasos.add("Absorción con negación: " + xr + " se reduce a " + reducido + " (porque " + x + " = 0) -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        if (operandos.size() == 1) return operandos.get(0);
        return this;
    }

    public String toString() {
        return "(" + operandos.stream().map(Expr::toString).collect(Collectors.joining(" | ")) + ")";
    }
}
