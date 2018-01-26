package ru.sbrf.wallet.command;

import com.mastercard.mcbp.bhapi.BaseResponse;

public abstract class CommandStatus<T> {

    private final T data;

    protected CommandStatus(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public static class OK<T> extends CommandStatus<T> {
        public OK(T result) {
            super(result);
        }

        public OK() {
            super(null);
        }
    }

    public static class ERROR<T> extends CommandStatus<T> {
        public ERROR(T error) {
            super(error);
        }
    }

    public static class NETWORK_ERROR<Exception> extends CommandStatus.ERROR<Exception> {
        public NETWORK_ERROR(Exception error) {
            super(error);
        }
    }

    public static class BH_ERROR<E extends BaseResponse> extends CommandStatus.ERROR<E> {
        public BH_ERROR(E error) {
            super(error);
        }

        public int getErrorCode() {
            return getData().getResultCode();
        }
    }

    public static class CANCEL<T> extends CommandStatus<Void> {
        public CANCEL() {
            super(null);
        }
    }

    public static class SESSION_FAIL<E> extends CommandStatus.ERROR<E> {
        public SESSION_FAIL(E data) {
            super(data);
        }
    }

    public static class PARSING_RESPONSE_ERROR<ClassCastException> extends CommandStatus.ERROR<ClassCastException> {
        public PARSING_RESPONSE_ERROR(ClassCastException error) {
            super(error);
        }
    }

    public static class END_OF_TASKS<T> extends CommandStatus.OK<T> {
        public END_OF_TASKS(T result) {
            super(result);
        }

        public END_OF_TASKS() {
            super(null);
        }
    }

    public static class TASK_CANCELLED<T> extends CommandStatus.OK<T> {
        public TASK_CANCELLED(T result) {
            super(result);
        }

        public TASK_CANCELLED() {
            super(null);
        }
    }

    public boolean isOK() {
        return this instanceof CommandStatus.OK;
    }

    public boolean isError() {
        return this instanceof CommandStatus.ERROR;
    }

    @Override
    public String toString() {
        return "class = " + this.getClass().getSimpleName() + " data= " + String.valueOf(getData());
    }
}
