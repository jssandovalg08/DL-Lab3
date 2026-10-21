package me.jssandoval.lab3.logic;

import me.jssandoval.lab3.model.Expr;

/** Un paso atómico: el mensaje de la regla y el árbol completo antes/después. */
public record Paso(String mensaje, Expr antes, Expr despues) { }
