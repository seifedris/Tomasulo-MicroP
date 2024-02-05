public class Cache {

    //make this class singleton
    private static Cache instance = null;

    int[] cache;

    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache(32);
        }
        return instance;
    }
    public Cache(int size) {
        cache = new int[size];
        for(int i=0;i!=cache.length;i++)
        	cache[i]=i+1;
    }

    public Cache(int[] cache) {
        this.cache = cache;
    }

    public int read(int address) {
        return cache[address];
    }

    public void write(int address, int value) {
        cache[address] = value;
    }

    public void print() {
    	System.out.println("Cache");
        for (int i = 0; i < cache.length; i++) {
        	
            System.out.print("C" + i + ": " + cache[i]+ " ");
           
        }
        System.out.println("\n");
    }

}
