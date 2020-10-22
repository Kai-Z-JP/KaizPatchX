package jp.ngt.mcte.editor.filter;

public abstract class CfgParameter<T> {
	private T value;

	public T getValue() {
		return value;
	}

	public void setValue(T par) {
		this.value = par;
	}

	public abstract void setRawValue(String par);

	public abstract ParameterType getType();

	public static class CfgParameterInt extends CfgParameter<Integer> {
		public final int min, max;

		public CfgParameterInt(int value, int min, int max) {
			this.min = min;
			this.max = max;
			this.setValue(value);
		}

		@Override
		public void setValue(Integer par) {
			if (par >= this.min && par <= this.max) {
				super.setValue(par);
			}
		}

		@Override
		public void setRawValue(String par) {
			this.setValue(Integer.valueOf(par));
		}

		@Override
		public ParameterType getType() {
			return ParameterType.INTEGER;
		}
	}

	public static class CfgParameterFloat extends CfgParameter<Float> {
		public final float min, max;

		public CfgParameterFloat(float value, float min, float max) {
			this.min = min;
			this.max = max;
			this.setValue(value);
		}

		@Override
		public void setValue(Float par) {
			if (par >= this.min && par <= this.max) {
				super.setValue(par);
			}
		}

		@Override
		public void setRawValue(String par) {
			this.setValue(Float.valueOf(par));
		}

		@Override
		public ParameterType getType() {
			return ParameterType.FLOAT;
		}
	}

	public static class CfgParameterBoolean extends CfgParameter<Boolean> {
		public CfgParameterBoolean(boolean value) {
			this.setValue(value);
		}

		@Override
		public void setRawValue(String par) {
			this.setValue(Boolean.valueOf(par));
		}

		@Override
		public ParameterType getType() {
			return ParameterType.BOOLEAN;
		}
	}

	public static class CfgParameterString extends CfgParameter<String> {
		public final String[] paramList;

		public CfgParameterString(String value, String[] list) {
			this.setValue(value);
			this.paramList = list;
		}

		@Override
		public void setRawValue(String par) {
			this.setValue(par);
		}

		@Override
		public ParameterType getType() {
			return (this.paramList.length > 0) ? ParameterType.STRING_LIST : ParameterType.STRING;
		}
	}

	public enum ParameterType {
		INTEGER,
		FLOAT,
		BOOLEAN,
		STRING,
		STRING_LIST;
	}
}
