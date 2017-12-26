package com.neuronrobotics.bowlerbuilder.controller.scripteditor.groovy.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import javafx.scene.Node;

public class ASTNodeViewFactory {

  public Node createView(ASTNode node) {
    switch (node.getType()) {
      case ArgumentListNode:
      case AttributeNode:
      case BreakNode:
      case BytecodeNode:
      case ContinueNode:
      case NotNode:
      case ShortTernaryNode:
        //No expression
        break;
      case ArrayNode:
      case BitwiseNegationNode:
      case BooleanNode:
      case CastNode:
      case ClassExpressionNode:
      case ClosureListNode:
      case ClosureNode:
      case ConstantNode:
      case ConstructorCallNode:
      case DoWhileNode:
      case FieldNode:
      case GStringNode:
      case ListNode:
      case MapNode:
      case MethodCallNode:
      case MethodPointerNode:
      case PostfixNode:
      case PrefixNode:
      case PropertyNode:
      case RangeNode:
      case ReturnNode:
      case SpreadMapNode:
      case SpreadNode:
      case StaticMethodCallNode:
      case TernaryNode:
      case TupleNode:
      case UnaryMinusNode:
      case UnaryPlusNode:
      case WhileNode:
        //One expression
        break;
      case CaseNode:
      case CatchNode:
      case ExpressionNode:
      case ThrowNode:
        //One ASTNode
        break;
      case ForNode:
      case MapEntryNode:
      case SynchronizedNode:
        //Two ASTNodes
        break;
      case BinaryNode:
        break;
      case BlockNode:
        break;
      case ClassDeclarationNode:
        break;
      case DeclarationNode:
        break;
      case IfElseNode:
        break;
      case MethodNode:
        break;
      case SwitchNode:
        break;
      case TryCatchFinallyNode:
        break;
      case VariableNode:
        break;
    }

    return null;
  }

}
