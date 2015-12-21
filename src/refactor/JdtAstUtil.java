package refactor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;  
import org.eclipse.jdt.core.dom.CompilationUnit;  
public class JdtAstUtil {  
    /** 
    * get compilation unit of source code 
    * @param javaFilePath  
    * @return CompilationUnit 
    */  
    public static CompilationUnit getCompilationUnit(String source){  
    @SuppressWarnings("deprecation")
	ASTParser astParser = ASTParser.newParser(AST.JLS3);  
        astParser.setSource(source.toCharArray());  
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);  
        CompilationUnit result = (CompilationUnit) (astParser.createAST(null));
        return result;  
    }  
}
