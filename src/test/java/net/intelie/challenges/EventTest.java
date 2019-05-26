package net.intelie.challenges;

import org.junit.Test;

import net.evan.solution.EvanEventIterator;
import net.evan.solution.MemoryEventStore;

import static org.junit.Assert.assertEquals;

import java.util.Random;

public class EventTest {
    @Test
    public void thisIsAWarning() throws Exception {
        Event event = new Event("some_type", 123L);

        //THIS IS A WARNING:
        //Some of us (not everyone) are coverage freaks.
        assertEquals(123L, event.timestamp());
        assertEquals("some_type", event.type());
    }
    
    @Test
    public void StoreEvents() throws Exception{
    	EventStore store = new MemoryEventStore();
    	
    	store.insert(new Event("rock", 90L));
    	store.insert(new Event("rock", 100L));
    	store.insert(new Event("rock", 101L));
    	store.insert(new Event("rock", 102L));
    	store.insert(new Event("rock", 103L));
    	for(int i=0; i<1000; i++) store.insert(new Event("paper", 100L));
    	for(long i=0; i<5000; i++) store.insert(new Event("scissors", i));
    	for(long i=0; i<5000; i++) store.insert(new Event("scissors", 5000L - i));
    	store.insert(new Event("rock", 110L));
    	
    	EventIterator iterator = store.query("rock", 100L, 105L);
    	int countRock = 0;
    	while(iterator.moveNext()) {
    		countRock += 1;
    	}
    	assertEquals(4,countRock);
    	
    	
    	iterator = store.query("paper", 0L, 105L);
    	int countPaper = 0;
    	while(iterator.moveNext()) {
    		countPaper += 1;
    	}
    	assertEquals(1000,countPaper);
    	
    	
    	iterator = store.query("scissors", 0L, 100005L);
    	int countScissors = 0;
    	while(iterator.moveNext()) {
    		countScissors += 1;
    	}
    	assertEquals(10000,countScissors);
    	
    }
    
    
    @Test
    public void noEventsFound() throws Exception{
    	EventStore store = new MemoryEventStore();
    	
    	
    	EventIterator iterator = store.query("rock", 0L, 100L);
    	int countRock = 0;
    	while(iterator.moveNext()) {
    		countRock += 1;
    	}
    	assertEquals(0,countRock);
    	
    	
    	store.insert(new Event("rock", 90L));
    	store.insert(new Event("rock", 100L));
    	store.insert(new Event("rock", 101L));
    	store.insert(new Event("rock", 102L));
    	store.insert(new Event("rock", 103L));
    	for(int i=0; i<1000; i++) store.insert(new Event("paper", 100L));
    	for(long i=0; i<100; i++) store.insert(new Event("scissors", i));
    	store.insert(new Event("rock", 110L));
    	
    	
    	
    	iterator = store.query("lizard", 0L, 105L);
    	int countLizard = 0;
    	while(iterator.moveNext()) {
    		countLizard += 1;
    	}
    	assertEquals(0,countLizard);
    	
    	
    	iterator = store.query("scissors", 200L, 300L);
    	int countScissors = 0;
    	while(iterator.moveNext()) {
    		countScissors += 1;
    	}
    	assertEquals(0,countScissors);
    	
    }
    
    
    @Test
    public void catchingErrors() throws Exception{
    	EventStore store = new MemoryEventStore();
    	
    	
    	store.insert(new Event("rock", 90L));
    	store.insert(new Event("rock", 100L));
    	store.insert(new Event("rock", 101L));
    	store.insert(new Event("rock", 102L));
    	store.insert(new Event("rock", 103L));
    	for(int i=0; i<100; i++) store.insert(new Event("paper", 100L));
    	for(long i=0; i<100; i++) store.insert(new Event("scissors", i));
    	store.insert(new Event("rock", 110L));
    	
    	
    	EventIterator iterator = store.query("rock", 0L, 100L);
    	try {
    		iterator.current();
    		assertEquals(0,1);
    	}
    	catch(IllegalStateException e) {
    		assertEquals(1,1);
    		assert(e.getMessage().contains("Must call moveNext() first!"));
    	}
    	catch(Exception e) {
    		assertEquals(0,1);
    	}
    	
    	
    	
    	iterator = store.query("rock", 0L, 100L);
    	try {
    		while(iterator.moveNext()) iterator.current();
    		iterator.current();
    		assertEquals(0,1);
    	}
    	catch(IllegalStateException e) {
    		assertEquals(1,1);
    		assert(e.getMessage().contains("Iteration reached the end."));
    	}
    	catch(Exception e) {
    		assertEquals(0,1);
    	}
    	
    }
    
