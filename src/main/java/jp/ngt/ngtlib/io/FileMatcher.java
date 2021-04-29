package jp.ngt.ngtlib.io;

import java.io.File;

@FunctionalInterface
public interface FileMatcher {
    boolean match(File file);
}