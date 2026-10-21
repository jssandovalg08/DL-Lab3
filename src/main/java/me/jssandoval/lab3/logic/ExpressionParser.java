package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.Expr;

import java.util.List;

/** Texto escrito por el usuario -> posfijo -> árbol (AST). No conoce la interfaz gráfica. */
public final class ExpressionParser {

    private ExpressionParser() { }

    /** Normaliza, valida y convierte a posfijo. */
    public static List<String> aPostfijo(String texto) {
        String normalizada = normalizar(texto);
        verificarParentesis(normalizada);
        return ShuntingYard.convertirAPostfijo(normalizada);
    }

    /** Construye un árbol NUEVO cada vez (simplificar() muta el árbol en el lugar). */
    public static Expr construir(List<String> posfijo) {
        return ShuntingYard.construirAST(posfijo);
    }

    /** Atajo: texto -> árbol. */
    public static Expr parsear(String texto) {
        return construir(aPostfijo(texto));
    }

    public static void verificarParentesis(String s) {
        int nivel = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') nivel++;
            else if (c == ')' && --nivel < 0) throw new IllegalArgumentException("Hay un paréntesis de cierre sin su apertura");
        }
        if (nivel != 0) throw new IllegalArgumentException("Hay paréntesis sin cerrar");
    }

    /**
     * Lleva la notación "extendida" a la que entiende ShuntingYard:
     *  - (*), (.) o (=) -> = (XNOR / XAND)
     *  - (+) -> ^ (XOR)   (el "+" suelto sigue siendo OR y el "*" suelto AND)
     *  - los símbolos ⊕ ⊙ ¬ · también se aceptan
     *  - llaves y corchetes -> paréntesis
     *  - AND implícito: E(A+B), (A+B)C, AB, A!B  ->  E*(A+B), (A+B)*C, A*B, A*!B
     */
    public static String normalizar(String s) {
        s = s.replaceAll("\\s+", "");
        s = s.replace('⊕', '^').replace('⊙', '=').replace('¬', '!').replace('·', '*');
        s = s.replace("(+)", "^").replace("(*)", "=").replace("(.)", "=").replace("(=)", "=");
        s = s.replace('{', '(').replace('[', '(').replace('}', ')').replace(']', ')');
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i > 0) {
                char p = s.charAt(i - 1);
                boolean izq = Character.isLetterOrDigit(p) || p == ')';
                boolean der = Character.isLetterOrDigit(c) || c == '(' || c == '!';
                if (izq && der) sb.append('*');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
