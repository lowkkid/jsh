package com.github.lowkkid.parser;

public class RedirectOptions {

    private final String redirectTo;
    private final RedirectType redirectType;
    private final RedirectStream redirectStream;

    public RedirectOptions(String redirectTo, RedirectType redirectType, RedirectStream redirectStream) {
        this.redirectTo = redirectTo;
        this.redirectType = redirectType;
        this.redirectStream = redirectStream;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public boolean isRedirectingStdOut() {
        return redirectStream == RedirectOptions.RedirectStream.STDOUT;
    }

    public boolean isRedirectingStdErr() {
        return redirectStream == RedirectOptions.RedirectStream.STDERR;
    }

    public boolean isAppending() {
        return redirectType == RedirectOptions.RedirectType.APPEND;
    }

    public boolean isRewriting() {
        return redirectType == RedirectType.REWRITE;
    }

    public RedirectType getRedirectType() {
        return redirectType;
    }

    public RedirectStream getRedirectStream() {
        return redirectStream;
    }

    public enum RedirectType {
        REWRITE, APPEND
    }

    public enum RedirectStream {
        STDOUT, STDERR
    }
}


