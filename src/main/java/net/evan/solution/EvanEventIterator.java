package net.evan.solution;

import java.util.HashSet;
import java.util.Set;

import net.intelie.challenges.Event;
import net.intelie.challenges.EventIterator;

public class EvanEventIterator implements EventIterator{
	
	// The EvanEventIterator basically just points at an EventLink and relies
	// on that already having an accurate pointer to the next event.
	// it'll then just keep looking at the next one until it reaches the end of the time range.
	
	// note that if events are ever added or removed from the store, the iterator will
	// reflect those changes. I wasn't sure if that was desired. If it isn't I'd have the
	// EventIterator hold a list of the events and upon querying that list is populated.
	
	private EventLink currentEventLink;
	private boolean startedIterate = false;
	
	private final String matchType;
	private final long maxTime;
	
	
	public EvanEventIterator(EventLink start, String type, long end) {
		currentEventLink = start;
		matchType = type;
		maxTime = end;
	}
	
	@Override
	public void close() throws Exception {
		// I... don't think this has to do anything.
	}

	@Override
	public boolean moveNext() {
		// just move to the next event. Return false if there is none, true if there is
		if(currentEventLink==null) return false;
		startedIterate = true;
		currentEventLink = currentEventLink.getNextMatch(maxTime);
		if(currentEventLink==null) return false;
		return true;
	}

	@Override
	public Event current() {
		// just get the current event from the event link. throw the appropriate errors too
		if(!startedIterate) throw new IllegalStateException("Must call moveNext() first!");
		if(currentEventLink==null) throw new IllegalStateException("Iteration reached the end.");
		return currentEventLink.getEvent();
	}

	@Override
	public void remove() {
		// the event store handles removing events. Let it do that.
		if(!startedIterate) throw new IllegalStateException("Must call moveNext() first!");
		if(currentEventLink==null) throw new IllegalStateException("Iteration reached the end.");
		currentEventLink.getStore().remove(currentEventLink);
	}

}
