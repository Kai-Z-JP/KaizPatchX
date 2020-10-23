package jp.ngt.rtm.modelpack.state;

/**
 * 1.12から移植、ダミー
 */
public final class DataFormatter {
	public DataFormatter() {
	}

	public void initDataMap(DataMap dm) {
		dm.setFormatter(this);
	}

	public boolean check(String key, DataEntry value) {
		return true;
	}

	public IDataFilter getFilter(String key) {
		return null;
	}

	public String[] getSuggestions(String key) {
		return null;
	}
}
