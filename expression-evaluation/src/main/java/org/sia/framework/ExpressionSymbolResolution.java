package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.xiaomi.tsm.tp.command.CommandIdentifier;
import com.xiaomi.tsm.tp.constant.Constant;
import com.xiaomi.tsm.tp.ctx.CommandContext;
import com.xiaomi.tsm.tp.exception.AbstractErrorReporting;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @summary:
 *
 * @create: 2020-07-10
 */
@Component
@Slf4j
public class ExpressionSymbolResolution {

    private final ExpressionNoOpsOperatorCode noOpsOperatorCode;

    private final ExpressionOperatorCode[] ops;

    private final AbstractErrorReporting errorReporting;

    public ExpressionSymbolResolution(ExpressionLinkedOperatorCode linkedOperatorCode,
        ExpressionLVOperatorCode lvOperatorCode, ExpressionSpecialOperatorCode commandOperatorCode,
        ExpressionNoOpsOperatorCode noOpsOperatorCode) {
        this.ops = new ExpressionOperatorCode[] {linkedOperatorCode, lvOperatorCode, commandOperatorCode};
        this.noOpsOperatorCode = noOpsOperatorCode;
        this.errorReporting = AbstractErrorReporting.create(Constant.COMMAND_GROUP_HANDLER_CODE_SYMBOL_RESOLUTION);
    }

    public void resolve(CommandContext commandContext) {
        commandContext.getCommandAPDU().setApdu(this.compute(commandContext));
    }

    private boolean shouldParseSubexpression(CommandContext ctx) {
        return CommandIdentifier.LOAD != ctx.getCid();
    }

    private String compute(CommandContext ctx) {
        boolean shouldParse = this.shouldParseSubexpression(ctx);
        Expression exp = Expression.build(ctx.getRawAPDU(), noOpsOperatorCode, shouldParse, errorReporting);
        if (shouldParse) {
            this.expandExpression(exp);
        }
        log.debug(exp.toString());
        return this.compute(exp, ctx);
    }

    private String compute(Expression exp, CommandContext ctx) {
        if (exp == null) {
            return errorReporting.expressionIsEmpty.by(ctx);
        }
        return exp.compute(ctx);
    }

    private void expandExpression(Expression expression) {
        char[] cs = expression.getTokens().toCharArray();
        int start = 0;
        for (int i = 0; i < cs.length;) {
            Optional<ExpressionOperatorCode> oc = this.matchPrefix(cs, i);
            if (oc.isPresent()) {
                boolean hasExpressionBefore = (i - start > 0);
                if (hasExpressionBefore) {
                    Expression subexpressionBefore = this.createExpression(cs, start, i, noOpsOperatorCode, false);
                    expression.addChild(subexpressionBefore);
                    start = i;
                }
                i += oc.get().prefix().length;
                int counter = 0;
                boolean shouldParseSubexpression = false;
                while (i < cs.length) {
                    if (this.matchPrefix(cs, i).isPresent()) {
                        shouldParseSubexpression = true;
                    }
                    boolean hasSamePrefix = this.matchPrefix(cs, i, oc.get());
                    if (hasSamePrefix) {
                        counter++;
                        i += oc.get().prefix().length;
                        continue;
                    }
                    boolean isFindExpression = this.matchPostfix(cs, i, oc.get());
                    if (isFindExpression) {
                        i += oc.get().postfix().length;
                        if (counter > 0) {
                            counter--;
                        } else {
                            int tokensLeftIdx = (start + oc.get().prefix().length);
                            int tokensRightIdx = (i - oc.get().postfix().length);
                            Expression subexpression = this.createExpression(cs, tokensLeftIdx, tokensRightIdx,
                                oc.get(), shouldParseSubexpression);
                            expression.addChild(subexpression);
                            if (shouldParseSubexpression) {
                                this.expandExpression(subexpression);
                            }
                            break;
                        }
                    } else {
                        i++;
                    }
                }
                start = i;
            } else {
                i++;
            }
        }
        boolean hasExpressionAfter = (cs.length - start > 0);
        if (hasExpressionAfter) {
            Expression subexpression = this.createExpression(cs, start, cs.length, noOpsOperatorCode, false);
            expression.addChild(subexpression);
        }
    }

    private Optional<ExpressionOperatorCode> matchPrefix(char[] cs, int idx) {
        for (int j = 0; j < ops.length; j++) {
            if (ops[j].match(cs, idx, ops[j].prefix())) {
                return Optional.of(ops[j]);
            }
        }
        return Optional.empty();
    }

    private boolean matchPrefix(char[] cs, int idx, ExpressionOperatorCode oc) {
        return oc.match(cs, idx, oc.prefix());
    }

    private boolean matchPostfix(char[] cs, int idx, ExpressionOperatorCode oc) {
        return oc.match(cs, idx, oc.postfix());
    }

    private Expression createExpression(char[] cs, int start, int end, ExpressionOperatorCode oc,
        boolean shouldParseSubexpression) {
        char[] dest = new char[end - start];
        System.arraycopy(cs, start, dest, 0, dest.length);
        return Expression.build(String.copyValueOf(dest), oc, shouldParseSubexpression, errorReporting);
    }
}
