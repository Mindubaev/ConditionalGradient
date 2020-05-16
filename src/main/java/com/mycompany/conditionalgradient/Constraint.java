/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conditionalgradient;

import java.util.Map;
import lpsolve.LpSolve;

/**
 *
 * @author Artur
 */
public class Constraint {
    
    private Map<String,Double> k;
    private double b;
    private Operator operator;

    public Constraint(Map<String,Double> k, double b, Operator operator) {
        this.k = k;
        this.b = b;
        this.operator = operator;
    }

    public Map<String,Double> getK() {
        return k;
    }

    public double[] getK(String[] order) {
        double[] val=new double[order.length];
        for (int i=0;i<order.length;i++){
            val[i]=this.k.get(order[i]);
        }
        return val;
    }
    
    public double getB() {
        return b;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setK(Map<String,Double> k) {
        this.k = k;
    }

    public void setB(double b) {
        this.b = b;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
    
    public static enum Operator{
        LE(LpSolve.LE),GE(LpSolve.GE),EQ(LpSolve.EQ);
        public int val;
        Operator(int val){
            this.val=val;
        }
    }
    
}
