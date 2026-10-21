package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.Expr;

import java.util.List;

/** Todo lo que la interfaz necesita mostrar después de simplificar. */
public record Resultado(
        String entrada,
        List<String> posfijo,
        Expr original,
        List<Paso> historial,
        Expr simplificado,
        List<String> variables,
        boolean equivalente,
        Tabla tablaOriginal,
        Tabla tablaSimplificada,
        Mapa karnaugh) { }
