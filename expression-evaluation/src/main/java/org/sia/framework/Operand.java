package com.xiaomi.tsm.tp.handler.command.symbol.resolution;

import lombok.Data;
import lombok.Getter;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @create: 2019-12-18
 */
@Data
public class Operand {
    @Getter
    private String value;

    private Operand() {}

    public static Operand build(String tokens) {
        Operand operand = new Operand();
        operand.value = tokens;
        return operand;
    }
}