    @Test
    public void removing() throws Exception{
    	EventStore store = new MemoryEventStore();
    	
    	store.insert(new Event("rock", 90L));
    	store.insert(new Event("rock", 100L));
    	store.insert(new Event("rock", 101L));
    	store.insert(new Event("rock", 102L));
    	store.insert(new Event("rock", 103L));
    	store.insert(new Event("rock", 110L));
    	
    	store.insert(new Event("paper", 5L));
    	store.insert(new Event("paper", 6L));
    	store.insert(new Event("paper", 7L));
    	store.insert(new Event("paper", 8L));
    	store.insert(new Event("paper", 9L));
    	store.insert(new Event("paper", 10L));
    	
    	store.insert(new Event("scissors", 5L));
    	store.insert(new Event("scissors", 6L));
    	store.insert(new Event("scissors", 7L));
    	store.insert(new Event("scissors", 8L));
    	store.insert(new Event("scissors", 9L));
    	store.insert(new Event("scissors", 10L));
    	
    	store.removeAll("rock");
    	
    	EventIterator iterator = store.query("rock", 100L, 105L);
    	int countRock = 0;
    	while(iterator.moveNext()) {
    		countRock += 1;
    	}
    	assertEquals(0,countRock);
    	
    	
    	iterator = store.query("paper", 0L, 100L);
    	int countPaper = 0;
    	while(iterator.moveNext()) {
    		if(iterator.current().timestamp() >= 8L) iterator.remove();
    		countPaper += 1;
    	}
    	assertEquals(6,countPaper);
    	
    	iterator = store.query("paper", 0L, 100L);
    	countPaper = 0;
    	while(iterator.moveNext()) {
    		countPaper += 1;
    	}
    	assertEquals(3,countPaper);
    	
    	
    	
    	iterator = store.query("scissors", 0L, 100L);
    	int countScissors = 0;
    	while(iterator.moveNext()) {
    		if(iterator.current().timestamp() == 8L) store.removeAll("scissors");
    		countScissors += 1;
    	}
    	assertEquals(4,countScissors);
    	
    }
    
    
    @Test
    public void StoreAfterQuery() throws Exception{
    	EventStore store = new MemoryEventStore();
    	
    	store.insert(new Event("rock", 90L));
    	store.insert(new Event("rock", 100L)); // found
    	store.insert(new Event("rock", 101L)); // found
    	store.insert(new Event("rock", 102L)); // found
    	store.insert(new Event("rock", 103L)); // found
    	store.insert(new Event("rock", 110L));
    	
    	EventIterator iterator = store.query("rock", 100L, 105L);
    	
    	// with my implementation, the query results DO get updated if things are added.
    	// this ends up being the fastest and most memory efficient because the query
    	// accesses the events right where they are stored
    	
    	// if this isn't the expected behavior, I'd have to copy the events into
    	// the iterator locally. it's doable!
    	
    	store.insert(new Event("rock", 100L)); // found
    	store.insert(new Event("rock", 103L)); // found
    	store.insert(new Event("rock", 104L)); // found
    	
    	int countRock = 0;
    	while(iterator.moveNext()) {
    		countRock += 1;
    	}
    	assertEquals(7,countRock);
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    private class InsertThread implements Runnable{

    	private final String type;
    	private final EventStore store;
    	private Random rand;
    	
		@Override
		public void run() {
			for(long i=0; i<100; i++) {
				try {
					Thread.sleep(Math.abs(rand.nextInt()%2));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				store.insert(new Event(type, i));
			}
		}
		public InsertThread(EventStore s, String t) {
			store = s;
			type = t;
			rand = new Random();
		}
    	
    }
    
    private class DeleteThread implements Runnable{

    	private final String type;
    	private final EventStore store;
    	
		@Override
		public void run() {
			store.removeAll(type);
		}
		public DeleteThread(EventStore s, String t) {
			store = s;
			type = t;
		}
    	
    }
    
    
    @Test
    public void threading() throws Exception{
    	for(int i = 0; i < 100; i++) {
	    	EventStore store = new MemoryEventStore();
	    	
	    	InsertThread inThread = new InsertThread(store,"rock");
	    	Thread t1 = new Thread(inThread, "t1");
	    	Thread t2 = new Thread(inThread, "t2");
	    	t1.start();
	    	t2.start();
	    	// wait for them to do their thing
	    	t1.join();
	        t2.join();
	        
	        EventIterator iterator = store.query("rock", 0L, 100L);
	    	int countRock = 0;
	    	while(iterator.moveNext()) {
	    		countRock += 1;
	    	}
	    	assertEquals(200,countRock);
    	}
    }
    
    
    
    
    
    
}