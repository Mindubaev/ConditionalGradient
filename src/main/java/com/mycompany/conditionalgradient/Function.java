/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conditionalgradient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lpsolve.LpSolve;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

/**
 *
 * @author Artur
 */
public class Function {

    private Map<String,double[]> k;
    private Map<Set<String>,double[]> compositeK=new HashMap<>();
    private double b;
    private List<Constraint> constrains=new ArrayList<>();

    public Function(double b,Map<String,double[]> k,Map<Set<String>,double[]> compositeK) {
        this.b = b;
        this.k=k;
        this.compositeK=compositeK;
    }

    public Function() {
        this.k=new HashMap<>();
    }
    
    public void addСompositeK(double[] k,String...varNames){
        Set<String> vars=new HashSet<>();
        for (String varName:varNames)
            vars.add(varName);
        this.compositeK.put(vars, k);
    }
    
    public void addVarK(String varName,double[] k){
        this.getK().put(varName, k);
    }
    
    public double getDerivative(String varName,Map<String,Double> point) throws Exception{
        double[] varK=this.getK().get(varName);
        
        int maxSizeOfk=1;//проставляем индексы и выяснеяем количество переменных
        Map<String,Integer> indexes=new HashMap<>();
        indexes.put(varName, Integer.valueOf(0));
        int indexCounter=1;
        for (Set<String> key:this.compositeK.keySet()){
            if(key.contains(varName)){
                if (key.size()>maxSizeOfk)
                    maxSizeOfk=key.size();
                for (String var:key){
                    if (!indexes.containsKey(var)){
                        indexes.put(var, indexCounter);
                        indexCounter++;
                    }
                }
            }
        }
        
        if (varK==null)
            throw new Exception("unexpected var name");
        DerivativeStructure deriv=new DerivativeStructure(maxSizeOfk, 1);
        DerivativeStructure dPart=new DerivativeStructure(maxSizeOfk,1,indexes.get(varName),point.get(varName));
        for (int i=0;i<varK.length;i++){
            deriv=deriv.add(dPart.pow(i+1).multiply(varK[i]));
        }
        for (Set<String> key:this.compositeK.keySet()){
            if(key.contains(varName)){
                dPart=new DerivativeStructure(maxSizeOfk, 1,indexes.get(varName),point.get(varName));
                for (String val:key){
                    if (!val.equals(varName))
                        dPart=dPart.multiply(new DerivativeStructure(maxSizeOfk, 1, indexes.get(val),point.get(val)));
                }
            }
            for (int i=0;i<this.compositeK.get(key).length;i++){
                deriv=deriv.add(dPart.pow(i+1).multiply(this.compositeK.get(key)[i]));
            }
        }
        deriv=deriv.add(this.b);
        
        int[] derivOrder= new int[maxSizeOfk];
        for (int i=0;i<maxSizeOfk;i++){
            derivOrder[i]=(i==0)?1:0;
        }
        
        return deriv.getPartialDerivative(derivOrder);
    }
    
    public double getValue(Map<String,Double> point) throws Exception{
        double res=this.b;
        if (point.size()!=this.getK().size())
            throw new Exception("num of vars doesnt match");
        res+=getValOfSimplexK(point);
        res+=getValOfCompositeK(point);
        return res;
    }
    
    public double getValOfSimplexK(Map<String,Double> point){
        double res=0;
        for (String varName:this.getK().keySet()){
            double val=point.get(varName);
            double[] varK=getK().get(varName);
            for (int i=0;i<varK.length;i++){
                res+=Math.pow(val, i+1)*varK[i];
            }
        }
        return res;
    }
    
    public double getValOfCompositeK(Map<String,Double> point){
        double res=0;
        for (Set<String> nameSet:this.compositeK.keySet()){
            double m=1;
            for (String name:nameSet)
                m=m*point.get(name);
            double[] currentK=this.compositeK.get(nameSet);
            for (int i=0;i<currentK.length;i++){
                res+=Math.pow(m,i+1)*currentK[i];
            }
        }
        return res;
    }

    public Decision minimize(double eps,Double step,Map<String,Double> point) throws Exception{
        double currentF=0;
        double nextF=0;
        Map<String,Double> newPoint=point;
        do{
           point=newPoint;
           currentF=nextF;
           String[] order=new String[this.k.size()];
           currentF=getValue(point);
           double[] linF=new double[this.k.size()];
           int i=0;
            for (String varName:this.k.keySet()){
                linF[i]=getDerivative(varName, point);
                order[i]=varName;
                i++;
            }
            LpSolve lpSolve=LpSolve.makeLp(0, linF.length);
            lpSolve.setTrace(false);
            lpSolve.setDebug(false);
            lpSolve.setVerbose(0);
            lpSolve.setObjFn(prepareArrayForLp(linF));
            for (Constraint c:this.constrains)
                lpSolve.addConstraint(prepareArrayForLp(c.getK(order)),c.getOperator().val, c.getB());
            lpSolve.printLp();
            lpSolve.solve();
            lpSolve.printSolution(1);
            double[] res=lpSolve.getPtrVariables();
            lpSolve.deleteLp();
            newPoint=new HashMap<>();
            for (int j=0;j<order.length;j++){
                String varName=order[j];
                double newVal=point.get(varName)+step*(res[j]-point.get(varName));
                newPoint.put(varName, newVal);
            }
            nextF=getValue(newPoint);
        }while(Math.abs(nextF-currentF)>eps);
        return new Decision(newPoint,nextF);
    }
    
    private double[] prepareArrayForLp(double[] arr){
        double[] newArr=new double[arr.length+1];
        newArr[0]=0;
        for (int i=0;i<arr.length;i++){
            newArr[i+1]=arr[i];
        }
        return newArr;
    }
    
    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }
    
    public void addConstraint(Constraint constraint) throws Exception{
        if (this.k.size()!=constraint.getK().size())
            throw new Exception("can not mutch constraint");
        getConstrains().add(constraint);
    }
    
    public boolean varIsExist(String varName){
        return this.k.get(varName)!=null;
    }

    public List<Constraint> getConstrains() {
        return constrains;
    }

    public void setConstrains(List<Constraint> constrains) {
        this.constrains = constrains;
    }

    public Map<String,double[]> getK() {
        return k;
    }

    public void setK(Map<String,double[]> k) {
        this.k = k;
    }
    
    public static class Decision{
        
        private Map<String,Double> solution;
        private double funcVal;

        public Decision(Map<String, Double> solution, double funcVal) {
            this.solution = solution;
            this.funcVal = funcVal;
        }
        
        public Map<String,Double> getSolution() {
            return solution;
        }

        public double getFuncVal() {
            return funcVal;
        }

        public void setSolution(Map<String,Double> solution) {
            this.solution = solution;
        }

        public void setFuncVal(double funcVal) {
            this.funcVal = funcVal;
        }
        
    }
    
}
