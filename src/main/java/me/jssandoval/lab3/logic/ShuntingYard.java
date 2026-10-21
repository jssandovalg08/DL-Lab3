package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.*;

import java.util.*;

public class ShuntingYard {
    private static int precedencia(char operador) {
        return switch (operador) {
            case '+', '|', '^', '=' -> 1;
            case '*', '&' -> 2;
            case '!', '-' -> 3;
            default -> -1;
        };
    }

    public static List<String> convertirAPostfijo(String infijo) {
        List<String> salida = new ArrayList<>();
        Deque<Character> pila = new ArrayDeque<>();

        for (int i = 0; i < infijo.length(); i++) {
            char c = infijo.charAt(i);

            if (c == ' ') continue;

            if (Character.isLetterOrDigit(c)) {
                salida.add(String.valueOf(c));
            } else if (c == '(') {
                pila.push(c);
            } else if (c == ')') {
                while (!pila.isEmpty() && pila.peek() != '(') {
                    salida.add(String.valueOf(pila.pop()));
                }
                if (!pila.isEmpty() && pila.peek() == '(') {
                    pila.pop();
                }
            } else {
                while (!pila.isEmpty() && precedencia(c) <= precedencia(pila.peek())) {
                    salida.add(String.valueOf(pila.pop()));
                }
                pila.push(c);
            }
        }

        while (!pila.isEmpty()) {
            salida.add(String.valueOf(pila.pop()));
        }

        return salida;
    }

    public static Expr construirAST(List<String> postfijo) {
        Deque<Expr> pila = new ArrayDeque<>();

        for (String token : postfijo) {
            char c = token.charAt(0);

            if (Character.isDigit(c)) {
                pila.push(new Constant(c == '1'));
            } else if (Character.isLetter(c)) {
                pila.push(new Var(token));
            } else if (c == '!' || c == '-') {
                Expr operando = pila.pop();
                pila.push(new Not(operando));
            } else {
                Expr der = pila.pop();
                Expr izq = pila.pop();

                switch (c) {
                    case '&', '*' -> pila.push(new And(izq, der));
                    case '|', '+' -> pila.push(new Or(izq, der));
                    // XNOR/XOR se mantienen como nodos propios (sin expandir a AND/OR):
                    // simplificar() sabe operar sobre ellos directamente.
                    case '=' -> pila.push(new Xor(izq, der, true));
                    case '^' -> pila.push(new Xor(izq, der, false));
                }
            }
        }
        return pila.pop();
    }
}
