package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

import com.xiaomi.tsm.tp.ctx.CommandContext;

/**
 * @author it_lanxy
 */
public interface ExpressionOperatorCode {
    /**
     * 操作码前缀
     *
     * @return
     */
    default char[] prefix() {
        return new char[0];
    }

    /**
     * 操作码后缀
     *
     * @return
     */
    default char[] postfix() {
        return new char[0];
    }

    /**
     * 操作码对操作数的计算规则
     *
     * @param operand
     * @param ctx
     * @return
     */
    default String compute(Operand operand, CommandContext ctx) {
        return operand.getValue();
    }

    /**
     * 匹配规则
     *
     * @param c
     * @param idx
     * @param fix
     * @return
     */
    default boolean match(char[] c, int idx, char[] fix) {
        for (int i = 0; i < fix.length; i++) {
            if (c[idx + i] != fix[i]) {
                return false;
            }
        }
        return true;
    }
}
