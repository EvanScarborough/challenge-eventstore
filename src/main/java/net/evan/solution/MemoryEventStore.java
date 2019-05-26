package net.evan.solution;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.intelie.challenges.Event;
import net.intelie.challenges.EventIterator;
import net.intelie.challenges.EventStore;

public class MemoryEventStore implements EventStore{

	// I decided to store data in basically a series of TreeMaps. These treemaps index by type,
	// then index events by timestamp. The events are also stored in 'EventLinks' which function
	// as a doubly-linked list with a pointer to the next and previous events. (See EventLink.java!)
	private Map<String,NavigableMap<Long,EventLink>> events = new TreeMap<String,NavigableMap<Long,EventLink>>();
	
	/**
	 * Inserts an event into the EventStore. Handles relinking of the lists and updating indices
	 * 
	 */
	@Override
	public void insert(Event event) {
		// we need to get the current time index, or make one
		NavigableMap<Long,EventLink> timeIndex;
		synchronized(events) {
			if(!events.containsKey(event.type())) {
				// there have been no events of this type yet, make a new map
				timeIndex = new TreeMap<Long,EventLink>();
				events.put(event.type(),timeIndex);
			}
			else {
				timeIndex = events.get(event.type());
			}
		}
		// we're going to synchronize the entire timeIndex. This means we can't insert multiple things at a time.
		// it would, of course, be better to synchronize something smaller, but I think that without implementing
		// my own tree data structure I couldn't get much better than this.
		// should be a pretty fast operation anyway.
		synchronized(timeIndex) {
			EventLink link = new EventLink(this,event);
			if(!timeIndex.containsKey(event.timestamp())) {
				// that timestamp does NOT exist in the index. We'll have to make an entry.
				
				// we need to do some relinking!
				Long prevTime = getPrevious(event);
				Long nextTime = getPrevious(event);
				if(prevTime == null && nextTime == null) {
					// it's the only one with this type, don't add any linking.
				}
				else if(prevTime == null) {
					// there's only a next
					timeIndex.get(nextTime).setPrevious_Relink(link);
				}
				else {
					// either there's a next only or both. Either way, we can set both links with this
					timeIndex.get(prevTime).getLast_sameTime().setNext_Relink(link);
				}
				timeIndex.put(event.timestamp(),link);
			}
			else {
				// that timestamp DOES already exist in the index. Just put it at the end of the event linked list at this index
				timeIndex.get(event.timestamp()).push_back_sameTime(link);
			}
		}
	}
	
	// leverage the navigablemap to get an earlier or later timestamp
	private Long getPrevious(Event event) {
		if(!events.containsKey(event.type())) return null;
		return events.get(event.type()).lowerKey(event.timestamp());
	}
	private Long firstOnOrAfter(String type,long time) {
		if(!events.containsKey(type)) return null;
		if(events.get(type).containsKey(time))return time;
		return events.get(type).higherKey(time);
	}
	private Long getNext(Event event) {
		if(!events.containsKey(event.type())) return null;
		return events.get(event.type()).higherKey(event.timestamp());
	}
	
	
	// remove a single eventlink
	public void remove(EventLink link) {
		if(!events.containsKey(link.getEvent().type())) return;
		NavigableMap<Long,EventLink> timeIndex = events.get(link.getEvent().type());
		synchronized(timeIndex) {
			// the timeindex might not directly contain the eventlink. It could be that it
			// holds a different event with the same time that was added before this one.
			if(timeIndex.containsValue(link)) {
				// that link is one of the values in the time index... we need to remove it
				EventLink nextSameTime = link.getNext_sameTime();
				if(nextSameTime == null) {
					// it was the only one at that time. just remove the key from the index
					timeIndex.remove(link.getEvent().timestamp());
				}
				else {
					// otherwise, we need to remap the timestamp to nextSameTime
					timeIndex.replace(nextSameTime.getEvent().timestamp(), nextSameTime);
				}
			}
			// break links to link (only TO because we want it to still work in the iterator)
			link.breakLinksIn();
		}
	}

	
	// removes all events of a certain type
	@Override
	public void removeAll(String type) {
		if(!events.containsKey(type)) return;
		NavigableMap<Long,EventLink> timeIndex = events.get(type);
		synchronized(timeIndex) {
			// don't allow insertions, deletions, or queries until we're done deleting things.
			Long endTime = timeIndex.lastKey();
			if(endTime==null)return;
			EventLink last = timeIndex.get(endTime).getLast();
			while(last != null) {
				EventLink prev = last.getPrevious();
				last.breakLinks();
				last = prev;
				if(last==null)break;
				if(last.getEvent().timestamp()!=endTime) {
					timeIndex.remove(endTime);
					endTime = last.getEvent().timestamp();
				}
			}
		}
		events.remove(type);
	}

	// basically just returns an iterator with a reference to the first event that matches
	@Override
	public EventIterator query(String type, long startTime, long endTime) {
		if(!events.containsKey(type)) { return new EvanEventIterator(null,type,endTime); }
		NavigableMap<Long,EventLink> timeIndex = events.get(type);
		synchronized(timeIndex) {
			// this should be a very quick operation, but we will synchronize it to prevent corruption
			Long onOrAfterTime = firstOnOrAfter(type,startTime);
			if(onOrAfterTime==null) {
				return new EvanEventIterator(null,type,endTime);
			}
			return new EvanEventIterator(EventLink.makePreEvent(timeIndex.get(onOrAfterTime)),type,endTime);
		}
	}

}
