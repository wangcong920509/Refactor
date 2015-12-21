package refactor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NumberLiteral;
public class DemoVisitor2 extends ASTVisitor {
	private int intsCnt = 0;
	private int doubCnt = 0;
	private String toInsertInt = "";
	private String toInsertDouble = "";
	private String result;
	private List<Integer> emitInts;
	private List<Double> emitDoubs;
	private List<Integer> forbdInts;
	private List<Double> forbdDoubs;
	public DemoVisitor2() {
		emitInts = new ArrayList<>();
		emitDoubs = new ArrayList<>();
		forbdInts = new ArrayList<>();
		forbdDoubs = new ArrayList<>();	
		initSet(emitInts, emitDoubs, forbdInts, forbdDoubs);
	}
	private static void initSet(List<Integer> emitInts, List<Double> emitDoubs, List<Integer> forbdInts,
			List<Double> forbdDoubs) {
		Integer[] initInt = {-1,0,1};
		Double[] initDoub = {-1.0,0.0,1.0};
		emitInts.addAll(Arrays.asList(initInt));
		emitDoubs.addAll(Arrays.asList(initDoub));
	}
	DemoVisitor2(List<Integer> emitIntList, List<Double> emitDoubList, List<Integer> forbdIntList,
			List<Double> forbdDoubList) {
		emitInts = new ArrayList<>();
		emitDoubs = new ArrayList<>();
		forbdInts = new ArrayList<>();
		forbdDoubs = new ArrayList<>();
		emitInts.addAll(emitIntList);
		emitDoubs.addAll(emitDoubList);
		forbdInts.addAll(forbdIntList);
		forbdDoubs.addAll(forbdDoubList);
	}
	public String getResult() {
		return result;
	}
	private boolean isDouble(String s) {
		return s.indexOf(".") != -1;
	}
	@Override
	public boolean visit(NumberLiteral node) {
		if (isDouble(node.toString())) {
			double value = Double.parseDouble(node.getToken());
			if (!emitDoubs.contains(value) || forbdDoubs.contains(value)) {
				toInsertDouble += String.format("  private static final double MAGIC_DOUBLE_%d = " + node + ";\n",
						doubCnt);
				doubCnt++;
			}
		} else {
			int value = Integer.parseInt(node.getToken());
			if (!emitInts.contains(value) || forbdInts.contains(value)) {
				toInsertInt += String.format("  private static final int MAGIC_INT_%d = " + node + ";\n", intsCnt);
				intsCnt++;
			}
		}
		return true;
	}
	@Override
	public void endVisit(CompilationUnit node) {
		String allCode = node.toString();
		result = allCode.replaceFirst("\\{", "{\n" +toInsertInt + "\n" + toInsertDouble);
	}
	public void changeEmitInts(List<Integer> list){
		emitInts = new ArrayList<Integer>();
		emitInts.addAll(list);
	}
	public void changeEmitDoubs(List<Double> list){
		emitDoubs = new ArrayList<Double>();
		emitDoubs.addAll(list);
	}
}