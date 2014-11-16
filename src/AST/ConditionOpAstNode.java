package AST;

import IR.*;
import Nucleus.Operand;
import SymbolScope.ScopeNode;

public class ConditionOpAstNode extends AstNode{
    public static enum OpType {
        GT, LT, GE, LE, EQ, NE
    }

    public OpType op;
    public AstNode right;
    public AstNode left;
    public String jumpLabel;

    public ConditionOpAstNode(OpType op, AstNode left, AstNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public IrCode generateIrCode(ScopeNode scope) {
        IrCode lC = left.generateIrCode(scope);
        IrCode rC = right.generateIrCode(scope);
        Operand.DataType resultDataType = lC.result.dataType;
        IrCode self = new IrCode();

        rC.rlvalue = IrCode.RLValue.RVALUE;
        lC.rlvalue = IrCode.RLValue.RVALUE;

        self.irNodeList.addAll(lC.irNodeList);
        self.irNodeList.addAll(rC.irNodeList);

        IrNode.Opcode opcode = IrNode.Opcode.UNKNWN;
        IrNode.Opcode st_opcode = IrNode.Opcode.UNKNWN;

        if(resultDataType == Operand.DataType.INT) {
            st_opcode = IrNode.Opcode.STOREI;

        }
        else if(resultDataType == Operand.DataType.FLOAT) {
            st_opcode = IrNode.Opcode.STOREF;
        }

        switch (op) {
            case GT: {
                opcode = IrNode.Opcode.GT;
                break;
            }
            case LT: {
                opcode = IrNode.Opcode.LT;
                break;
            }
            case GE: {
                opcode = IrNode.Opcode.GE;
                break;
            }
            case LE: {
                opcode = IrNode.Opcode.LE;
                break;
            }
            case NE: {
                opcode = IrNode.Opcode.NE;
                break;
            }
            case EQ: {
                opcode = IrNode.Opcode.EQ;
                break;
            }
        }

        // Right child needs to be a temp
        if(rC.result.operandType == Operand.OperandType.TEMPORARY) {
            self.irNodeList.add(new CTypeIrNode(opcode, lC.result, rC.result, this.jumpLabel));
        }
        else {
            self.irNodeList.add(new MTypeIrNode(st_opcode, rC.result, scope.createTemp(resultDataType)));
            self.irNodeList.add(new CTypeIrNode(opcode, lC.result, scope.getCurrentTemp(), this.jumpLabel));
        }
        return self;
    }
}
