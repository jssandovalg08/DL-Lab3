package me.jssandoval.lab3.logic;

/** Clasificación (y explicación) de los mensajes que producen las reglas de simplificación. */
public final class Categorias {

    /** Categorías que se muestran en la leyenda. */
    public static final String[] TODAS = {
            "Doble negación", "De Morgan", "Identidad", "Complemento",
            "Idempotencia", "Absorción", "Absorción con negación", "Factor común", "XOR", "XNOR"
    };

    private Categorias() { }

    /** "Factor común (A): -> ..." -> "Factor común"; el resto: lo que está antes de ':'. */
    public static String deMensaje(String mensaje) {
        int idx = mensaje.indexOf(':');
        String categoria = idx >= 0 ? mensaje.substring(0, idx).trim() : mensaje;
        return categoria.startsWith("Factor común") ? "Factor común" : categoria;
    }

    public static String descripcion(String categoria) {
        if (categoria.startsWith("Absorción con negación")) return "A + Ā·B = A + B   y   A·(Ā + B) = A·B  (el negado sobra)";
        if (categoria.startsWith("Absorción")) return "A + A·B = A   y   A·(A + B) = A  (un término contiene a otro)";
        if (categoria.startsWith("XNOR")) return "Se recorta, reordena o reconoce un XNOR (negaciones, términos repetidos, factor común).";
        if (categoria.startsWith("XOR")) return "Se recorta, reordena o reconoce un XOR (negaciones, términos repetidos, factor común).";
        if (categoria.startsWith("Factor común")) return "A·B + A·C = A·(B + C)";
        if (categoria.startsWith("Idempotencia")) return "A + A = A   y   A·A = A";
        if (categoria.startsWith("Complemento")) return "Un término junto a su negado: la suma da 1, el producto da 0, el XOR da 1.";
        if (categoria.startsWith("Identidad")) return "A + 0 = A,  A·1 = A,  A + 1 = 1,  A·0 = 0  (y A XOR 0 = A,  A XOR 1 = Ā)";
        if (categoria.startsWith("De Morgan")) return "La negación de una suma es el producto de las negaciones, y al revés.";
        if (categoria.startsWith("Doble negación")) return "Dos negaciones seguidas se cancelan.";
        if (categoria.startsWith("Negación de constante")) return "El negado de 1 es 0 y el de 0 es 1.";
        return "";
    }
}
