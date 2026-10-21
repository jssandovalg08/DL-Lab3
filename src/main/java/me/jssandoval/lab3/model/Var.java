package me.jssandoval.lab3.model;

import java.util.*;
import java.util.stream.Collectors;

public class Var extends Expr {
    public String nombre;
    public Var(String n) { nombre = n; }

    public Expr simplificar(List<String> pasos, boolean[] aplicado) { return this; }
    public Expr copiar() { return new Var(nombre); }
    public Expr recomponerXor(List<String> pasos, boolean[] aplicado) { return this; }
    public String toString() { return nombre; }
}
