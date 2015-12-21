package main;
import java.util.ArrayList;
import java.util.List;
public class Main {
	public static int mVariable1, m_variable2, MVariable3, M_Variable4;
	public static double let_it_go;
	private static boolean hello, println;
	public static void main(){
		mVariable1 = 1;
		m_variable2 = 2;
		MVariable3 = 3;
		M_Variable4 = 4;
		let_it_go = 5.0;
		hello = true;
		m_variable2 = mVariable1 + M_Variable4;
		if(let_it_go < 10){
			System.out.println(m_variable2);
			System.out.println(m_variable2);
		}
		while(1){
			if(true){
				return 1;
			}
			else{
				break;
			}
			let_it_go = 5.0;
		}
		System.out.println("Hello World");
		return 0;
		M_Variable4 = 4;
		let_it_go = 5.0;
		hello = true;
	}
	public static void hello(int i){
		let_it_go = 2.0;
		mVariable1++;
		M_Variable4--;
	}
	public static void hello2(double j){
		let_it_go = 1.0;
		hello = false;
		mVariable1++;
		M_Variable4--;
	}
}
class A{
	int mVariable5;
	int M_Variable6;
}