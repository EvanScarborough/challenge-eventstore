package net.evan.solution;

import net.intelie.challenges.Event;

public class EventLink {
	
	// EventLink is basically a node in a linked list that also stores an event.
	
	private final Event event;
	
	private final MemoryEventStore store;
	
	// pointers to the next and previous events
	private EventLink next = null;
	private EventLink previous = null;
	
	public EventLink(MemoryEventStore s, Event e) {
		store = s;
		event = e;
	}
	// for when you want to make an EventLink that just points to some place in the list
	public static EventLink makePreEvent(EventLink e) {
		EventLink ret = new EventLink(null,null);
		ret.setNext(e);
		return ret;
	}
	
	// simple getters
	public Event getEvent() {return event;}
	public EventLink getPrevious() {return previous;}
	public EventLink getNext() {return next;}
	public MemoryEventStore getStore() {return store;}
	
	// insert an event and relink the forward and backwards links
	public void setPrevious_Relink(EventLink link) {
		// note that these will overwrite the inserted link's previous and next.
		// if it did otherwise we'd be asking for infinite loops.
		if(previous != null) {
			previous.setNext(link);
			link.setPrevious(previous);
		}
		previous = link;
		link.setNext(this);
	}
	public void setNext_Relink(EventLink link) {
		if(next != null) {
			next.setPrevious(link);
			link.setNext(next);
		}
		next = link;
		link.setPrevious(this);
	}
	
	// just set the previous and next
	public EventLink setPrevious(EventLink link) {
		EventLink ret = previous;
		previous = link;
		return ret;
	}
	public EventLink setNext(EventLink link) {
		EventLink ret = next;
		next = link;
		return ret;
	}
	
	// gets the next event if the next event is at the same time as this one.
	public EventLink getNext_sameTime() {
		if(next == null)return null;
		if(next.getEvent().timestamp() != getEvent().timestamp()) return null;
		return next;
	}
	
	// push an event to the back of the list before you get to a new timestamp
	public void push_back_sameTime(EventLink link) {
		// there's not a next, so we just insert it
		if(next == null) {next=link; return;}
		if(next.getEvent().timestamp() == event.timestamp()) {
			// if the next time is the same as this one, then the next event should handle placing this.
			next.push_back_sameTime(link);
			return;
		}
		// otherwise, this one is the last of the same time, so insert link here
		setNext_Relink(link);
	}
	// get the last event overall
	public EventLink getLast_sameTime() {
		if(next==null)return this;
		if(next.getEvent().timestamp()!=event.timestamp())return this;
		return next.getLast_sameTime();
	}
	// get the last event overall
	public EventLink getLast() {
		if(next==null)return this;
		return next.getLast();
	}
	
	// breaks all of the links to and from this event. Good for completely removing an event
	public void breakLinks() {
		if(next!=null) {next.setPrevious(previous);}
		if(previous!=null) {previous.setNext(next);}
		previous = null;
		next = null;
	}
	
	// break only the incoming links. This is useful for when we want to remove an event
	// but still iterate from it
	public void breakLinksIn() {
		if(next!=null) {next.setPrevious(previous);}
		if(previous!=null) {previous.setNext(next);}
	}
	
	
	// get the next event as long as it's before the end time
	public EventLink getNextMatch(long end) {
		if(next==null)return null;
		if(next.getEvent().timestamp() >= end)return null;
		return next;
	}
}
