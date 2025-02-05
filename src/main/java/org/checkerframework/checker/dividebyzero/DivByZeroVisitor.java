package org.checkerframework.checker.dividebyzero;

import com.sun.source.tree.*;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.type.TypeKind;

import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
import org.checkerframework.checker.dividebyzero.qual.*;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

public class DivByZeroVisitor extends BaseTypeVisitor<DivByZeroAnnotatedTypeFactory> {

  /** Set of operators we care about */
  private static final Set<Tree.Kind> DIVISION_OPERATORS =
          EnumSet.of(
                  /* x /  y */ Tree.Kind.DIVIDE,
                  /* x /= y */ Tree.Kind.DIVIDE_ASSIGNMENT,
                  /* x %  y */ Tree.Kind.REMAINDER,
                  /* x %= y */ Tree.Kind.REMAINDER_ASSIGNMENT);

  /**
   * Determine whether to report an error at the given binary AST node. The error text is defined in
   * the messages.properties file.
   *
   * @param node the AST node to inspect
   * @return true if an error should be reported, false otherwise
   */
  private boolean errorAt(BinaryTree node) {
    // A BinaryTree can represent any binary operator, including + or -.
    // TODO

    // filter out non-interesting expressions
    if (!DIVISION_OPERATORS.contains(node.getKind())) {
      return false;
    }

    return hasAnnotation(node.getRightOperand(), ZeroInt.class)
            || hasAnnotation(node.getRightOperand(), Top.class)
            || hasAnnotation(node.getRightOperand(), Bottom.class);
  }

  private boolean assignmentErrorAt(Tree lhs, Tree rhs) {
    // consider assignment case only

    System.out.println("Considering assignment of " + atypeFactory.getAnnotatedType(lhs)
            + " = " + atypeFactory.getAnnotatedType(rhs)
            + " with verdict: " + isParent(lhs, rhs));
    return !isParent(lhs, rhs);
  }

  /**
   * Determine whether to report an error at the given compound assignment AST node. The error text
   * is defined in the messages.properties file.
   *
   * @param node the AST node to inspect
   * @return true if an error should be reported, false otherwise
   */
  private boolean errorAt(CompoundAssignmentTree node) {
    // A CompoundAssignmentTree represents any binary operator combined with an assignment,
    // such as "x += 10".
    // TODO

    // filter out non-interesting expressions
    if (!DIVISION_OPERATORS.contains(node.getKind())) {
      return false;
    }

    return hasAnnotation(node.getExpression(), ZeroInt.class)
            || hasAnnotation(node.getExpression(), Top.class)
            || hasAnnotation(node.getExpression(), Bottom.class);
  }

  // ========================================================================
  // Useful helpers

  private static final Set<TypeKind> INT_TYPES = EnumSet.of(TypeKind.INT, TypeKind.LONG);

  private boolean isInt(Tree node) {
    return INT_TYPES.contains(atypeFactory.getAnnotatedType(node).getKind());
  }

  private boolean hasAnnotation(Tree node, Class<? extends Annotation> c) {
    return atypeFactory.getAnnotatedType(node).hasPrimaryAnnotation(c);
  }

  private boolean isParent(Tree rhs, Tree lhs) {
    boolean result = hasAnnotation(lhs, Top.class);
    if (hasAnnotation(rhs, Top.class)) {
      return result;
    }

    if (hasAnnotation(rhs, NonZeroInt.class)) {
      return result || hasAnnotation(lhs, NonZeroInt.class);
    }
    if (hasAnnotation(rhs, ZeroInt.class)) {
      return result || hasAnnotation(lhs, ZeroInt.class);
    }

    // only bottom left possible for rhs
    return true;
  }

  // ========================================================================
  // Checker Framework plumbing

  public DivByZeroVisitor(BaseTypeChecker c) {
    super(c);
  }

  @Override
  protected boolean commonAssignmentCheck(Tree varTree, ExpressionTree valueExpTree, @CompilerMessageKey String errorKey, Object... extraArgs) {
    System.out.println("visiting assignment type: " + varTree.getKind() + " = " + valueExpTree.getKind());
    if (assignmentErrorAt(varTree, valueExpTree)) {
      checker.reportError(valueExpTree, "assignment");
    }
    return super.commonAssignmentCheck(varTree, valueExpTree, errorKey, extraArgs);
  }

  @Override
  public Void visitBinary(BinaryTree node, Void p) {
    if (isInt(node)) {
      if (errorAt(node)) {
        checker.reportError(node, "divide.by.zero");
      }
    }
    return super.visitBinary(node, p);
  }

  @Override
  public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
    if (isInt(node.getExpression())) {
      if (errorAt(node)) {
        checker.reportError(node, "divide.by.zero");
      }
    }
    return super.visitCompoundAssignment(node, p);
  }
}
