package me.jssandoval.lab3.model;

import java.util.*;
import java.util.stream.Collectors;

public class And extends Expr {
    public List<Expr> operandos;

    public And(Expr... exprs) { operandos = new ArrayList<>(Arrays.asList(exprs)); }
    public And(List<Expr> exprs) { operandos = new ArrayList<>(exprs); }

    public Expr copiar() { return new And(operandos.stream().map(Expr::copiar).collect(Collectors.toList())); }

    private static List<Expr> clausulas(Expr e) {
        return e instanceof Or o ? o.operandos : List.of(e);
    }

    public Expr recomponerXor(List<String> pasos, boolean[] aplicado) {
        for (int i = 0; i < operandos.size() && !aplicado[0]; i++) {
            operandos.set(i, operandos.get(i).recomponerXor(pasos, aplicado));
        }
        if (aplicado[0]) return this;

        // (p | q) & (!p | !q) -> p ^ q   (con negaciones dentro: (A | !B) & (!A | B) -> A XNOR B)
        for (int i = 0; i < operandos.size(); i++) {
            for (int j = i + 1; j < operandos.size(); j++) {
                if (operandos.get(i) instanceof Or o1 && operandos.get(j) instanceof Or o2
                        && o1.operandos.size() == 2 && o2.operandos.size() == 2
                        && sonComplementarios(o1.operandos, o2.operandos)) {
                    Xor xor = (Xor) construirXor(o1.operandos.get(0), o1.operandos.get(1), false);
                    pasos.add((xor.negado ? "XNOR: " : "XOR: ") + o1 + " & " + o2 + " -> " + xor);
                    operandos.set(i, xor);
                    operandos.remove(j);
                    aplicado[0] = true;
                    return operandos.size() == 1 ? operandos.get(0) : this;
                }
            }
        }
        return this;
    }

    public boolean contiene(Expr e) {
        return operandos.stream().anyMatch(o -> o.esIgual(e));
    }

    public Expr simplificar(List<String> pasos, boolean[] aplicado) {
        for (int i = 0; i < operandos.size() && !aplicado[0]; i++) {
            operandos.set(i, operandos.get(i).simplificar(pasos, aplicado));
        }
        if (aplicado[0]) return this;

        // Aplanado: (A & B) & C -> A & B & C (silencioso, no es una "regla" visible)
        List<Expr> aplanados = new ArrayList<>();
        for (Expr e : operandos) {
            if (e instanceof And) aplanados.addAll(((And) e).operandos);
            else aplanados.add(e);
        }
        if (aplanados.size() != operandos.size()) operandos = aplanados;

        // Identidad/Dominancia con constantes: si hay un 0, todo el AND es 0
        if (operandos.stream().anyMatch(e -> e instanceof Constant c && !c.value)) {
            Expr resultado = new Constant(false);
            pasos.add("Identidad: " + this + " -> " + resultado + " (contiene una constante 0)");
            aplicado[0] = true;
            return resultado;
        }
        // Los 1 son neutros en AND, se eliminan (A & 1 -> A)
        List<Expr> sinUnos = new ArrayList<>(operandos);
        sinUnos.removeIf(e -> e instanceof Constant c && c.value);
        if (sinUnos.size() != operandos.size()) {
            operandos = sinUnos;
            Expr resultado = operandos.isEmpty() ? new Constant(true)
                    : (operandos.size() == 1 ? operandos.get(0) : this);
            pasos.add("Identidad: constante 1 eliminada en AND -> " + resultado);
            aplicado[0] = true;
            return resultado;
        }

        // Complemento: A & !A -> 0
        for (Expr x : operandos) {
            for (Expr y : operandos) {
                if (x.esComplementoDe(y)) {
                    Expr resultado = new Constant(false);
                    pasos.add("Complemento: " + this + " -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        // Idempotencia: A & A -> A
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

        // Absorción: A & (A | B) -> A  y, en general, C1 & (C1 | X) -> C1
        // (una cláusula cuyos términos contienen a todos los de otra es redundante)
        for (int i = 0; i < operandos.size(); i++) {
            List<Expr> absorbente = clausulas(operandos.get(i));
            for (int j = 0; j < operandos.size(); j++) {
                if (i == j) continue;
                List<Expr> candidato = clausulas(operandos.get(j));
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

        // Absorción con término anidado: si un término de un OR está formado solo por
        // factores que ya están en el AND, ese OR es redundante:
        // !A & !B & (!E | (!A & !B)) -> !A & !B
        for (int j = 0; j < operandos.size(); j++) {
            if (!(operandos.get(j) instanceof Or o)) continue;
            List<Expr> otros = new ArrayList<>(operandos);
            otros.remove(j);
            if (otros.isEmpty()) continue;
            for (Expr t : o.operandos) {
                List<Expr> factores = t instanceof And a ? a.operandos : List.of(t);
                if (contieneTodos(otros, factores)) {
                    Expr eliminado = operandos.remove(j);
                    Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                    pasos.add("Absorción: " + eliminado + " es redundante (contiene " + t + ") -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        // Absorción con negación (consenso): A & (!A | B) -> A & B
        for (int i = 0; i < operandos.size(); i++) {
            Expr x = operandos.get(i);
            if (x instanceof Or) continue;
            for (int j = 0; j < operandos.size(); j++) {
                if (i == j) continue;
                if (operandos.get(j) instanceof Or or) {
                    Expr aEliminar = or.operandos.stream()
                            .filter(e -> e.esComplementoDe(x))
                            .findFirst().orElse(null);
                    if (aEliminar != null) {
                        or.operandos.remove(aEliminar);
                        Expr reemplazo;
                        if (or.operandos.isEmpty()) reemplazo = new Constant(false);
                        else if (or.operandos.size() == 1) reemplazo = or.operandos.get(0);
                        else reemplazo = or;
                        operandos.set(j, reemplazo);
                        Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                        pasos.add("Absorción con negación: se elimina " + aEliminar + " (complemento de " + x + ") -> " + resultado);
                        aplicado[0] = true;
                        return resultado;
                    }
                }
            }
        }

        // XOR/XNOR bajo contexto: A & (A ^ B) -> A & !B ;  A & (A XNOR B) -> A & B
        // (dentro de un AND, cada término vale 1 al evaluar los demás)
        for (int i = 0; i < operandos.size(); i++) {
            Expr x = operandos.get(i);
            for (int j = 0; j < operandos.size(); j++) {
                if (i == j || !(operandos.get(j) instanceof Xor xr)) continue;
                Expr reducido = xorBajoSupuesto(xr, x, true);
                if (reducido != null) {
                    operandos.set(j, reducido);
                    Expr resultado = operandos.size() == 1 ? operandos.get(0) : this;
                    pasos.add("Absorción con negación: " + xr + " se reduce a " + reducido + " (porque " + x + " = 1) -> " + resultado);
                    aplicado[0] = true;
                    return resultado;
                }
            }
        }

        if (operandos.size() == 1) return operandos.get(0);
        return this;
    }

    public String toString() {
        return "(" + operandos.stream().map(Expr::toString).collect(Collectors.joining(" & ")) + ")";
    }
}
