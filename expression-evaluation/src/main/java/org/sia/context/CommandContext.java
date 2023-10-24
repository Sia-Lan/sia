package com.xiaomi.tsm.tp.ctx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xiaomi.tsm.tp.bo.ResponseAPDU;
import com.xiaomi.tsm.tp.command.CommandAPDU;
import com.xiaomi.tsm.tp.command.CommandBridge;
import com.xiaomi.tsm.tp.command.CommandIdentifier;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @summary:
 *
 * @create: 2020-04-29
 */
@Data
@JsonIgnoreProperties(
    value = {Context.JSON_IGNORE_FLUSH_TO_DB, Context.JSON_IGNORE_EXCEPTION, Context.JSON_IGNORE_CONTEXT_CONFIG})
@EqualsAndHashCode(callSuper = false)
public class CommandContext extends Context {

    private CommandAPDU commandAPDU;

    private ResponseAPDU responseAPDU;

    private String rawAPDU;

    private CommandIdentifier cid;

    private String dataKey;

    private String propertiesPrefix;

    private CommandBridge commandBridge;

    private Integer idx;

    public static CommandContext create(Context ctx) {
        CommandContext commandContext = new CommandContext();
        commandContext.copy(ctx);
        return commandContext;
    }

    @Override
    public String propertiesPrefix() {
        return propertiesPrefix;
    }
}
