package salience.db;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterManager {

	private static Twitter twitter = null;

	public static Twitter getTwitterInstance() {
		if (twitter == null) {
			final TwitterFactory tf = new TwitterFactory();
			twitter = tf.getInstance();
		}
		return twitter;
	}

}