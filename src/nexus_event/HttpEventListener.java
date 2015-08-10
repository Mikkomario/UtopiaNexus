package nexus_event;

import genesis_event.EventSelector;
import genesis_event.Handled;

/**
 * HttpEventListeners are interested in http events
 * @author Mikko Hilpinen
 * @since 2.5.2015
 */
public interface HttpEventListener extends Handled
{
	/**
	 * @return The eventSelector that defines which events the listener will be informed about
	 */
	public EventSelector<HttpEvent> getHttpEventSelector();
	
	/**
	 * This method is called when an HttpEvent the listener is interested in occurs
	 * @param e The http event that just occurred
	 */
	public void onHttpEvent(HttpEvent e);
}
