package uk.ac.sheffield.com1003.cafe.causality;

import com.github.javaparser.ast.expr.Expression;

public class Argument {

    public static boolean checkArguments(Expression argument) {
        return argument.isMethodCallExpr();
    }

    /**
     * If one of the argument of a method call is another method call expression, connect these two methods in the method call graph.
     *
     * @param expr          argument
     * @param graph         graph
     * @param startNodeID   caller node simple name
     * @param startNodeName caller node qualified name
     */
    public static void addArgumentMethodCall(Expression expr, Digraph graph, String startNodeID, String startNodeName) {
        // If the argument is a method call expression
        if (expr.isMethodCallExpr() && expr.asMethodCallExpr().getScope().isPresent()) {
            String className = expr.asMethodCallExpr().getScope().get().calculateResolvedType().describe();
            if (className.contains(Util.PACKAGE_NAME)) {
                String argumentNode = String.join(".", Util.getLastSegment(className), expr.asMethodCallExpr().getNameAsString());
                graph.addNodeAndEdge(startNodeID, startNodeName, argumentNode, expr.asMethodCallExpr().resolve().getQualifiedName());
            }
        }
    }
}
