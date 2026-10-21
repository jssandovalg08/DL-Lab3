package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.Expr;

import java.util.List;

/**
 * Tabla de verdad "operación por operación": las primeras numVars columnas son variables,
 * el resto son subexpresiones (la última es la expresión completa).
 * "cabeceras" tiene una expresión por columna (para dibujar los encabezados).
 */
public record Tabla(String[] columnas, Object[][] datos, List<Expr> cabeceras, int numVars) { }
