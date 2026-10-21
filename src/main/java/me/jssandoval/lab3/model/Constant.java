package me.jssandoval.lab3.model;

import java.util.*;
import java.util.stream.Collectors;

public class Constant extends Expr {
    public final boolean value;
    public Constant(boolean value) { this.value = value; }

    public Expr simplificar(List<String> pasos, boolean[] aplicado) { return this; }
    public Expr copiar() { return new Constant(value); }
    public Expr recomponerXor(List<String> pasos, boolean[] aplicado) { return this; }
    public String toString() { return value ? "1" : "0"; }
}
