package com.coderknock.codegen.tool.domin;

public class Result<T> {
    private T data;
    private int code;
    private String message;

    Result(T data, int code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder().data(data).build();
    }

    public static <T> Result<T> success() {
        return Result.<T>builder().build();
    }

    public static <T> Result<T> fail(int code, String message) {
        return Result.<T>builder().code(code).message(message).build();
    }

    public static <T> ResultBuilder<T> builder() {
        return new ResultBuilder<T>();
    }

    public boolean isSuccess() {
        return code == 0;
    }

    public T getData() {
        return this.data;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Result)) return false;
        final Result<?> other = (Result<?>) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        if (this.getCode() != other.getCode()) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Result;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        result = result * PRIME + this.getCode();
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

    public String toString() {
        return "Result(data=" + this.getData() + ", code=" + this.getCode() + ", message=" + this.getMessage() + ")";
    }

    public static class ResultBuilder<T> {
        private T data;
        private int code;
        private String message;

        ResultBuilder() {
        }

        public ResultBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ResultBuilder<T> code(int code) {
            this.code = code;
            return this;
        }

        public ResultBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Result<T> build() {
            return new Result<T>(this.data, this.code, this.message);
        }

        public String toString() {
            return "Result.ResultBuilder(data=" + this.data + ", code=" + this.code + ", message=" + this.message + ")";
        }
    }
}
