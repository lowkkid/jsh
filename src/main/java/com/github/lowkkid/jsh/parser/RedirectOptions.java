package com.github.lowkkid.jsh.parser;

public record RedirectOptions(String redirectTo, RedirectType redirectType, RedirectStream redirectStream) {

    public boolean isRedirectingStdOut() {
        return redirectStream == RedirectStream.STDOUT;
    }

    public boolean isRedirectingStdErr() {
        return redirectStream == RedirectStream.STDERR;
    }

    public boolean isAppending() {
        return redirectType == RedirectType.APPEND;
    }

    public boolean isRewriting() {
        return redirectType == RedirectType.REWRITE;
    }

    public enum RedirectType {
        REWRITE, APPEND
    }

    public enum RedirectStream {
        STDOUT, STDERR
    }
}


