package com.api.demo.pricedata.repositories.pricedata;

import org.h2.util.Task;

public class MrMeeseeks {

	public MrMeeseeks(Task task) {
		System.out.println("I'm Mr. Meeseeks! Look at me!");
		int attempts = 0;
		int patience = 100;
		while(!task.isFinished()) {
			task.execute();
			attempts += 1;
			patience -=1;
		}

		if(attempts > 50) {
			MrMeeseeks help = new MrMeeseeks(task);
		}

		if(patience < 20) { getMad(); }

		if(patience < 0) { kill(); }
	}

	private void getMad() { /* Existence is pain! */ };
	private void kill() { /* Dammit Jerry! */ };
}
