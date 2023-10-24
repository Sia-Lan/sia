package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @create: 2019-12-18
 */

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.xiaomi.tsm.tp.constant.Constant;
import com.xiaomi.tsm.tp.ctx.CommandContext;
import com.xiaomi.tsm.tp.exception.AbstractErrorReporting;
import com.xiaomi.tsm.tp.properties.TaskPropertiesFactory;

/**
 * A variable reference starts with a '$' character and the variable body is enclosed in curly braces. The variable body
 * inside the curly braces is subject to variable expansion.
 *
 * @author it_lanxy
 */
@Component
public class ExpressionLinkedOperatorCode implements ExpressionOperatorCode {

    private final TaskPropertiesFactory.Special special;

    private final AbstractErrorReporting errorReporting;

    public ExpressionLinkedOperatorCode(TaskPropertiesFactory propertiesFactory) {
        this.special = propertiesFactory.fetchSpecial.get();
        this.errorReporting = AbstractErrorReporting.create(Constant.EXPRESSION_OPERATOR_CODE_LINKED);
    }

    @Override
    public char[] prefix() {
        return new char[] {'$', '{'};
    }

    @Override
    public char[] postfix() {
        return new char[] {'}'};
    }

    @Override
    public String compute(Operand operand, CommandContext ctx) {
        // 链接：符号引用转换为直接引用
        String propertyKey = operand.getValue();
        String value = special.fetchProperty.by(ctx, propertyKey);
        if (StringUtils.isEmpty(value)) {
            errorReporting.expressionMissProperty.by(ctx, propertyKey);
            return propertyKey;
        } else {
            return value;
        }
    }

    @Override
    public String toString() {
        return "$";
    }
}
