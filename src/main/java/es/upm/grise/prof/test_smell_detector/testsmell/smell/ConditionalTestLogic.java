package es.upm.grise.prof.test_smell_detector.testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import es.upm.grise.prof.test_smell_detector.testsmell.AbstractSmell;
import es.upm.grise.prof.test_smell_detector.testsmell.SmellyElement;
import es.upm.grise.prof.test_smell_detector.testsmell.TestMethod;
import es.upm.grise.prof.test_smell_detector.testsmell.Util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*
This class check a test method for the existence of loops and conditional statements in the methods body
 */
public class ConditionalTestLogic extends AbstractSmell {
    private List<SmellyElement> smellyElementList;

    public ConditionalTestLogic() {
        smellyElementList = new ArrayList<>();
    }

    /**
     * Checks of 'Conditional Test Logic' smell
     */
    @Override
    public String getSmellName() {
        return "Conditional Test Logic";
    }

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that use conditional statements
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        ConditionalTestLogic.ClassVisitor classVisitor;
        classVisitor = new ConditionalTestLogic.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);
    }

    /**
     * Returns the set of analyzed elements (i.e. test methods)
     */
    @Override
    public List<SmellyElement> getSmellyElements() {
        return smellyElementList;
    }


    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        private int conditionCount, ifCount, switchCount, forCount, foreachCount, whileCount = 0;
        TestMethod testMethod;

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);

                testMethod.setHasSmell(conditionCount > 0 | ifCount > 0 | switchCount > 0 | foreachCount > 0 | forCount > 0 | whileCount > 0);

                testMethod.addDataItem("ConditionCount", String.valueOf(conditionCount));
                testMethod.addDataItem("IfCount", String.valueOf(ifCount));
                testMethod.addDataItem("SwitchCount", String.valueOf(switchCount));
                testMethod.addDataItem("ForeachCount", String.valueOf(foreachCount));
                testMethod.addDataItem("ForCount", String.valueOf(forCount));
                testMethod.addDataItem("WhileCount", String.valueOf(whileCount));

                smellyElementList.add(testMethod);

                //reset values for next method
                currentMethod = null;
                conditionCount = 0;
                ifCount = 0;
                switchCount = 0;
                forCount = 0;
                foreachCount = 0;
                whileCount = 0;
            }
        }


        @Override
        public void visit(IfStmt n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                ifCount++;
            }
        }

        @Override
        public void visit(SwitchStmt n, Void arg) {

            super.visit(n, arg);
            if (currentMethod != null) {
                switchCount++;
            }
        }

        @Override
        public void visit(ConditionalExpr n, Void arg) {

            super.visit(n, arg);
            if (currentMethod != null) {
                conditionCount++;
            }
        }

        @Override
        public void visit(ForStmt n, Void arg) {

            super.visit(n, arg);
            if (currentMethod != null) {
                forCount++;
            }
        }

        @Override
        public void visit(ForeachStmt n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                foreachCount++;
            }
        }

        @Override
        public void visit(WhileStmt n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                whileCount++;
            }
        }
    }

}
