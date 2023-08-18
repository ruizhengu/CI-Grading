package uk.ac.sheffield.com1003.cafe.causality;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class MethodCall {
    public static List<String[]> data = new ArrayList<>();
    public static String aspectJPackage = "aspect";
    public static String causalityPackage = "causality";

    public static void addMethodCalls(CompilationUnit cu, Digraph graph) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration m, Void arg) {
                if (Util.filterInstrumentation(m)) {
                    System.out.println(m.resolve().getQualifiedName());
                    String callerNode = String.join(".", m.resolve().getClassName(), m.getNameAsString());
                    graph.addNodeIfNotExists(callerNode, m.resolve().getQualifiedName());
                    new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(MethodCallExpr n, Void arg) {
                            String calleeNode = String.join(".", n.resolve().getClassName(), n.getNameAsString());
                            // If the method call belongs to a class in the project
                            if (n.resolve().getQualifiedName().contains(Util.PACKAGE_NAME)) {
                                graph.addNodeAndEdge(callerNode, m.resolve().getQualifiedName(), calleeNode, n.resolve().getQualifiedName());
                            }
                            // If one of the arguments passed to a method is a method call
                            for (Expression argument : n.getArguments()) {
                                if (argument.isMethodCallExpr()) {
                                    Argument.addArgumentMethodCall(argument, graph, callerNode, m.resolve().getQualifiedName());
                                }
                            }
                            // Write all method names to a csv file
                            String[] declaration = {m.resolve().getQualifiedName()};
                            String[] callExpression = {n.resolve().getQualifiedName()};

                            if (Util.notDuplicatedArray(data, declaration) && declaration[0].contains(Util.PACKAGE_NAME)) {
                                data.add(declaration);
                            }
                            if (Util.notDuplicatedArray(data, callExpression) && callExpression[0].contains(Util.PACKAGE_NAME)) {
                                data.add(callExpression);
                            }
                        }
                    }.visit(m, null);
                }

            }
        }.visit(cu, null);
        Table.writeMethodCoverage(data);
    }
}
