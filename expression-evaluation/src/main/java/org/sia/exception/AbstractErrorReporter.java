package com.xiaomi.tsm.tp.exception;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;

import com.xiaomi.tsm.seconfig.bo.Scp;
import com.xiaomi.tsm.tp.ctx.Context;
import com.xiaomi.tsm.tp.ctx.TaskContext;
import com.xiaomi.tsm.tp.function.BiFactory;
import com.xiaomi.tsm.tp.function.Factory;

import at.favre.lib.bytes.Bytes;

/**
 * @author: lanxinyu@xiaomi.com
 *
 * @summary:
 *
 * @create: 2020-07-08
 */
public abstract class AbstractErrorReporting implements ErrorReporting {

    public static final String ERROR_MSG_INVALID_REQUEST = "Invalid request";

    public static final String ERROR_MSG_PROPERTY_VALUE_NOT_FOUND = "Property value not found";

    public static AbstractErrorReporting create(int code) {
        return new AbstractErrorReporting() {
            @Override
            public int code() {
                return code;
            }
        };
    }

    public Factory<Context, String> verifyCardCryptogramFailed = ctx -> {
        String reasonPhrase = "Verify card cryptogram failed";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(reasonPhrase));
        return reasonPhrase;
    };

    public Factory<Context, String> genSessionKeysFailed = ctx -> {
        String reasonPhrase = "Generate session keys failed";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(reasonPhrase));
        return reasonPhrase;
    };

    public BiFactory<Context, String, String> keyIsEmpty = (ctx, key) -> {
        String reasonPhrase = "Generate key failed";
        String keyWord = "Result key is empty: " + key;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.INTERNAL_SERVER_ERROR,
            new InternalServerErrorException(keyWord));
        return reasonPhrase;
    };

    public BiFactory<Context, String, String> beanNotFoundByKeyRule = (ctx, key) -> {
        String reasonPhrase = "Bean not found by key rule";
        String keyWord = "Key rule is: " + key;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(keyWord));
        return reasonPhrase;
    };

    public BiFactory<Context, String, String> beanNotFoundByManufacturer = (ctx, key) -> {
        String reasonPhrase = "Bean not found by manufacturer";
        String keyWord = "Manufacturer is: " + key;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(keyWord));
        return reasonPhrase;
    };

    public BiFactory<Context, Scp, String> beanNotFoundByScp = (ctx, scp) -> {
        String reasonPhrase = "Bean not found by scp";
        String keyWord = "Bean not found by scp: " + scp.name();
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(keyWord));
        return reasonPhrase;
    };

    public BiConsumer<Context, String> genKeyFailedMethodNotExtended = (ctx, lambda) -> {
        String reasonPhrase = "Generate key failed";
        String keyWord = "Method not extended: " + lambda;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_EXTENDED, new NotExtendedException(keyWord));
    };

    public Factory<Context, String> expressionIsEmpty = ctx -> {
        String reasonPhrase = "Expression must not be null";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(reasonPhrase));
        return reasonPhrase;
    };

    public Factory<Context, String> expressionIsInvalid = ctx -> {
        String reasonPhrase = "Expression is invalid";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(reasonPhrase));
        return reasonPhrase;
    };

    public BiFactory<Context, String, String> expressionMissProperty = (ctx, propertyKey) -> {
        String reasonPhrase = ERROR_MSG_PROPERTY_VALUE_NOT_FOUND;
        String keyWord = "Property key: " + propertyKey;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.BAD_REQUEST,
            new ConfigurationException(keyWord));
        return reasonPhrase;
    };

    public BiFactory<Context, String, String> seShallPutKeyMetadataIsEmpty = (ctx, aid) -> {
        String reasonPhrase = "Key not found";
        String keyWord = "SE shall put key metadata not found. aid: " + aid;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(keyWord));
        return keyWord;
    };

    public BiFactory<Context, String, String> sePutKeyMetadataIsEmpty = (ctx, aid) -> {
        String reasonPhrase = "Key not found";
        String keyWord = "SE put key metadata not found. aid: " + aid;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(keyWord));
        return reasonPhrase;
    };

    public BiFactory<Context, String, String> invalidKeyTypeCoding = (ctx, keyTypeCoding) -> {
        String reasonPhrase = "Invalid key type coding";
        String keyWord = "Please try '" + keyTypeCoding + "'";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.PRECONDITION_REQUIRED,
            new ConfigurationException(keyWord));
        return reasonPhrase;
    };

    public Consumer<Context> invalidKeyLength = ctx -> {
        String reasonPhrase = "Invalid key length";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ConfigurationException(reasonPhrase));
    };

    public BiFactory<Context, String, String> beanNotFoundByKeyTypeCoding = (ctx, keyTypeCoding) -> {
        String reasonPhrase = "Bean not found by key type coding";
        String keyWord = "Please try '" + keyTypeCoding + "'";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.PRECONDITION_REQUIRED,
            new ConfigurationException(keyWord));
        return reasonPhrase;
    };

    public BiFactory<Context, String, String> specialKeyNotSupport = (ctx, specialKey) -> {
        String reasonPhrase = "Special key not support";
        String keyWord = "Special key is: " + specialKey;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_IMPLEMENTED,
            new SwitchBranchException(keyWord));
        return reasonPhrase;
    };

    public BiConsumer<Context, String> badRequest =
        (ctx, reasonPhase) -> this.exceptionCaught(ctx, reasonPhase, HttpStatus.BAD_REQUEST,
            new IllegalArgumentException(reasonPhase));

    public Consumer<Context> forbidden =
        ctx -> {
            String reasonPhase = "Access denied";
            String keyWord = "Request is not eligible";
            this.exceptionCaught(ctx, reasonPhase, HttpStatus.FORBIDDEN,
                new IllegalArgumentException(keyWord));
        };

    public BiConsumer<Context, TaskContext.Phase> taskPhaseNotSupport =
        (ctx, taskPhase) -> {
            String reasonPhase = "Unknown task phase";
            String keyWord = "Task phase not support: " + taskPhase.name();
            this.exceptionCaught(ctx, reasonPhase, HttpStatus.BAD_REQUEST,
                new IllegalArgumentException(keyWord));
        };

    public Consumer<Context> tenantTaskConfigNotFound = ctx -> {
        String reasonPhrase = "Tenant task config not found";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_FOUND,
            new ConfigurationException(reasonPhrase));
    };

    public Consumer<Context> shouldNotReachHere = ctx -> {
        String reasonPhrase = "Shouldn't reach here";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_IMPLEMENTED,
            new SwitchBranchException(reasonPhrase));
    };

    public Consumer<Context> invalidProcessCommandAPDU = ctx -> {
        String reasonPhrase = "Invalid procedure context";
        String keyWord = "Command APDU list must not be empty";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_EXTENDED, new NotExtendedException(keyWord));
    };

    public BiConsumer<Context, String> invalidRawAPDU = (ctx, cause) -> {
        String reasonPhrase = "Invalid raw apdu";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_EXTENDED,
            new NotExtendedException(cause));
    };

    public Consumer<Context> invalidResponseAPDU = ctx -> {
        String reasonPhrase = "Invalid process response";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(reasonPhrase));
    };

    public BiConsumer<Context, String> invalidStatusWord = (ctx, statusWord) -> {
        String reasonPhrase = "Invalid status word";
        String keyWord = "Status word is: " + statusWord;
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(keyWord));
    };

    public Consumer<Context> invalidSeMetadata = ctx -> {
        String reasonPhrase = "Invalid se metadata";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.INTERNAL_SERVER_ERROR,
            new InternalServerErrorException(reasonPhrase));
    };

    public BiConsumer<Context, Throwable> runtimeError = (ctx, throwable) -> {
        String reasonPhrase = "Runtime error";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    };

    public Consumer<Context> genStaticKeyFailed = ctx -> {
        String reasonPhrase = "Generate static key failed";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.EXPECTATION_FAILED,
            new ExpectationFailedException(reasonPhrase));
    };

    public Factory<Context, String> unsupportedMorePutKeyCommands = ctx -> {
        String reasonPhrase = "Unsupported 'More PUT KEY commands' mode";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_EXTENDED,
            new NotExtendedException(reasonPhrase));
        return reasonPhrase;
    };

    public Consumer<Context> commandBridgeSymbolResolveFailed = ctx -> {
        String reasonPhrase = "Command bridge symbol resolve failed";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.INTERNAL_SERVER_ERROR,
            new InternalServerErrorException(reasonPhrase));
    };

    public BiConsumer<Context, String> commandBridgeSymbolMismatch = (ctx, operand) -> {
        String reasonPhrase = "Command bridge symbol mismatch";
        String keyWord = "Operand : ".concat(operand);
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_EXTENDED, new NotExtendedException(keyWord));
    };

    public BiConsumer<Context, String> commandBridgeConfigMismatch = (ctx, key) -> {
        String reasonPhrase = "Command bridge config mismatch";
        String keyWord = "Required to fill in: ".concat(key);
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_EXTENDED, new ConfigurationException(keyWord));
    };

    public Consumer<Context> secureDomainKeyMetadataIsEmpty = ctx -> {
        String reasonPhrase = "Fetch secure domain key metadata failed";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.INTERNAL_SERVER_ERROR,
            new InternalServerErrorException(reasonPhrase));
    };

    public Factory<Context, Bytes> nonsupportKeyStrategy = ctx -> {
        String reasonPhrase = "Nonsupport Key strategy";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.NOT_EXTENDED,
            new NotExtendedException(reasonPhrase));
        return Bytes.empty();
    };

    public Consumer<Context> trap = ctx -> {
        String reasonPhrase = "Trap";
        this.exceptionCaught(ctx, reasonPhrase, HttpStatus.OK, new TrapException());
    };
}
