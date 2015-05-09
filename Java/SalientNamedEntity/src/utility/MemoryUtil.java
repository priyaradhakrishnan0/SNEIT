package utility;



public class MemoryUtil {

	
	public static long getUsedMemory() {
		System.gc() ;
		System.gc() ;
		System.gc() ;
		
		return ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / ( 1024 * 1024 ) ;
	}
	
	public static void printUsedMemory() {
		System.out.println(String.format("memory used is %d MB", getUsedMemory())) ;
	}
	
}