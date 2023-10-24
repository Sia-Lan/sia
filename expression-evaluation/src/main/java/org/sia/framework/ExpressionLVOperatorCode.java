package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

import org.springframework.stereotype.Component;

import com.xiaomi.tsm.tp.codec.Number;
import com.xiaomi.tsm.tp.ctx.CommandContext;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @create: 2019-12-18
 */
@Component
@Slf4j
public class ExpressionLVOperatorCode implements ExpressionOperatorCode {

    @Override
    public char[] prefix() {
        return new char[] {'#', '('};
    }

    @Override
    public char[] postfix() {
        return new char[] {')'};
    }

    @Override
    public String compute(Operand operand, CommandContext ctx) {
        String data = operand.getValue();
        String lc = Number.Hex.toHexString(data.length() / 2);
        return lc.concat(data);
    }

    @Override
    public String toString() {
        return "#";
    }
}
