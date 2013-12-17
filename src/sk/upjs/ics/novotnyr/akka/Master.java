package sk.upjs.ics.novotnyr.akka;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.Broadcast;
import akka.routing.RoundRobinRouter;

public class Master extends UntypedActor {
	private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);	
	
	private Map<String, Integer> allFrequencies = new HashMap<String, Integer>();

	private ActorRef sentenceFrequencyCounters = getContext().actorOf(
			Props.create(SentenceCountActor.class).withRouter(
					new RoundRobinRouter(3)));

	@Override
	public void preStart() throws Exception {
		super.preStart();		
		getContext().watch(sentenceFrequencyCounters);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object message) throws Exception {
		logger.debug(message.toString());
		
		if (message instanceof String) {
			sentenceFrequencyCounters.tell(message, getSelf());
		} else if (message instanceof Map<?, ?>) {
			allFrequencies = aggregate(allFrequencies, (Map<String, Integer>) message);
		} else if (message instanceof ResultRequest) {
			sentenceFrequencyCounters.tell(new Broadcast(PoisonPill.getInstance()), getSelf());

		} else if (message instanceof Terminated) {
			logger.info( ((Terminated) message) + " terminated");

			System.err.println(allFrequencies);
			
			getContext().system().shutdown();
		} else {
			unhandled(message);
		}
	}

	public static <T> Map<T, Integer> aggregate(Map<T, Integer> data1,
			Map<T, Integer> data2) {
		Map<T, Integer> aggregatedData = new HashMap<T, Integer>(data1);
		for (T key : data2.keySet()) {
			int existingFrequency = 0;
			if (aggregatedData.containsKey(key)) {
				existingFrequency = aggregatedData.get(key);
			}
			aggregatedData.put(key, existingFrequency + data2.get(key));
		}
		return aggregatedData;
	}
}
