package alienrabble.util;

/***
 * A small class that is given an integer N and returns
 * a random permutation of the set [0,1,..,N-1].
 * 
 * @author monkey
 *
 */
public class RandomPermutation {
//	    private static int[] a = {1,2,3,4,5,6};

	private int randSeed; //TODO work out how to use this!
	private int size;
	private boolean initialized = false;
	private int[] outArray;
	
	public RandomPermutation(){}
	public RandomPermutation(int randSeed){
		this.randSeed = randSeed;
	}
	public void setRandSeed(int randSeed){
		this.randSeed = randSeed;
		initialized = false;
	}
	public void setSize(int size){
		this.size = size;
		outArray = new int [size];
		for(int p = 0;p<size;p++){
			outArray[p]=p;
		}
		initialized = true;
	}


    // Print content of array 
    public int[] getPermutation() {
    	if (!initialized) return null;
    	int[] a = (int[])outArray.clone();
		return a;
	}

	/***
	 *  It produces the next random permutation by looping
	 *  through the current permutation and swapping each item
	 *  with some other randomly selected item.
	 */
    public boolean next(){
    	if (!initialized) return false;
		//int[] b = (int[])outArray.clone();
		for (int k = outArray.length - 1; k > 0; k--) {
		    int w = (int)Math.floor(Math.random() * (k+1));
		    int temp = outArray[w];
		    outArray[w] = outArray[k];
		    outArray[k] = temp;
		}
		
		return true;
	}
    
   

}

