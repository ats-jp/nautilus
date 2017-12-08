package nautilus.test;

/////////////////////////////////////////////////////////////////////////
//
//Example that shows listing all spooled files on a server asynchronously using
//the PrintObjectListListener interface to get feedback as the list is being built.
//Listing asynchronously allows the caller to start processing the list objects
//before the entire list is built for a faster perceived response time
//for the user.
//
/////////////////////////////////////////////////////////////////////////

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.PrintObjectListEvent;
import com.ibm.as400.access.PrintObjectListListener;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;

public class NPExampleListSplfAsynch extends Object
	implements PrintObjectListListener {

	private AS400 system_;

	private boolean fListError;

	private boolean fListClosed;

	private boolean fListCompleted;

	private Exception listException;

	private int listObjectCount;

	public NPExampleListSplfAsynch(AS400 system) {
		system_ = system;
	}

	// list all spooled files on the server asynchronously using a listener
	public void listSpooledFiles() {
		fListError = false;
		fListClosed = false;
		fListCompleted = false;
		listException = null;
		listObjectCount = 0;

		try {
			String strSpooledFileName;
			boolean fCompleted = false;
			int listed = 0, size;

			if (system_ == null) {
				system_ = new AS400();
			}

			System.out.println(
				" Now receiving all spooled files Asynchronously using a listener");

			SpooledFileList splfList = new SpooledFileList(system_);

			// set filters, all users, on all queues
			splfList.setUserFilter("*ALL");
			splfList.setQueueFilter("/QSYS.LIB/%ALL%.LIB/%ALL%.OUTQ");

			// add the listener.
			splfList.addPrintObjectListListener(this);

			// open the list, openAsynchronously returns immediately
			splfList.openAsynchronously();

			do {
				// wait for the list to have at least 25 objects or to be done
				waitForWakeUp();

				fCompleted = splfList.isCompleted();
				size = splfList.size();

				// output the names of all objects added to the list
				// since we last woke up
				while (listed < size) {
					if (fListError) {
						System.out
							.println(" Exception on list - " + listException);
						break;
					}

					if (fListClosed) {
						System.out.println(
							" The list was closed before it completed!");
						break;
					}

					SpooledFile splf = (SpooledFile) splfList
						.getObject(listed++);
					if (splf != null) {
						// output this spooled file name
						strSpooledFileName = splf
							.getStringAttribute(SpooledFile.ATTR_SPOOLFILE);
						System.out
							.println(" spooled file = " + strSpooledFileName);
					}
				}

			} while (!fCompleted);

			// clean up after we are done with the list
			splfList.close();
			splfList.removePrintObjectListListener(this);
		}

		catch (ExtendedIllegalStateException e) {
			System.out.println(" The list was closed before it completed!");
		}

		catch (Exception e) {
			// ...handle any other exceptions...
			e.printStackTrace();
		}

	}

	// This is where the foreground thread waits to be awaken by the
	// the background thread when the list is updated or it ends.
	private synchronized void waitForWakeUp() throws InterruptedException {
		// don''t go back to sleep if the listener says the list is done
		if (!fListCompleted) {
			wait();
		}
	}

	// The following methods implement the PrintObjectListListener interface

	// This method is invoked when the list is closed.
	@Override
	public void listClosed(PrintObjectListEvent event) {
		System.out.println("*****The list was closed*****");
		fListClosed = true;
		synchronized (this) {
			// Set flag to indicate that the list has
			// completed and wake up foreground thread.
			fListCompleted = true;
			notifyAll();
		}
	}

	// This method is invoked when the list is completed.
	@Override
	public void listCompleted(PrintObjectListEvent event) {
		System.out.println("*****The list has completed*****");
		synchronized (this) {
			// Set flag to indicate that the list has
			// completed and wake up foreground thread.
			fListCompleted = true;
			notifyAll();
		}
	}

	// This method is invoked if an error occurs while retrieving
	// the list.
	@Override
	public void listErrorOccurred(PrintObjectListEvent event) {
		System.out.println("*****The list had an error*****");
		fListError = true;
		listException = event.getException();
		synchronized (this) {
			// Set flag to indicate that the list has
			// completed and wake up foreground thread.
			fListCompleted = true;
			notifyAll();
		}
	}

	// This method is invoked when the list is opened.
	@Override
	public void listOpened(PrintObjectListEvent event) {
		System.out.println("*****The list was opened*****");
		listObjectCount = 0;
	}

	// This method is invoked when an object is added to the list.
	@Override
	public void listObjectAdded(PrintObjectListEvent event) {
		// every 25 objects we'll wake up the foreground
		// thread to get the latest objects...
		if ((++listObjectCount % 25) == 0) {
			System.out.println("*****25 more objects added to the list*****");
			synchronized (this) {
				// wake up foreground thread
				notifyAll();
			}
		}
	}

	public static void main(String args[]) {
		NPExampleListSplfAsynch list = new NPExampleListSplfAsynch(new AS400());
		try {
			list.listSpooledFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
