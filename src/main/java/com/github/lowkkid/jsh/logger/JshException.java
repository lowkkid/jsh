//package com.github.lowkkid.jsh.exception;
//
//public abstract class JshException extends RuntimeException {
//
//    public JshException(String message) {
//        super(message);
//    }
//
//    @Override
//    public String getMessage() {
//        return type().name() + ": " + super.getMessage();
//    }
//
//    public abstract Type type();
//
//    public enum Type {
//        ERROR,
//        WARNING,
//    }
//}
