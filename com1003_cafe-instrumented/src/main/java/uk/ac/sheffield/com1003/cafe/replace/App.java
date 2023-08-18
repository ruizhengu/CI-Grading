package uk.ac.sheffield.com1003.cafe.replace;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import uk.ac.sheffield.com1003.cafe.causality.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class App {
    public static String DIR_SOLUTION;
    public static String DIR_REFERENCE;
    public static Set<File> solutionFiles;
    public static Set<File> referenceFiles;
    public static CompilationUnit cu;
    public static BlockStmt methodToReplace;

    public App() {
        DIR_SOLUTION = Util.getOSPath();
        DIR_REFERENCE = Util.getReferencePath();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_SOLUTION));
        combinedTypeSolver.add(new JavaParserTypeSolver(DIR_REFERENCE));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        solutionFiles = Util.getFiles(new File(DIR_SOLUTION));
        referenceFiles = Util.getFiles(new File(DIR_REFERENCE));
    }

    public void solutionMethodVisitor(CompilationUnit cu, String methodName, File file) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration method, Void arg) {
                if (Util.filterInstrumentation(method)) {
                    if (method.resolve().getQualifiedName().equals(methodName)) {
                        // Replace the method body.
                        method.setBody(methodToReplace);
                        // Save the changes.
                        try {
                            FileWriter fileWriter = new FileWriter(file);
                            fileWriter.write(cu.toString());
                            fileWriter.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }.visit(cu, null);


    }

    public void replaceMethod(String methodName) {
        for (File file : solutionFiles) {
            try {
                cu = StaticJavaParser.parse(file);
                solutionMethodVisitor(cu, methodName, file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void referenceMethodVisitor(CompilationUnit cu, String methodName) {
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration method, Void arg) {
                if (Util.filterInstrumentation(method)) {
                    if (method.resolve().getQualifiedName().equals(methodName) && method.getBody().isPresent()) {
                        methodToReplace = method.getBody().get();
                    }
                }
            }
        }.visit(cu, null);
    }

    public void getReferenceImplementation(String methodName) {
        for (File file : referenceFiles) {
            try {
                cu = StaticJavaParser.parse(file);
                referenceMethodVisitor(cu, methodName);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        String confoundingMethod = "uk.ac.sheffield.com1003.cafe.Recipe.getPrice";

        App app = new App();
        app.getReferenceImplementation(confoundingMethod);
        app.replaceMethod(confoundingMethod);
    }
}
