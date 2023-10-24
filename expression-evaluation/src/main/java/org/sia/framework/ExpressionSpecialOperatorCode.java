package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.xiaomi.tsm.tp.command.CommandIdentifier;
import com.xiaomi.tsm.tp.constant.Constant;
import com.xiaomi.tsm.tp.ctx.CommandContext;
import com.xiaomi.tsm.tp.exception.AbstractErrorReporting;
import com.xiaomi.tsm.tp.factory.BeanFactory;
import com.xiaomi.tsm.tp.handler.command.symbol.resolution.command.SpecialOperatorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @summary:
 *
 * @create: 2020-04-28
 */
@Component
@Slf4j
public class ExpressionSpecialOperatorCode implements ExpressionOperatorCode {

    private final BeanFactory beanFactory;

    private final AbstractErrorReporting errorReporting;

    public ExpressionSpecialOperatorCode(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.errorReporting = AbstractErrorReporting.create(Constant.EXPRESSION_OPERATOR_CODE_COMMAND);
    }

    @Override
    public char[] prefix() {
        return new char[] {'*', '{'};
    }

    @Override
    public char[] postfix() {
        return new char[] {'}'};
    }

    @Override
    public String compute(Operand operand, CommandContext ctx) {
        CommandIdentifier cid = ctx.getCid();
        Optional<SpecialOperatorCode> specialOperatorCode =
            beanFactory.fetchSpecialOperatorCode(cid);
        String specialKey = operand.getValue();
        if (specialOperatorCode.isPresent()) {
            SpecialOperatorCode special = specialOperatorCode.get();
            if (special.isSupport(specialKey)) {
                return special.compute(specialKey, ctx);
            }
        }
        return errorReporting.specialKeyNotSupport.by(ctx, specialKey);
    }
}
