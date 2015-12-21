package DSLParser;

import com.alibaba.fastjson.*;

public class DslParser {
	public static RefactorConfig parse(String config){
		return JSON.parseObject(config, RefactorConfig.class);
	}
	
	public static String jsonify(RefactorConfig config){
		return JSON.toJSONString(config);
	}
}
