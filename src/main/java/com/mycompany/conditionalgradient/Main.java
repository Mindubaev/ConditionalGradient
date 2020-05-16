/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conditionalgradient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

/**
 *
 * @author Artur
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Function function=new Function();
        function.setB(0);
        function.addVarK("x1", new double[]{4,1});
        function.addVarK("x2", new double[]{-8,1.5});
        function.addСompositeK(new double[]{-2,0}, "x1","x2");
        
        Map<String,Double> point=new HashMap<>();
        point.put("x1", Double.valueOf(0));
        point.put("x2", Double.valueOf(0));
        
        Map<String,Double> c1=new HashMap<>();
        c1.put("x1", Double.valueOf(3));
        c1.put("x2", Double.valueOf(5));
        function.addConstraint(new Constraint(c1, 15, Constraint.Operator.LE));
        Map<String,Double> c2=new HashMap<>();
        c2.put("x1", Double.valueOf(1));
        c2.put("x2", Double.valueOf(-1));
        function.addConstraint(new Constraint(c2, 1, Constraint.Operator.LE));
        Map<String,Double> c3=new HashMap<>();
        c3.put("x1", Double.valueOf(1));
        c3.put("x2", Double.valueOf(0));
        function.addConstraint(new Constraint(c3, 0, Constraint.Operator.GE));
        Map<String,Double> c4=new HashMap<>();
        c4.put("x1", Double.valueOf(0));
        c4.put("x2", Double.valueOf(1));
        function.addConstraint(new Constraint(c4, 0, Constraint.Operator.GE));

        Function.Decision decision=function.minimize(0.01, 0.2,point);
        System.out.println("Solution");
        System.out.println("Func:"+decision.getFuncVal());
        System.out.println(decision.getSolution());
        
    }

}
