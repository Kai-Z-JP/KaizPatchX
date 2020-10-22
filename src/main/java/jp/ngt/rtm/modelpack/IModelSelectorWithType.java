package jp.ngt.rtm.modelpack;

public interface IModelSelectorWithType extends IModelSelector {
	/**
	 * 同一タイプのもののみ選択画面に表示する
	 */
	String getSubType();
}