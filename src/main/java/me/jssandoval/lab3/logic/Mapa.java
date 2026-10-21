package me.jssandoval.lab3.logic;

import java.util.List;

/** Mapa de Karnaugh: la primera columna son las etiquetas de fila (código Gray). */
public record Mapa(String[] columnas, Object[][] datos, List<String> filasVars, List<String> colVars) { }
