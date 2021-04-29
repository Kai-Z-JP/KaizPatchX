package jp.ngt.ngtlib.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ファイルのスキャン結果をZipごとに管理
 */
public class ScanResult {
    private final Map<String, MatchResult> result = new HashMap<>();

    private MatchResult getMatchResult(String key) {
        if (!this.result.containsKey(key)) {
            this.result.put(key, new MatchResult());
        }
        return this.result.get(key);
    }

    public List<File> asList() {
        List<File> list = new ArrayList<>();
        this.result.values().stream().map(MatchResult::asList).forEach(list::addAll);
        return list;
    }

    public void add(String key1, FileMatcher key2, File file) {
        this.getMatchResult(key1).add(key2, file);
    }

    public static class MatchResult {
        private final Map<FileMatcher, List<File>> result = new HashMap<>();

        private List<File> getList(FileMatcher key) {
            if (!this.result.containsKey(key)) {
                this.result.put(key, new ArrayList<>());
            }
            return this.result.get(key);
        }

        public List<File> asList() {
            List<File> list = new ArrayList<>();
            this.result.values().forEach(list::addAll);
            return list;
        }

        public void add(FileMatcher key, File file) {
            this.getList(key).add(file);
            NGTFileLoader.log("[NGTFL] Add file : %s (%d)", file.getName(), this.getList(key).size());
        }
    }
}
