package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.xiaomi.tsm.tp.ctx.CommandContext;
import com.xiaomi.tsm.tp.exception.AbstractErrorReporting;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @summary: 表达式 = 子表达式相加 例如把表达式 C9#(DF080101) 转化成Expression为 Expression
 *
 * @create: 2020-02-13
 */
@Getter
@Generated
public class Expression {
    private String tokens;
    @Setter
    private Operand operand;
    private ExpressionOperatorCode operatorCode;
    private List<Expression> subexpressionList;
    private transient AbstractErrorReporting errorReporting;

    public static Expression build(String tokens, ExpressionOperatorCode oc, boolean shouldParseSubexpression,
        AbstractErrorReporting errorReporting) {
        Expression exp = new Expression();
        exp.tokens = tokens;
        exp.operatorCode = oc;
        if (!shouldParseSubexpression) {
            exp.operand = Operand.build(tokens);
        }
        exp.errorReporting = errorReporting;
        return exp;
    }

    public void addChild(Expression child) {
        if (subexpressionList == null) {
            subexpressionList = new ArrayList<>();
        }
        subexpressionList.add(child);
    }

    public String compute(CommandContext ctx) {
        if (!this.checkValid()) {
            return errorReporting.expressionIsInvalid.by(ctx);
        }
        if (operand == null) {
            operand = this.computeSubexpression(ctx);
        }
        return operatorCode.compute(operand, ctx);
    }

    private Operand computeSubexpression(CommandContext ctx) {
        String result = "";
        for (Expression sub : subexpressionList) {
            result = result.concat(sub.compute(ctx));
        }
        return Operand.build(result);
    }

    private boolean checkValid() {
        return !(operatorCode == null || (operand == null && CollectionUtils.isEmpty(subexpressionList)));
    }

    @Override
    public String toString() {
        return "Expression(tokens=" + this.getTokens() + ", operatorCode=" + this.getOperatorCode()
            + (this.getOperand() == null ? "" : ", operand=" + this.getOperand())
            + (CollectionUtils.isEmpty(this.getSubexpressionList()) ? ""
                : ", subexpressionList=" + this.getSubexpressionList())
            + ")";
    }
}
