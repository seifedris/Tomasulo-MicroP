public class RegisterFile {


    Object[] registers;
    static RegisterFile instance = null;
    Bus bus=Bus.getInstance();

    public static RegisterFile getInstance() {
        if (instance == null) {
            instance = new RegisterFile(32);
        }
        return instance;
    }

    public RegisterFile(int size) {
        registers = new Object[size];
        for(int i=0;i!=registers.length;i++) {
        	registers[i]=(Integer)i;
        }
    }

    public Object read(int register) {
        return registers[register];
    }

    public void write(int register, Object value) {
        registers[register] = value;
    }

    public void print() {
    	System.out.println("Registers");
        for (int i = 0; i < registers.length; i++) {
            System.out.print("R" + i + ": " + registers[i] + " - ");
        }
        System.out.println("");
    }
    public void fetchFromBus() {
    	for (int i = 0; i < registers.length; i++) {
			if (registers[i].equals(bus.tag)) {
				
				registers[i]= bus.value;
			}
		}
    }

}
