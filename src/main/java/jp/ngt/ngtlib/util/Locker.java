package jp.ngt.ngtlib.util;

public class Locker {
	private boolean locking;

	public synchronized void lock() {
		while (this.locking) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.locking = true;
	}

	public synchronized void unlock() {
		this.locking = false;
		this.notifyAll();
	}
}