
public class Test implements Runnable {

	public static void main(String [] args) {
		new Test(1000);
		new Test(500);
		while(true) {
			try {
				Thread.sleep(250);
				System.out.println(Thread.currentThread().getName());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	Thread me = new Thread(this);
	int interval;
	public Test(int interval) {
		this.interval = interval;
		me.start();
	}

	public void run() {
		while(true) {
			try {
				System.out.println(Thread.currentThread().getName());
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
		
	}

}
