package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

import org.springframework.stereotype.Component;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @summary:
 *
 * @create: 2020-02-14
 */
@Component
public class ExpressionNoOpsOperatorCode implements ExpressionOperatorCode {

    @Override
    public String toString() {
        return "no-ops";
    }
}
