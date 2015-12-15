package refactor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
public class RefactorFunc extends ASTVisitor{
	/*
	 * 0 : sampleVariable
	 * 1 : sample_variable
	 * 2 : SampleVariable
	 * 3 : Sample_Variable
	 */
	private int variableStyle = 0;
	private float threshold = 1;
	private String currType = "";
	private HashMap<String, List<MethodDeclaration>> methods = new HashMap<String, List<MethodDeclaration>>();
	private List<String> oldStrings = new ArrayList<String>();
	private List<String> newStrings = new ArrayList<String>();
	@Override
	public boolean visit(FieldDeclaration node){
		for (Object obj : node.fragments()){
			VariableDeclarationFragment v = (VariableDeclarationFragment)obj;
			SimpleName sn = v.getName();
			String newIden = sn.toString();
			oldStrings.add(newIden);
			char oldFirstChar = newIden.charAt(0);
			if(variableStyle < 2 && oldFirstChar >= 'A' && oldFirstChar <= 'Z')
				newIden = newIden.replace(oldFirstChar, Character.toLowerCase(oldFirstChar));
			else if(variableStyle >= 2 && oldFirstChar >= 'a' && oldFirstChar <= 'z')
				newIden = newIden.replace(oldFirstChar, Character.toUpperCase(oldFirstChar));
			int flag = 0;
			if(variableStyle % 2 == 0){
				for(int i = 1; i < newIden.length(); i++){
					char oldChar = newIden.charAt(i);
					if(oldChar == '_'){
						flag = 1;
						newIden = newIden.substring(0, i) + newIden.substring(i + 1);
						i--;
						continue;
					}
					if(flag == 1){
						char newChar = Character.toUpperCase(oldChar);
						newIden = newIden.replace(oldChar, newChar);
						flag = 0;
					}
				}
			}
			else if(variableStyle == 1){
				for(int i = 1; i < newIden.length(); i++){
					char oldChar = newIden.charAt(i);
					if(oldChar >= 'A' && oldChar <= 'Z'){
						char newChar = Character.toLowerCase(oldChar);
						if(newIden.charAt(i - 1) == '_')
							newIden = newIden.replace(oldChar, newChar);
						else{
							newIden = newIden.substring(0, i) + "_" + newChar + newIden.substring(i + 1);
							i++;
						}
					}
				}
			}
			else{
				for(int i = 1; i < newIden.length(); i++){
					char oldChar = newIden.charAt(i);
					if(oldChar >= 'A' && oldChar <= 'Z' && newIden.charAt(i - 1) != '_'){
						newIden = newIden.substring(0, i) + "_" + newIden.substring(i);
						i++;
					}
					else if(newIden.charAt(i - 1) == '_' && oldChar >= 'a' && oldChar <= 'z'){
						char newChar = Character.toUpperCase(oldChar);
						newIden = newIden.replace(oldChar, newChar);
					}
				}
			}
			sn.setIdentifier(newIden);
			newStrings.add(newIden);
		}
		return true;
	}
	public void setVariableStyle(int flag){
		variableStyle = flag;
	}
	public void setThreshold(float thr){
		threshold = thr;
	}
	@Override
	public boolean visit(SimpleName node){
		String snString = node.toString();
		for(int i = 0; i < oldStrings.size(); i++)
			if(oldStrings.get(i).compareTo(snString) == 0){
				node.setIdentifier(newStrings.get(i));
				break;
			}
		return true;
	}
	@Override
	public boolean visit(TypeDeclaration node){
		currType = node.getName().getIdentifier();
		return true;
	}
	@Override
	public boolean visit(MethodDeclaration node){
		if(methods.get(currType) == null)
			methods.put(currType, new ArrayList<MethodDeclaration>());
		methods.get(currType).add(node);
		return true;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void endVisit(TypeDeclaration node){
		if(methods.get(currType) == null)
			return;
		int len = methods.get(currType).size();
		float[][] similarity = new float[len][len];
		for(int i = 0; i < len - 1; i++)
			for(int j = i + 1; j < len; j++)
				similarity[i][j] = 0;
		for(int i = 0; i < len - 1; i++)
			for(int j = i + 1; j < len; j++){
				similarity[i][j] = getSimi(methods.get(currType).get(i), methods.get(currType).get(j));
			    System.out.println("Similarity between Method " + i + " and Method " + j + " is " + similarity[i][j] + ".");
			}
		for(int i = 0; i < len - 1; i++)
			for(int j = i + 1; j < len; j++)
				if(similarity[i][j] >= threshold){
					MethodDeclaration ast1 = methods.get(currType).get(i);
					MethodDeclaration ast2 = methods.get(currType).get(j);
					AST ast = node.getAST();
					MethodDeclaration md = ast.newMethodDeclaration();
					//set the name
					SimpleName nameNewMethod = ast.newSimpleName(ast1.getName().getIdentifier() + "_" + ast2.getName().getIdentifier());
					md.setName(nameNewMethod);
					node.bodyDeclarations().add(md);
					//set the parameters
					List<SingleVariableDeclaration> svd1 = ast1.parameters();
					List<SingleVariableDeclaration> svd2 = ast2.parameters();
					for(int k = 0; k < svd1.size(); k++){
						SingleVariableDeclaration variableDeclaration = svd1.get(k);
						md.parameters().add(ASTNode.copySubtree(ast, variableDeclaration));
					}
					for(int k = 0; k < svd2.size(); k++){
						SingleVariableDeclaration variableDeclaration = svd2.get(k);
						md.parameters().add(ASTNode.copySubtree(ast, variableDeclaration));
					}
					SingleVariableDeclaration svd3 = ast.newSingleVariableDeclaration();
					String newParamName = "outlineFlag";
					svd3.setName(ast.newSimpleName(newParamName));
					svd3.setType(ast.newPrimitiveType(PrimitiveType.INT));
					md.parameters().add(svd3);
					Block newBlock = outlineMethod(ast, methods.get(currType).get(i).getBody(), methods.get(currType).get(j).getBody(), newParamName);
					md.setBody(newBlock);
					ast1.delete();
					ast2.delete();
				}
	}
	@SuppressWarnings("unchecked")
	Block outlineMethod(AST ast, Block bk1, Block bk2, String outlineFlag){
		Block result = ast.newBlock();
		List<Statement> list1 = bk1.statements();
		List<Statement> list2 = bk2.statements();
		int len1 = list1.size();
		int len2 = list2.size();
		int cursor1 = 0;
		int cursor2 = 0;
		Queue<Integer> queue1 = new LinkedBlockingQueue<Integer>();
		Queue<Integer> queue2 = new LinkedBlockingQueue<Integer>();
		for(int i = 0; i < len1; i++)
			for(int j = 0; j < len2; j++)
				if(sameStatement(list1.get(i), list2.get(j))){
					queue1.add(i);
					queue2.add(j);
					break;
				}
		while(cursor1 < len1 || cursor2 < len2){
			// if...then...else
			if(cursor1 != queue1.element() || cursor2 != queue2.element()){
				IfStatement ifstmt = ast.newIfStatement();
				InfixExpression infixexpr = ast.newInfixExpression();
				infixexpr.setLeftOperand(ast.newSimpleName(outlineFlag));
				infixexpr.setOperator(org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS);
				infixexpr.setRightOperand(ast.newNumberLiteral("1")); 
				ifstmt.setExpression(infixexpr);
				Block thenBlock = ast.newBlock();
				Block elseBlock = ast.newBlock();
				ifstmt.setThenStatement(thenBlock);
				ifstmt.setElseStatement(elseBlock);
				while(cursor1 < queue1.element()){
					thenBlock.statements().add(ASTNode.copySubtree(ast, list1.get(cursor1)));
					cursor1 += 1;
				}
				while(cursor2 < queue2.element()){
					elseBlock.statements().add(ASTNode.copySubtree(ast, list2.get(cursor2)));
					cursor2 += 1;
				}
				result.statements().add(ifstmt);
			}
			result.statements().add(ASTNode.copySubtree(ast, list1.get(cursor1)));
			cursor1 += 1;
			cursor2 += 1;
			queue1.poll();
			queue2.poll();
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	float getSimi(MethodDeclaration md1, MethodDeclaration md2){
		Block bl1 = md1.getBody();
		Block bl2 = md2.getBody();
		List<Statement> list1 = bl1.statements();
		List<Statement> list2 = bl2.statements();
		int totalStmt = list1.size() + list2.size();
		float sameStmt = 0;
		for(int i = 0; i < list1.size(); i++)
			for(int j = 0; j < list2.size(); j++)
				if(sameStatement(list1.get(i), list2.get(j))){
					sameStmt += 2; 
					break;
				}
		return sameStmt / totalStmt;
	}
	boolean sameStatement(Statement stmt1, Statement stmt2){
		if(stmt1.toString().compareTo(stmt2.toString()) == 0)
			return true;
		return false;
	}
}