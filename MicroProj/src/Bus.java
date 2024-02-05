
public class Bus {
	   private static Bus instance = null;

	    int value;
	    String tag;
	    public static Bus getInstance() {
	        if (instance == null) {
	            instance = new Bus();
	        }
	        return instance;
	    }
	  
	    public Bus() {
	       value=0;
	       tag="";
	    }

	    public int getValue() {
	        return value;
	    }

	    public void setValue( int value) {
	        this.value = value;
	    }
	    

	    public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
		public void print() {
	        System.out.println("Bus tag: "+tag+" value: "+value);
	    }

	}