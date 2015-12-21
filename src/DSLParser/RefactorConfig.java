package DSLParser;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * { "variableNameStyle": { "capital": false, "underline": false },
 * "permitFunctionSimilarity": 0.4, "permitDeadcode": false,
 * "permitMagicNumber": { "integer": [0,1], "float": [10.24] } } 
 * <br>===<br>
 * ["sampleVariable", "sample_variable", "SampleVariable", "Sample_Variable"]
 * */
public class RefactorConfig {
	private VariableNameStyle variableNameStyle; // 变量命名风格
	private float permitFunctionSimilarity; // 允许的函数相似阈值
	private boolean permitDeadcode; // 是否允许保留死代码
	private PermitMagicNumber permitMagicNumber; // 允许出现哪些 magic number
	
	
	public List<Integer> getPermitIntegerList(){
		return permitMagicNumber.getIntegers();
	}
	public List<Double> getPermitFloatList(){
		return permitMagicNumber.getFloats();
	}
	/**@return 0~3 for ["sampleVariable", "sample_variable", "SampleVariable", "Sample_Variable"]
	 * <br>!c!u=0; !cu=1; c!u=2; cu=3. Beautiful!
	 * */
	public int getVariableNameStyleIndex(){
		return 2 * (variableNameStyle.capital ? 1 : 0) +
			   1 * (variableNameStyle.underline ? 1 : 0);
	}
	
	/////////
	

	public VariableNameStyle getVariableNameStyle() {
		return variableNameStyle;
	}

	public void setVariableNameStyle(VariableNameStyle variableNameStyle) {
		this.variableNameStyle = variableNameStyle;
	}

	public float getPermitFunctionSimilarity() {
		return permitFunctionSimilarity;
	}

	public void setPermitFunctionSimilarity(float permitFunctionSimilarity) {
		this.permitFunctionSimilarity = permitFunctionSimilarity;
	}

	public boolean isPermitDeadcode() {
		return permitDeadcode;
	}

	public void setPermitDeadcode(boolean permitDeadcode) {
		this.permitDeadcode = permitDeadcode;
	}

	public PermitMagicNumber getPermitMagicNumber() {
		return permitMagicNumber;
	}

	public void setPermitMagicNumber(PermitMagicNumber permitMagicNumber) {
		this.permitMagicNumber = permitMagicNumber;
	}

	@Override
	public String toString() {
		return DslParser.jsonify(this); // 233 耦合了. 测试用吧
	}

	class VariableNameStyle {
		private boolean capital;
		private boolean underline;

		public boolean isCapital() {
			return capital;
		}

		public void setCapital(boolean capital) {
			this.capital = capital;
		}

		public boolean isUnderline() {
			return underline;
		}

		public void setUnderline(boolean underline) {
			this.underline = underline;
		}
	}

	class PermitMagicNumber {
		private List<Integer> integers = new ArrayList<>();
		private List<Double> floats = new ArrayList<>();

		@JSONField(name="integer")
		public List<Integer> getIntegers() {
			return integers;
		}
		@JSONField(name="integer")
		public void setIntegers(List<Integer> integers) {
			this.integers = integers;
		}
		@JSONField(name="float")
		public List<Double> getFloats() {
			return floats;
		}
		@JSONField(name="float")
		public void setFloats(List<Double> floats) {
			this.floats = floats;
		}
	}
}
