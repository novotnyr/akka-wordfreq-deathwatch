package sk.upjs.ics.novotnyr.akka;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import akka.actor.UntypedActor;

public class SentenceCountActor extends UntypedActor {
	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof String) { 
			String sentence = (String) message;
			
			Map<String, Integer> freqs = getFrequencies(sentence);
			getSender().tell(freqs, getSelf());
		} else {
			unhandled(message);
		}
	}

	private Map<String, Integer> getFrequencies(String sentence) {
		Map<String, Integer> freqs = new HashMap<String, Integer>();
		
		Scanner scanner = new Scanner(sentence);
		while(scanner.hasNext()) {
			String word = scanner.next();
			
			int frequency = 1;
			if(freqs.containsKey(word)) {
				frequency += freqs.get(word);
			} 
			freqs.put(word, frequency);
		}
		return freqs;
	}

}
